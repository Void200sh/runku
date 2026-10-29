package com.void200.runku.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class LocationSample(
    val lat: Double,
    val lon: Double,
    val elevation: Double,
    val speedMps: Float,
    val bearing: Float,
    val accuracy: Float,
    val timestamp: Long,
)

/** Wraps FusedLocationProviderClient into a cold Flow of LocationSample. */
class LocationTracker(context: Context) {

    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    @SuppressLint("MissingPermission")
    fun stream(intervalMs: Long = 1000L): Flow<LocationSample> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setMinUpdateDistanceMeters(1f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    trySend(
                        LocationSample(
                            lat = loc.latitude,
                            lon = loc.longitude,
                            elevation = loc.altitude,
                            speedMps = if (loc.hasSpeed()) loc.speed else 0f,
                            bearing = if (loc.hasBearing()) loc.bearing else 0f,
                            accuracy = if (loc.hasAccuracy()) loc.accuracy else 0f,
                            timestamp = loc.time,
                        )
                    )
                }
            }
        }

        // Use a background looper so location callbacks don't block the main thread
        // (prevents ANR / app-not-responding crashes during long runs).
        val handlerThread = android.os.HandlerThread("runku-location").apply { start() }
        client.requestLocationUpdates(request, callback, handlerThread.looper)
        awaitClose {
            client.removeLocationUpdates(callback)
            handlerThread.quitSafely()
        }
        awaitClose { client.removeLocationUpdates(callback) }
    }
}
