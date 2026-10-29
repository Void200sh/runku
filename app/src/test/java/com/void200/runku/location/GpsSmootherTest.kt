package com.void200.runku.location

import org.junit.Assert.*
import org.junit.Test

class GpsSmootherTest {

    // ~1e-5 deg lat ≈ 1.1 m
    private fun fix(lat: Double, lon: Double = 110.2, time: Long, speed: Float = 0f, accuracy: Float = 5f) =
        LocationSample(lat, lon, 0.0, speed, 0f, accuracy, time)

    @Test fun firstFixSeedsAnchorAndEmitsOnlyZeroDistance() {
        val filter = GpsSmoother()
        val r = filter.process(fix(-7.48, time = 1000), 0.0)
        assertNotNull(r)
        assertEquals(0.0, r!!.addedDistance, 0.0001)
    }

    @Test fun stationaryJitterWithinAccuracyNeverGrowsRoute() {
        val filter = GpsSmoother()
        filter.process(fix(-7.48, time = 1000), 0.0)
        // jitter of ±2 m back and forth — well inside the 6 m movement threshold
        repeat(200) { i ->
            val drift = if (i % 2 == 0) 0.00002 else -0.00002
            val r = filter.process(fix(-7.48 + drift, time = 2000L + i * 1000), i.toDouble())
            assertNull("jitter must not be accepted at i=$i", r)
        }
    }

    @Test fun sustainedForwardMovementIsAccepted() {
        val filter = GpsSmoother()
        filter.process(fix(-7.4800, time = 1000), 0.0)
        // walk ~10 m north per fix (well above threshold), consistent direction
        val r = filter.process(fix(-7.47991, time = 4000, speed = 3.0f), 3.0)
        assertNotNull(r)
        assertTrue("expected ~10 m, got ${r!!.addedDistance}", r.addedDistance in 8.0..13.0)
    }

    @Test fun teleportAndBadAccuracyRejected() {
        val filter = GpsSmoother()
        filter.process(fix(-7.48, time = 1000), 0.0)
        // 5 km jump in 1 s → teleport
        assertNull(filter.process(fix(-7.435, time = 2000, speed = 2f), 1.0))
        // accuracy 80 m → rejected outright
        assertNull(filter.process(fix(-7.4799, time = 3000, speed = 2f, accuracy = 80f), 2.0))
    }

    @Test fun anchorAdvancesSoDistanceDoesNotDoubleCount() {
        val filter = GpsSmoother()
        filter.process(fix(-7.4800, time = 1000), 0.0)
        val a = filter.process(fix(-7.47991, time = 4000, speed = 3f), 3.0)
        val b = filter.process(fix(-7.47982, time = 7000, speed = 3f), 6.0)
        assertNotNull(a); assertNotNull(b)
        // second step measured from the advanced anchor, ~10 m again (not 20 m)
        assertTrue("second step ~10 m, got ${b!!.addedDistance}", b.addedDistance in 8.0..13.0)
    }
}
