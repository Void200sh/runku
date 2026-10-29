package com.void200.runku.hr

/** Heart-rate zone model (max-HR based, simple %HRmax). */
object HrZones {
    // Default max HR if user has not set one.
    const val DEFAULT_MAX_HR = 190

    enum class Zone(val label: String, val lowPct: Double, val highPct: Double) {
        Z1("Z1", 0.50, 0.60),   // recovery / warm-up
        Z2("Z2", 0.60, 0.70),   // easy / endurance
        Z3("Z3", 0.70, 0.80),   // tempo / aerobic
        Z4("Z4", 0.80, 0.90),   // threshold
        Z5("Z5", 0.90, 1.00);   // VO2max
    }

    fun zoneFor(bpm: Int, maxHr: Int = DEFAULT_MAX_HR): Zone {
        val pct = bpm.toDouble() / maxHr
        return Zone.entries.firstOrNull { pct >= it.lowPct && pct < it.highPct } ?: Zone.Z5
    }

    val all: List<Zone> get() = Zone.entries.toList()
}
