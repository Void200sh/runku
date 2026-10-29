package com.void200.runku.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY startTime DESC")
    fun observeAll(): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getById(id: Long): Activity?

    @Query("SELECT * FROM activities WHERE id = :id")
    fun observeById(id: Long): Flow<Activity?>

    @Insert
    suspend fun insert(activity: Activity): Long

    @Update
    suspend fun update(activity: Activity)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COALESCE(SUM(distanceMeters),0) FROM activities WHERE startTime >= :since")
    suspend fun distanceSince(since: Long): Double

    @Query("SELECT COUNT(*) FROM activities WHERE startTime >= :since")
    suspend fun countSince(since: Long): Int
}

@Dao
interface TrackPointDao {
    @Query("SELECT * FROM track_points WHERE activityId = :id ORDER BY seq ASC")
    fun observeForActivity(id: Long): Flow<List<TrackPoint>>

    @Query("SELECT * FROM track_points WHERE activityId = :id ORDER BY seq ASC")
    suspend fun getForActivity(id: Long): List<TrackPoint>

    @Insert
    suspend fun insert(point: TrackPoint): Long

    @Insert
    suspend fun insertAll(points: List<TrackPoint>)

    @Query("DELETE FROM track_points WHERE activityId = :id")
    suspend fun deleteForActivity(id: Long)
}
