package com.void200.runku.map

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.void200.runku.location.LocationSample
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * osmdroid basemap — used only when an offline MBTiles pack is available
 * (or online tiles if connectivity exists). Falls back to CanvasRouteMap otherwise.
 */
@Composable
fun OsmdroidRouteMap(
    context: Context,
    points: List<LocationSample>,
    modifier: Modifier = Modifier,
    offlinePath: String? = null,
) {
    AndroidView(
        modifier = modifier,
        factory = {
            Configuration.getInstance().userAgentValue = it.packageName
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                offlinePath?.let { p ->
                    // Point the tile cache at a local MBTiles file/archive when provided.
                    // (Set via TileProviderFactory in a full implementation.)
                }
            }
        },
        update = { map ->
            map.overlays.clear()
            if (points.size >= 2) {
                val geo = points.map { GeoPoint(it.lat, it.lon) }
                val line = Polyline().apply {
                    setPoints(geo)
                    outlinePaint.strokeWidth = 8f
                }
                map.overlays.add(line)

                val start = Marker(map).apply { position = geo.first(); title = "Start" }
                val end = Marker(map).apply { position = geo.last(); title = "Finish" }
                map.overlays.addAll(listOf(start, end))

                val box = BoundingBox.fromGeoPoints(geo)
                map.post { map.zoomToBoundingBox(box, true) }
            }
            map.invalidate()
        },
    )
}
