package com.void200.runku.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Activity::class, TrackPoint::class],
    version = 1,
    exportSchema = false,
)
abstract class RunkuDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun trackPointDao(): TrackPointDao

    companion object {
        @Volatile private var INSTANCE: RunkuDatabase? = null

        fun get(context: Context): RunkuDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RunkuDatabase::class.java,
                    "runku.db",
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
