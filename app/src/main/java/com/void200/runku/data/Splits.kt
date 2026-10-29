package com.void200.runku.data

import com.void200.runku.location.Geo

/** One per-kilometre split. */
data class Split(val km: Int, val secPerKm: Double, val isPartial: Boolean)

object Splits {

    /** Compute per-km splits from ordered track points (uses cumulative haversine). */
    fun from(points: List<TrackPoint>): List<Split> {
        if (points.size < 2) return emptyList()
        val out = mutableListOf<Split>()
        var progressed = 0.0
        var nextMark = 1000.0
        var kmIndex = 1
        var lastMarkTime = points.first().timestamp

        for (i in 1 until points.size) {
            val a = points[i - 1]; val b = points[i]
            val d = Geo.distance(a.lat, a.lon, b.lat, b.lon)
            if (d <= 0 || d > 500) continue // jitter / teleport
            val prevProgressed = progressed
            progressed += d
            while (progressed >= nextMark) {
                // interpolate the crossing time
                val frac = (nextMark - prevProgressed) / d
                val crossTime = a.timestamp + ((b.timestamp - a.timestamp) * frac).toLong()
                val sec = (crossTime - lastMarkTime) / 1000.0
                out.add(Split(kmIndex, sec.coerceAtLeast(1.0), isPartial = false))
                lastMarkTime = crossTime
                nextMark += 1000.0
                kmIndex++
            }
        }
        // partial trailing km
        val remainder = progressed - (nextMark - 1000.0)
        if (remainder > 50) {
            val sec = (points.last().timestamp - lastMarkTime) / 1000.0
            out.add(Split(kmIndex, sec.coerceAtLeast(1.0), isPartial = true))
        }
        return out
    }
}

/** Simple achievement badges derived from an activity history. */
object Badges {
    data class Badge(val icon: String, val label: String)

    fun forActivity(activity: Activity, allActivities: List<Activity>): List<Badge> {
        val out = mutableListOf<Badge>()
        val km = activity.distanceMeters / 1000.0
        if (km >= 5) out.add(Badge("🏅", "5K"))
        if (km >= 10) out.add(Badge("🥇", "10K"))
        if (km >= 21.0975) out.add(Badge("🏆", "Half"))
        if (km >= 42.195) out.add(Badge("👑", "Full"))

        // streak: consecutive days with at least one activity ending today/yesterday
        val days = allActivities.map { dayKey(it.startTime) }.toSet()
        var streak = 0
        var cursor = dayKey(activity.startTime)
        while (days.contains(cursor)) {
            streak++
            cursor -= 86_400_000L
        }
        if (streak >= 3) out.add(Badge("🔥", "$streak hari"))

        // fastest: best avg pace among all runs
        val best = allActivities.filter { it.distanceMeters > 1000 && it.avgPaceSecPerKm > 0 }
            .minByOrNull { it.avgPaceSecPerKm }
        if (best != null && best.id == activity.id) out.add(Badge("⚡", "Tercepat"))

        return out
    }

    private fun dayKey(ts: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = ts
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
