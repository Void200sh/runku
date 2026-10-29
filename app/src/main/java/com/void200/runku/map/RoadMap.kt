package com.void200.runku.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.void200.runku.location.LocationSample
import com.void200.runku.ui.theme.RunkuColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.cos

data class RoadData(val bounds: List<Double>, val lines: List<RoadLine>)

data class RoadLine(val points: List<Pair<Double, Double>>, val highway: String, val name: String?)

@Composable fun RoadMap(file: File, points: List<LocationSample>, colors: RunkuColors, modifier: Modifier = Modifier) {
    val data by produceState<RoadData?>(null, file.path) {
        value = withContext(Dispatchers.IO) { runCatching {
            val json = JSONObject(file.readText())
            val ways = json.getJSONArray("elements")
            RoadData(json.getString("bounds").split(',').map { it.toDouble() }, buildList {
                for (i in 0 until ways.length()) {
                    val w = ways.getJSONObject(i)
                    val geometry = w.optJSONArray("geometry") ?: continue
                    val tags = w.optJSONObject("tags")
                    val hw = tags?.optString("highway", "road") ?: "road"
                    val name = tags?.optString("name")
                    add(RoadLine(buildList { for (j in 0 until geometry.length()) {
                        val p = geometry.getJSONObject(j); add(p.getDouble("lat") to p.getDouble("lon"))
                    } }, hw, if (name.isNullOrBlank()) null else name))
                }
            })
        }.getOrNull() }
    }
    Canvas(modifier.fillMaxSize()) {
        val d = data ?: run {
            // Draw loading placeholder
            drawRect(colors.surface2)
            return@Canvas
        }
        val b = d.bounds
        val lonScale = cos(Math.toRadians((b[1] + b[3]) / 2))
        val scale = minOf(size.width / ((b[2]-b[0]) * lonScale), size.height / (b[3]-b[1])) * 0.94
        fun xy(lat: Double, lon: Double) = Offset(
            (size.width/2 + (lon-(b[0]+b[2])/2)*lonScale*scale).toFloat(),
            (size.height/2 - (lat-(b[1]+b[3])/2)*scale).toFloat())

        // Background
        drawRect(colors.surface2)

        // Draw roads by type: major first (bottom), minor on top
        val majorTypes = setOf("motorway", "trunk", "primary", "secondary", "tertiary")
        val minorTypes = setOf("residential", "unclassified", "service", "track", "path", "footway", "cycleway")

        // Minor roads (thin, light)
        d.lines.filter { it.highway in minorTypes || it.highway !in majorTypes }.forEach { road ->
            if (road.points.size < 2) return@forEach
            val path = Path()
            road.points.forEachIndexed { i, p -> val o = xy(p.first, p.second); if (i == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y) }
            drawPath(path, colors.muted.copy(alpha = 0.35f), style = Stroke(1.5f))
        }

        // Major roads (thicker, darker)
        d.lines.filter { it.highway in majorTypes }.forEach { road ->
            if (road.points.size < 2) return@forEach
            val (width, color): Pair<Float, Color> = when (road.highway) {
                "motorway", "trunk" -> 4f to colors.text.copy(alpha = 0.6f)
                "primary" -> 3.5f to colors.text.copy(alpha = 0.5f)
                "secondary" -> 3f to colors.text.copy(alpha = 0.45f)
                else -> 2.5f to colors.text.copy(alpha = 0.4f)
            }
            val path = Path()
            road.points.forEachIndexed { i, p -> val o = xy(p.first, p.second); if (i == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y) }
            drawPath(path, color, style = Stroke(width))
        }

        // GPS route on top (accent color, thick)
        if (points.isNotEmpty()) {
            val path = Path()
            points.forEachIndexed { i, p -> val o = xy(p.lat, p.lon); if (i == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y) }
            drawPath(path, colors.accent, style = Stroke(6f, cap = Stroke.Cap.Round))
            val p = points.last()
            drawCircle(colors.accent.copy(alpha = 0.25f), 18f, xy(p.lat, p.lon))
            drawCircle(colors.accent, 8f, xy(p.lat, p.lon))
            if (points.size > 1) {
                val first = points.first()
                drawCircle(colors.accent, 6f, xy(first.lat, first.lon))
            }
        }
    }
}
