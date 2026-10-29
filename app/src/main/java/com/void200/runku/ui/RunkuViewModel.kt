package com.void200.runku.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.void200.runku.data.Activity
import com.void200.runku.data.ActivityType
import com.void200.runku.data.RunkuDatabase
import com.void200.runku.data.SettingsStore
import com.void200.runku.data.TargetType
import com.void200.runku.data.TrackPoint
import com.void200.runku.hr.BleHeartRate
import com.void200.runku.hr.HrZones
import com.void200.runku.location.Geo
import com.void200.runku.location.LocationSample
import com.void200.runku.location.GpsSmoother
import com.void200.runku.location.LocationTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Live run state while tracking. */
data class LiveState(
    val running: Boolean = false,
    val paused: Boolean = false,
    val startedAt: Long = 0,
    val elapsedMillis: Long = 0,
    val distanceMeters: Double = 0.0,
    val elevationGain: Double = 0.0,
    val currentBpm: Int = 0,
    val avgBpm: Int = 0,
    val maxBpm: Int = 0,
    val points: List<LocationSample> = emptyList(),
    val smoothedSpeedMps: Double = 0.0,
    val targetType: TargetType = TargetType.FREE,
    val targetValue: Double = 0.0,
) {
    val paceSecPerKm: Double get() = if (distanceMeters < 20.0) 0.0 else if (elapsedMillis <= 0) 0.0 else (elapsedMillis / 1000.0) / (distanceMeters / 1000.0)
    val speedKmh: Double get() = Geo.speedKmh(distanceMeters, elapsedMillis)
    val progress: Float get() = when (targetType) {
        TargetType.DISTANCE -> if (targetValue > 0) (distanceMeters / (targetValue * 1000.0)).toFloat().coerceIn(0f, 1f) else 0f
        TargetType.DURATION -> if (targetValue > 0) (elapsedMillis / (targetValue * 1000.0)).toFloat().coerceIn(0f, 1f) else 0f
        else -> 0f
    }
}

class RunkuViewModel(app: Application) : AndroidViewModel(app) {

    private val db = RunkuDatabase.get(app)
    private val settings = SettingsStore(app)

