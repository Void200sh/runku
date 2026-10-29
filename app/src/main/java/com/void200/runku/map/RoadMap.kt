package com.void200.runku.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.void200.runku.location.LocationSample
import com.void200.runku.ui.theme.RunkuColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import kotlin.math.cos

data class RoadData(val bounds: List<Double>, val lines: List<List<Pair<Double, Double>>>)

@Composable fun RoadMap(file: File, points: List<LocationSample>, colors: RunkuColors, modifier: Modifier = Modifier) {
    val data by produceState<RoadData?>(null, file.path) {
        value = withContext(Dispatchers.IO) { runCatching {
            val json = JSONObject(file.readText())
            val ways = json.getJSONArray("elements")
            RoadData(json.getString("bounds").split(',').map { it.toDouble() }, buildList {
                for (i in 0 until ways.length()) {
                    val geometry = ways.getJSONObject(i).optJSONArray("geometry") ?: continue
                    add(buildList { for (j in 0 until geometry.length()) {
                        val p = geometry.getJSONObject(j); add(p.getDouble("lat") to p.getDouble("lon"))
                    } })
                }
            })
        }.getOrNull() }
    }
    Canvas(modifier.fillMaxSize()) {
        val d = data ?: return@Canvas
        val b = d.bounds
        val lonScale = cos(Math.toRadians((b[1] + b[3]) / 2))
        val scale = minOf(size.width / ((b[2]-b[0]) * lonScale), size.height / (b[3]-b[1])) * 0.94
        fun xy(lat: Double, lon: Double) = Offset(
            (size.width/2 + (lon-(b[0]+b[2])/2)*lonScale*scale).toFloat(),
            (size.height/2 - (lat-(b[1]+b[3])/2)*scale).toFloat())
        d.lines.forEach { line ->
            val path = Path()
            line.forEachIndexed { i, p -> val o=xy(p.first,p.second); if(i==0) path.moveTo(o.x,o.y) else path.lineTo(o.x,o.y) }
            drawPath(path, colors.muted.copy(alpha=0.45f), style=Stroke(2f))
        }
        if(points.isNotEmpty()) {
            val path=Path()
            points.forEachIndexed { i,p -> val o=xy(p.lat,p.lon); if(i==0) path.moveTo(o.x,o.y) else path.lineTo(o.x,o.y) }
            drawPath(path, colors.accent, style=Stroke(6f))
            val p=points.last(); drawCircle(colors.accent,7f,xy(p.lat,p.lon))
        }
    }
}
