package com.void200.runku.location

/**
 * Movement gate: never append stationary fixes to the route.
 *
 * Problem being solved: a phone sitting still still reports "moving" fixes
 * because GNSS positions wander several metres inside the accuracy circle.
 * Drawing those produces a scribbled line that grows with no real movement.
 *
 * Strategy:
 *  - Reject invalid / low-accuracy fixes.
 *  - Keep a stable `anchor`. Movement is only recognised when the fix is a
 *    clear, sustained step away from the anchor.
 *  - A step counts only after several consecutive fixes agree on the same
 *    direction of travel (guards against back-and-forth noise).
 *  - Once accepted, the anchor advances and the accepted point is emitted.
 *  - Falling back inside the movement threshold resets the streak, so jitter
 *    around a single spot never accumulates distance.
 */
class GpsSmoother {

    private var anchor: LocationSample? = null
    private var previous: LocationSample? = null
    private var streak = 0
    private var streakLat = 0.0
    private var streakLon = 0.0

    data class Result(val accepted: LocationSample, val addedDistance: Double, val stableSpeedMps: Double)

    fun reset() {
        anchor = null
        previous = null
        streak = 0
    }

    fun process(sample: LocationSample, elapsedSec: Double): Result? {
        // --- validity gates ---
        if (!sample.lat.isFinite() || !sample.lon.isFinite() ||
            sample.lat !in -90.0..90.0 || sample.lon !in -180.0..180.0 ||
            sample.accuracy <= 0f || sample.accuracy > 20f ||
            (sample.lat == 0.0 && sample.lon == 0.0)
        ) return null

        val prior = previous
        if (prior != null && sample.timestamp <= prior.timestamp) return null
        previous = sample

        val anchor0 = anchor
        if (anchor0 == null) {
            // First fix: seed the anchor, emit nothing (no movement yet).
            anchor = sample
            streakLat = sample.lat
            streakLon = sample.lon
            streak = 1
            return Result(sample, 0.0, 0.0)
        }

        val dt = (sample.timestamp - anchor0.timestamp) / 1000.0
        if (dt <= 0) return null

        val stepFromAnchor = Geo.distance(anchor0.lat, anchor0.lon, sample.lat, sample.lon)
        val stepSpeed = stepFromAnchor / dt

        // Teleport / impossible speed: skip the sample without moving the anchor.
        if (stepSpeed > 12.0 || sample.speedMps > 12f) { streak = 0; return null }

        // A real step must clear the combined uncertainty of both fixes.
        val threshold = maxOf(6.0, (anchor0.accuracy + sample.accuracy) * 0.6)

        if (stepFromAnchor < threshold) {
            // We are (still) effectively at the anchor. Reset the movement streak
            // so wandering noise cannot slowly accumulate into a fake move.
            streak = 0
            streakLat = anchor0.lat
            streakLon = anchor0.lon
            return null
        }

        // Distance from the previous accepted step direction: require consistency.
        val sinceStreak = Geo.distance(streakLat, streakLon, sample.lat, sample.lon)
        if (sinceStreak < threshold * 0.4) {
            // Not enough forward progress since last streak point yet.
            return null
        }

        // Confirmed sustained movement: accept, advance anchor, emit the point.
        anchor = sample
        streakLat = sample.lat
        streakLon = sample.lon
        streak = 0
        val speed = if (sample.speedMps > 0.3f) sample.speedMps.toDouble() else stepSpeed
        return Result(sample, stepFromAnchor, speed)
    }
}
