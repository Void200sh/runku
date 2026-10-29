package com.void200.runku.map

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Offline MBTiles packs — per-city, fully offline.
 *
 * Each `.mbtiles` file is an SQLite DB with a `metadata` table containing
 * `bounds` (west,south,east,north). We read that locally and use it to
 * auto-select the pack that covers the current GPS position.
 */
class OfflineMapStore(private val context: Context) {

    data class OfflineMap(
        val name: String,       // pretty name from file, e.g. "Magelang"
        val file: File,
        val sizeBytes: Long,
        val bounds: Bounds?,    // from MBTiles metadata, if present
    ) {
        fun sizeLabel(): String {
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024) "%.1f GB".format(mb / 1024) else "%.0f MB".format(mb)
        }
    }

    /** west, south, east, north — degrees. */
    data class Bounds(val west: Double, val south: Double, val east: Double, val north: Double) {
        fun contains(lat: Double, lon: Double): Boolean =
            lat in south..north && lon in west..east
    }

    fun mapsDir(): File =
        File(context.getExternalFilesDir(null) ?: context.filesDir, "maps").apply { mkdirs() }

    @Volatile private var cache: List<OfflineMap>? = null

    fun available(): List<OfflineMap> {
        cache?.let { return it }
        val dir = mapsDir()
        val list = dir.listFiles { f -> f.extension.lowercase() in setOf("mbtiles", "sqlite", "db") }
            ?.map { f ->
                OfflineMap(
                    name = prettyName(f.nameWithoutExtension),
                    file = f,
                    sizeBytes = f.length(),
                    bounds = readBounds(f),
                )
            }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
        cache = list
        return list
    }

    fun refresh() { cache = null }

    fun hasAny(): Boolean = available().isNotEmpty()

    /** Auto-select the offline pack covering this GPS position. Null → use canvas. */
    fun packFor(lat: Double, lon: Double): OfflineMap? =
        available().firstOrNull { it.bounds?.contains(lat, lon) == true }

    private fun prettyName(raw: String): String =
        raw.replace('_', ' ').replace('-', ' ')
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

    /** Read `bounds` from the MBTiles metadata table (pure local SQLite). */
    private fun readBounds(file: File): Bounds? = runCatching {
        SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            db.rawQuery("SELECT value FROM metadata WHERE name='bounds'", null).use { cur ->
                if (cur.moveToFirst()) {
                    val parts = cur.getString(0).split(',').map { it.trim().toDoubleOrNull() }
                    if (parts.size == 4 && parts.all { it != null }) {
                        Bounds(parts[0]!!, parts[1]!!, parts[2]!!, parts[3]!!)
                    } else null
                } else null
            }
        }
    }.getOrNull()
}
