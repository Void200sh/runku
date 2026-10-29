package com.void200.runku.map

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class MapRegion(val id: String, val city: String, val province: String, val bounds: String)

/** Downloads small OSM road vectors, not imaginary prebuilt MBTiles URLs.
 * Never bulk downloads from the public OSM raster tile servers. */
object MapCatalog {
    val regions = listOf(
        MapRegion("magelang", "Magelang", "Jawa Tengah", "110.19,-7.51,110.25,-7.45"),
        MapRegion("semarang", "Semarang", "Jawa Tengah", "110.35,-7.03,110.47,-6.94"),
        MapRegion("yogyakarta", "Yogyakarta", "DI Yogyakarta", "110.32,-7.85,110.42,-7.74"),
        MapRegion("solo", "Surakarta", "Jawa Tengah", "110.77,-7.60,110.87,-7.53"),
        MapRegion("jakarta", "Jakarta Pusat", "DKI Jakarta", "106.78,-6.22,106.88,-6.12"),
        MapRegion("bandung", "Bandung", "Jawa Barat", "107.56,-6.96,107.67,-6.86"),
        MapRegion("surabaya", "Surabaya Pusat", "Jawa Timur", "112.70,-7.31,112.79,-7.23"),
        MapRegion("malang", "Malang", "Jawa Timur", "112.59,-8.02,112.67,-7.93"),
        MapRegion("denpasar", "Denpasar", "Bali", "115.17,-8.71,115.26,-8.62"),
    )
    val status = MutableStateFlow<Map<String, String>>(emptyMap())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = mutableMapOf<String, Job>()
    fun file(context: Context, region: MapRegion) = File(OfflineMapStore(context).mapsDir(), "${region.id}.roads.json")
    fun installed(context: Context, region: MapRegion): Boolean = runCatching {
        val json = JSONObject(file(context, region).readText())
        json.getJSONArray("elements").length() > 0 && json.has("bounds")
    }.getOrDefault(false)

    @Synchronized fun download(context: Context, region: MapRegion) {
        if (jobs[region.id]?.isActive == true) return
        val app = context.applicationContext
        status.value = status.value + (region.id to "Mengunduh jalan kota…")
        jobs[region.id] = scope.launch {
            val target = file(app, region)
            val temp = File(target.path + ".part")
            runCatching {
                val b = region.bounds.split(',')
                val query = "[out:json][timeout:45];way[highway][highway!~\"construction|proposed\"](${b[1]},${b[0]},${b[3]},${b[2]});out geom;"
                val conn = URL("https://overpass-api.de/api/interpreter").openConnection() as HttpURLConnection
                try {
                    conn.requestMethod = "POST"; conn.doOutput = true
                    conn.connectTimeout = 20000; conn.readTimeout = 65000
                    conn.setRequestProperty("User-Agent", "runku/1.0 offline-city-map")
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    conn.outputStream.use { it.write(("data=" + URLEncoder.encode(query, "UTF-8")).toByteArray()) }
                    check(conn.responseCode == 200) { "Server peta HTTP ${conn.responseCode}; coba lagi nanti" }
                    val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                    check(!json.has("remark")) { "Server peta sibuk; coba lagi nanti" }
                    check(json.getJSONArray("elements").length() > 0) { "Tidak ada data jalan" }
                    json.put("bounds", region.bounds).put("city", region.city)
                    temp.writeText(json.toString())
                    check(temp.renameTo(target)) { "Tidak bisa menyimpan paket" }
                    status.value = status.value + (region.id to "Siap offline · %.1f MB".format(target.length() / 1048576.0))
                } finally { conn.disconnect() }
            }.onFailure {
                temp.delete()
                status.value = status.value + (region.id to "Gagal: ${it.message}")
            }
        }
    }
}
