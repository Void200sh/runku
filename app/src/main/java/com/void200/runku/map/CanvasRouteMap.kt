package com.void200.runku.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.void200.runku.location.LocationSample
import com.void200.runku.ui.theme.RunkuColors

/**
 * Canvas fallback renderer — the default when no offline map is downloaded.
 * Draws a light grid + the GPS route polyline, auto-fit to the bounds.
 * No basemap, no tiles, no storage: pure vectors.
 */
@Composable
fun CanvasRouteMap(
    points: List<LocationSample>,
    colors: RunkuColors,
    modifier: Modifier = Modifier,
    showGrid: Boolean = true,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        if (showGrid) {
            val step = size.minDimension / 8f
            val grid = colors.line.copy(alpha = 0.45f)
            var x = 0f
            while (x <= w) { drawLine(grid, Offset(x, 0f), Offset(x, h), 1f); x += step }
            var y = 0f
            while (y <= h) { drawLine(grid, Offset(0f, y), Offset(w, y), 1f); y += step }
        }

        if (points.size < 2) return@Canvas

        // bounds
        var minLat = points[0].lat; var maxLat = points[0].lat
        var minLon = points[0].lon; var maxLon = points[0].lon
        for (p in points) {
            if (p.lat < minLat) minLat = p.lat; if (p.lat > maxLat) maxLat = p.lat
            if (p.lon < minLon) minLon = p.lon; if (p.lon > maxLon) maxLon = p.lon
        }
        val spanLat = (maxLat - minLat).coerceAtLeast(1e-5)
        val spanLon = (maxLon - minLon).coerceAtLeast(1e-5)
        val pad = 0.08f

        fun project(p: LocationSample): Offset {
            val fx = ((p.lon - minLon) / spanLon).toFloat()
            val fy = ((p.lat - minLat) / spanLat).toFloat()
            // flip Y (lat grows upward), add padding
            val px = (pad + fx * (1 - 2 * pad)) * w
            val py = (1 - (pad + fy * (1 - 2 * pad))) * h
            return Offset(px, py)
        }

        val path = Path()
        val first = project(points.first())
        path.moveTo(first.x, first.y)
        for (i in 1 until points.size) {
            val o = project(points[i]); path.lineTo(o.x, o.y)
        }
        drawPath(path, color = colors.accent, style = Stroke(width = 7f, cap = StrokeCap.Round))

        // start / current dots
        drawCircle(colors.accent, radius = 11f, center = first)
        drawCircle(Color.White, radius = 4f, center = first)
        val last = project(points.last())
        drawCircle(colors.accent.copy(alpha = 0.25f), radius = 18f, center = last)
        drawCircle(colors.accent, radius = 11f, center = last)
    }
}
