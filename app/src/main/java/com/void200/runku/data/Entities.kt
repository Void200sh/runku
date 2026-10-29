package com.void200.runku.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "Lari",
    val type: ActivityType = ActivityType.RUN,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0,
    val distanceMeters: Double = 0.0,
    val durationMillis: Long = 0,
    val avgPaceSecPerKm: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val elevationGainM: Double = 0.0,
    val avgHr: Int = 0,
    val maxHr: Int = 0,
    val targetType: TargetType = TargetType.FREE,
    val targetValue: Double = 0.0,      // km, sec/km, or seconds depending on targetType
    val mapMode: String = "canvas",     // "canvas" | "tiles"
    val completed: Boolean = false,
)

enum class ActivityType { RUN, WALK, BIKE }

enum class TargetType { DISTANCE, PACE, DURATION, FREE }

@Entity(tableName = "track_points")
data class TrackPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityId: Long,
    val lat: Double,
    val lon: Double,
    val elevation: Double = 0.0,
    val speedMps: Float = 0f,
    val bearing: Float = 0f,
    val accuracy: Float = 0f,
    val hr: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val seq: Int = 0,
)