    val settingsFlow = settings.flow.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsStore.Snapshot())

    val activities: StateFlow<List<Activity>> =
        db.activityDao().observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _live = MutableStateFlow(LiveState())
    val live: StateFlow<LiveState> = _live.asStateFlow()

    private var tickerJob: Job? = null
    private var gpsJob: Job? = null
    private var hrJob: Job? = null
    private var last: LocationSample? = null
    private val smoother = GpsSmoother()
    private var activeStarted = 0L
    private var activeAccumulated = 0L
    private var saving = false
    private val pointHr = mutableMapOf<Long, Int>()

    fun setTrackPoint(bpm: Int) = onHeartRate(bpm)
    fun isRunning(): Boolean = _live.value.running

    /** Start GPS + HR collection for the current run. Call with a Context when a run is active. */
    fun bindSensors(context: Context, hrSource: String = "ble") {
        gpsJob?.cancel()
        gpsJob = viewModelScope.launch {
            LocationTracker(context).stream(1000L).collect { sample ->
                onSample(sample)
            }
        }
        hrJob?.cancel()
        if (hrSource == "ble") {
            hrJob = viewModelScope.launch {
                runCatching {
                    BleHeartRate(context).stream().collect { bpm -> onHeartRate(bpm) }
                }
            }
        }
    }

    fun unbindSensors() {
        gpsJob?.cancel(); gpsJob = null
        hrJob?.cancel(); hrJob = null
    }

    // -------- tracking --------
    fun startRun(type: ActivityType = ActivityType.RUN, targetType: TargetType = TargetType.FREE, targetValue: Double = 0.0) {
        if (_live.value.running) return
        last = null
        pointHr.clear()
        activeAccumulated = 0L
        activeStarted = android.os.SystemClock.elapsedRealtime()
        hrSamples = 0; hrSum = 0L; smoother.reset()
        _live.value = LiveState(
            running = true, startedAt = System.currentTimeMillis(),
            targetType = targetType, targetValue = targetValue,
        )
        startTicker()
    }

    fun onSample(sample: LocationSample) {
        val s = _live.value
        if (!s.running || s.paused) return
        val res = smoother.process(sample, s.elapsedMillis / 1000.0) ?: return
        var gain = 0.0
        last?.let { prev ->
            if (sample.elevation > prev.elevation) gain = sample.elevation - prev.elevation
        }
        last = sample
        pointHr[res.accepted.timestamp] = s.currentBpm
        _live.value = s.copy(
            distanceMeters = s.distanceMeters + res.addedDistance,
            elevationGain = s.elevationGain + gain,
            points = s.points + res.accepted,
            smoothedSpeedMps = res.stableSpeedMps,
        )
    }

    private var hrSamples = 0
    private var hrSum = 0L

    fun onHeartRate(bpm: Int) {
        if (bpm !in 30..240 || !_live.value.running || _live.value.paused) return
        hrSamples++; hrSum += bpm
        val s = _live.value
        val count = hrSamples
        val newAvg = (hrSum / count).toInt()
        _live.value = s.copy(
            currentBpm = bpm,
            avgBpm = newAvg,
            maxBpm = maxOf(s.maxBpm, bpm),
        )
    }

    fun pause() {
        if (!_live.value.running || _live.value.paused) return
        activeAccumulated += android.os.SystemClock.elapsedRealtime() - activeStarted
        _live.value = _live.value.copy(paused = true, elapsedMillis = activeAccumulated)
        smoother.reset(); last = null
    }
    fun resume() {
        if (!_live.value.running || !_live.value.paused) return
        activeStarted = android.os.SystemClock.elapsedRealtime()
        smoother.reset(); last = null
        _live.value = _live.value.copy(paused = false)
    }

    fun stopRun(onSaved: (Long) -> Unit = {}) {
        if (!_live.value.running || saving) return
        saving = true
        val s = _live.value
        _live.value = s.copy(paused = true)
        stopTicker()
        unbindSensors()
        viewModelScope.launch {
            val endTime = System.currentTimeMillis()
            val activity = Activity(
                name = "Lari ${java.text.SimpleDateFormat("d MMM", java.util.Locale("id")).format(java.util.Date(s.startedAt))}",
                type = ActivityType.RUN,
                startTime = s.startedAt,
                endTime = endTime,
                distanceMeters = s.distanceMeters,
                durationMillis = s.elapsedMillis,
                avgPaceSecPerKm = s.paceSecPerKm,
                avgSpeedKmh = s.speedKmh,
                elevationGainM = s.elevationGain,
                avgHr = s.avgBpm,
                maxHr = s.maxBpm,
                targetType = s.targetType,
                targetValue = s.targetValue,
                completed = true,
            )
            val id = db.activityDao().insert(activity)
            val pts = s.points.mapIndexed { i, p ->
                TrackPoint(
                    activityId = id, lat = p.lat, lon = p.lon, elevation = p.elevation,
                    speedMps = p.speedMps, bearing = p.bearing, accuracy = p.accuracy,
                    timestamp = p.timestamp, seq = i, hr = pointHr[p.timestamp] ?: 0,
                )
            }
            db.trackPointDao().insertAll(pts)
            _live.value = LiveState()
            saving = false
            onSaved(id)
        }
    }

    fun cancelRun() { stopTicker(); unbindSensors(); _live.value = LiveState() }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val s = _live.value
                if (!s.running) break
                if (!s.paused) _live.value = s.copy(elapsedMillis = activeAccumulated + android.os.SystemClock.elapsedRealtime() - activeStarted)
            }
        }
    }

    private fun stopTicker() { tickerJob?.cancel(); tickerJob = null }

    // -------- settings --------
    fun setTheme(id: String) { viewModelScope.launch { settings.setTheme(com.void200.runku.ui.theme.RunkuTheme.fromId(id)) } }
    fun setDarkMode(m: String) { viewModelScope.launch { settings.setDarkMode(m) } }
    fun setMapMode(m: String) { viewModelScope.launch { settings.setMapMode(m) } }
    fun setHrSource(s: String) { viewModelScope.launch { settings.setHrSource(s) } }

    suspend fun getActivity(id: Long) = db.activityDao().getById(id)
    suspend fun pointsFor(id: Long) = db.trackPointDao().getForActivity(id)
    suspend fun renameActivity(id: Long, name: String) {
        require(name.isNotBlank())
        db.activityDao().getById(id)?.let { db.activityDao().update(it.copy(name = name.trim().take(100))) }
    }
    fun deleteActivity(id: Long) = viewModelScope.launch { db.activityDao().delete(id); db.trackPointDao().deleteForActivity(id) }

    fun zone(bpm: Int) = HrZones.zoneFor(bpm)
}
