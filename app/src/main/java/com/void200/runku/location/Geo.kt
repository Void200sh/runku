package com.void200.runku.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Geodesic helpers (Haversine + pace/speed). */
object Geo {

    private const val R = 6_371_000.0 // earth radius, metres

    /** Distance in metres between two lat/lon points. */
    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    /** Seconds per kilometre from a distance (m) and duration (ms). 0 if not moving. */
    fun paceSecPerKm(distanceMeters: Double, durationMillis: Long): Double {
        if (distanceMeters < 1.0) return 0.0
        return (durationMillis / 1000.0) / (distanceMeters / 1000.0)
    }

    fun speedKmh(distanceMeters: Double, durationMillis: Long): Double {
        if (durationMillis <= 0) return 0.0
        return (distanceMeters / 1000.0) / (durationMillis / 3600_000.0)
    }

    /** mm:ss or h:mm:ss */
    fun formatDuration(millis: Long): String {
        val totalSec = millis / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    /** m'ss" per km */
    fun formatPace(secPerKm: Double): String {
        if (secPerKm <= 0 || secPerKm.isInfinite()) return "--'--\""
        val m = secPerKm.toInt() / 60
        val s = secPerKm.toInt() % 60
        return "%d'%02d\"".format(m, s)
    }

    fun formatDistance(meters: Double, unit: String = "km"): String {
        return if (unit == "mi") "%.2f".format(meters / 1609.344) else "%.2f".format(meters / 1000.0)
    }
}
