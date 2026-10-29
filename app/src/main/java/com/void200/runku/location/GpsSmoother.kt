package com.void200.runku.location

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * GPS stabilizer: rejects jitter, smooths the route, and keeps pace stable.
 *
 * Real-world phone GPS drifts several metres even standing still. Raw samples
 * drawn directly produce a scribbled line and wildly oscillating pace. This
 * filter:
 *   - drops fixes with poor accuracy (> MAX_ACCURACY)
 *   - drops fixes that jump further than physically plausible (> MAX_SPEED)
 *   - requires a minimum displacement before counting distance (MIN_MOVE)
 *   - smooths coordinates with an exponential moving average
 *   - smooths speed/pace over a sliding window
 */
class GpsSmoother {

    companion object {
        const val MAX_ACCURACY = 30f       // metres — ignore worse fixes
        const val MIN_MOVE = 3.0           // metres — below this = jitter
        const val MAX_SPEED_MPS = 12.0     // 43 km/h — implausible on foot
        const val ALPHA = 0.35             // EMA factor (0..1) for position
    }

    private var lastAccepted: LocationSample? = null
    private var smoothLat = 0.0
    private var smoothLon = 0.0
    private var hasSmooth = false

    private val speedWindow = ArrayDeque<Double>()

    data class Result(
        val accepted: LocationSample,   // smoothed point to draw/store
        val addedDistance: Double,      // metres to add to total (0 if jitter)
        val stableSpeedMps: Double,     // smoothed speed
    )

    fun reset() {
        lastAccepted = null
        hasSmooth = false
        speedWindow.clear()
    }

    fun process(sample: LocationSample, elapsedSec: Double): Result? {
        // 1. accuracy gate
        if (sample.accuracy > MAX_ACCURACY) return null
        if (sample.lat == 0.0 && sample.lon == 0.0) return null

        val prev = lastAccepted
        if (prev == null) {
            lastAccepted = sample
            smoothLat = sample.lat
            smoothLon = sample.lon
            hasSmooth = true
            return Result(sample, 0.0, 0.0)
        }

        val dt = ((sample.timestamp - prev.timestamp) / 1000.0).coerceAtLeast(0.5)
        val rawDist = Geo.distance(prev.lat, prev.lon, sample.lat, sample.lon)
        val impliedSpeed = rawDist / dt

        // 2. implausible jump gate (teleport / bad fix)
        if (impliedSpeed > MAX_SPEED_MPS && rawDist > 15.0) return null

        // 3. jitter gate — distance only counts after MIN_MOVE
        var added = 0.0
        if (rawDist >= MIN_MOVE) added = rawDist

        // 4. EMA smoothing of the drawn position
        smoothLat = smoothLat * (1 - ALPHA) + sample.lat * ALPHA
        smoothLon = smoothLon * (1 - ALPHA) + sample.lon * ALPHA

        val smoothed = sample.copy(lat = smoothLat, lon = smoothLon)

        // 5. speed smoothing over a ~5s window
        speedWindow.addLast(if (impliedSpeed > MAX_SPEED_MPS) 0.0 else impliedSpeed)
        if (speedWindow.size > 5) speedWindow.removeFirst()
        val avgSpeed = if (speedWindow.isEmpty()) 0.0 else speedWindow.average()

        // do not count a "move" while the window says we are stationary
        if (avgSpeed < 0.4) added = 0.0

        lastAccepted = sample
        return Result(smoothed, added, avgSpeed)
    }

    /** Pace from smoothed totals, guarded against divide-by-tiny. */
    fun stablePace(distanceMeters: Double, elapsedMillis: Long): Double {
        if (distanceMeters < 20.0) return 0.0       // too early to be meaningful
        if (elapsedMillis <= 0) return 0.0
        return (elapsedMillis / 1000.0) / (distanceMeters / 1000.0)
    }
}
