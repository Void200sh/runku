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
    val targetType: TargetType = TargetType.FREE,
    val targetValue: Double = 0.0,
) {
    val paceSecPerKm: Double get() = Geo.paceSecPerKm(distanceMeters, elapsedMillis)
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
        hrSamples = 0; hrSum = 0L
        _live.value = LiveState(
            running = true, startedAt = System.currentTimeMillis(),
            targetType = targetType, targetValue = targetValue,
        )
        startTicker()
    }

    fun onSample(sample: LocationSample) {
        val s = _live.value
        if (!s.running || s.paused) return
        var added = 0.0
        var gain = 0.0
        last?.let { prev ->
            added = Geo.distance(prev.lat, prev.lon, sample.lat, sample.lon)
            if (added < 0.3 || added > 200) added = 0.0 // jitter / teleport guard
            if (sample.elevation > prev.elevation) gain = sample.elevation - prev.elevation
        }
        last = sample
        _live.value = s.copy(
            distanceMeters = s.distanceMeters + added,
            elevationGain = s.elevationGain + gain,
            points = s.points + sample,
        )
    }

    private var hrSamples = 0
    private var hrSum = 0L

    fun onHeartRate(bpm: Int) {
        if (bpm <= 0) return
        hrSamples++; hrSum += bpm
        val s = _live.value
        val count = hrSamples
        val newAvg = if (count > 1) ((s.avgBpm.toLong() * (count - 1) + bpm) / count).toInt() else bpm
        _live.value = s.copy(
            currentBpm = bpm,
            avgBpm = newAvg,
            maxBpm = maxOf(s.maxBpm, bpm),
        )
    }

    fun pause() { _live.value = _live.value.copy(paused = true) }
    fun resume() { _live.value = _live.value.copy(paused = false) }

    fun stopRun(onSaved: (Long) -> Unit = {}) {
        val s = _live.value
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
                    timestamp = p.timestamp, seq = i,
                )
            }
            db.trackPointDao().insertAll(pts)
            _live.value = LiveState()
            onSaved(id)
        }
    }

    fun cancelRun() { stopTicker(); _live.value = LiveState() }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val s = _live.value
                if (!s.running) break
                if (!s.paused) _live.value = s.copy(elapsedMillis = System.currentTimeMillis() - s.startedAt)
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
    fun deleteActivity(id: Long) = viewModelScope.launch { db.activityDao().delete(id); db.trackPointDao().deleteForActivity(id) }

    fun zone(bpm: Int) = HrZones.zoneFor(bpm)
}
