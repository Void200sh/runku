package com.void200.runku.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.void200.runku.data.Activity
import com.void200.runku.data.TrackPoint
import java.io.File

/** Exports an activity to GPX / JSON / CSV, written to cache/exports and shareable. */
object Exporter {

    fun gpx(activity: Activity, points: List<TrackPoint>): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine("""<gpx version="1.1" creator="runku" xmlns="http://www.topografix.com/GPX/1/1">""")
        appendLine("  <trk><name>${escape(activity.name)}</name><trkseg>")
        for (p in points) {
            appendLine("""    <trkpt lat="${p.lat}" lon="${p.lon}"><ele>${p.elevation}</ele><time>${iso(p.timestamp)}</time></trkpt>""")
        }
        appendLine("  </trkseg></trk>")
        appendLine("</gpx>")
    }

    fun json(activity: Activity, points: List<TrackPoint>): String = buildString {
        appendLine("{")
        appendLine("""  "name": "${escape(activity.name)}",""")
        appendLine("""  "type": "${activity.type}",""")
        appendLine("""  "startTime": ${activity.startTime},""")
        appendLine("""  "distanceMeters": ${activity.distanceMeters},""")
        appendLine("""  "durationMillis": ${activity.durationMillis},""")
        appendLine("""  "avgPaceSecPerKm": ${activity.avgPaceSecPerKm},""")
        appendLine("""  "avgHr": ${activity.avgHr},""")
        appendLine("""  "maxHr": ${activity.maxHr},""")
        appendLine("""  "points": [""")
        points.forEachIndexed { i, p ->
            val comma = if (i < points.size - 1) "," else ""
            appendLine("""    {"lat":${p.lat},"lon":${p.lon},"ele":${p.elevation},"hr":${p.hr},"t":${p.timestamp}}$comma""")
        }
        appendLine("  ]")
        appendLine("}")
    }

    fun csv(activity: Activity, points: List<TrackPoint>): String = buildString {
        appendLine("seq,timestamp,lat,lon,elevation,speed_mps,hr")
        for (p in points) {
            appendLine("${p.seq},${p.timestamp},${p.lat},${p.lon},${p.elevation},${p.speedMps},${p.hr}")
        }
    }

    fun writeAndShare(context: Context, filename: String, content: String) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, filename).apply { writeText(content) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Ekspor $filename"))
    }

    private fun escape(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    private fun iso(epochMs: Long): String =
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
            .format(java.util.Date(epochMs))
}
