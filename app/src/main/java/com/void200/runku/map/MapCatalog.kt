package com.void200.runku.map

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

/**
 * Built-in catalogue of ready-made offline city packs (Indonesia).
 *
 * These are direct .mbtiles downloads — the user taps "Unduh" and the file
 * lands in the app's maps folder automatically, no manual copying.
 *
 * Sources: OpenAndroMaps / OSM exports. URLs are placeholders wired to the
 * app's maps dir; swap in real per-city .mbtiles URLs when publishing.
 */
data class MapRegion(
    val id: String,
    val city: String,
    val province: String,
    val sizeLabel: String,
    val url: String,
    val bounds: String,           // west,south,east,north
    val zoom: String = "0-15",
)

object MapCatalog {

    val regions: List<MapRegion> = listOf(
        MapRegion("magelang", "Magelang", "Jawa Tengah", "~45 MB",
            "https://download.openandromaps.org/indonesia/magelang.mbtiles", "110.10,-7.55,110.40,-7.40"),
        MapRegion("semarang", "Semarang", "Jawa Tengah", "~90 MB",
            "https://download.openandromaps.org/indonesia/semarang.mbtiles", "110.25,-7.15,110.55,-6.93"),
        MapRegion("yogyakarta", "Yogyakarta", "DI Yogyakarta", "~85 MB",
            "https://download.openandromaps.org/indonesia/yogyakarta.mbtiles", "110.25,-7.90,110.50,-7.70"),
        MapRegion("solo", "Surakarta", "Jawa Tengah", "~70 MB",
            "https://download.openandromaps.org/indonesia/surakarta.mbtiles", "110.75,-7.65,110.92,-7.48"),
        MapRegion("jakarta", "Jakarta", "DKI Jakarta", "~150 MB",
            "https://download.openandromaps.org/indonesia/jakarta.mbtiles", "106.60,-6.40,107.00,-6.05"),
        MapRegion("bandung", "Bandung", "Jawa Barat", "~110 MB",
            "https://download.openandromaps.org/indonesia/bandung.mbtiles", "107.45,-7.05,107.75,-6.80"),
        MapRegion("surabaya", "Surabaya", "Jawa Timur", "~130 MB",
            "https://download.openandromaps.org/indonesia/surabaya.mbtiles", "112.55,-7.35,112.85,-7.15"),
        MapRegion("malang", "Malang", "Jawa Timur", "~80 MB",
            "https://download.openandromaps.org/indonesia/malong.mbtiles", "112.55,-8.05,112.70,-7.90"),
        MapRegion("denpasar", "Denpasar", "Bali", "~95 MB",
            "https://download.openandromaps.org/indonesia/denpasar.mbtiles", "115.10,-8.75,115.30,-8.55"),
    )

    /** Enqueue a download into the app's private maps folder. Returns the download id. */
    fun download(context: Context, region: MapRegion): Long {
        val dir = OfflineMapStore(context).mapsDir()
        val request = DownloadManager.Request(Uri.parse(region.url))
            .setTitle("Peta ${region.city}")
            .setDescription("runku · ${region.sizeLabel}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, null, "maps/${region.id}.mbtiles")
            .setAllowedOverMetered(true)
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return dm.enqueue(request)
    }
}
