package com.void200.runku.map

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.void200.runku.location.LocationSample
import com.void200.runku.ui.components.Chip
import com.void200.runku.ui.theme.runku
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Renders the GPS route on an offline basemap when a matching city pack exists,
 * otherwise on the lightweight canvas (default). Auto-selects by GPS bbox.
 */
@Composable
fun RouteMap(
    points: List<LocationSample>,
    modifier: Modifier = Modifier,
    forceCanvas: Boolean = false,
) {
    val c = MaterialTheme.runku
    val context = LocalContext.current
    val store = remember { OfflineMapStore(context) }

    // Load available packs ONCE, off the main thread. Never re-read disk on
    // every GPS sample (that caused jank while running).
    var packs by remember { mutableStateOf<List<OfflineMapStore.OfflineMap>?>(null) }
    LaunchedEffect(Unit) {
        packs = withContext(Dispatchers.IO) { store.available() }
    }

    // Select a pack for the current area. Recompute only when a coarse
    // position bucket changes, not for every single point.
    val pack = remember(packs, points.size / 15) {
        val last = points.lastOrNull()
        val all = packs ?: emptyList()
        if (last != null) {
            all.firstOrNull { it.bounds?.contains(last.lat, last.lon) == true } ?: all.firstOrNull()
        } else all.firstOrNull()
    }

    if (pack == null || forceCanvas) {
        Box(modifier.fillMaxSize()) {
            CanvasRouteMap(points, c, Modifier.fillMaxSize())
            Chip(
                "Canvas · tanpa peta",
                modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
            )
        }
    } else {
        Box(modifier.fillMaxSize()) {
            if (pack.file.name.endsWith(".roads.json")) RoadMap(pack.file, points, c, Modifier.fillMaxSize())
            else OfflineMapView(context, points, pack, Modifier.fillMaxSize())
            Chip(
                "${pack.name} · ${pack.sizeLabel()}",
                selected = true,
                modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
            )
        }
    }
}

@Composable
private fun OfflineMapView(
    context: Context,
    points: List<LocationSample>,
    pack: OfflineMapStore.OfflineMap,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = {
            Configuration.getInstance().userAgentValue = it.packageName
            Configuration.getInstance().osmdroidBasePath = it.cacheDir
            Configuration.getInstance().osmdroidTileCache = java.io.File(it.cacheDir, "tiles").apply { mkdirs() }
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                setUseDataConnection(false) // OFFLINE ONLY
                controller.setZoom(15.0)
                OfflineTiles.attach(this, pack.file)
            }
        },
        update = { map ->
            map.overlays.removeAll { it is Polyline || it is Marker }
            if (points.size >= 2) {
                val geo = points.map { GeoPoint(it.lat, it.lon) }
                map.overlays.add(Polyline().apply {
                    setPoints(geo)
                    outlinePaint.strokeWidth = 9f
                })
                map.overlays.add(Marker(map).apply { position = geo.first(); title = "Mulai" })
                map.overlays.add(Marker(map).apply { position = geo.last(); title = "Sekarang" })
                runCatching { map.zoomToBoundingBox(BoundingBox.fromGeoPoints(geo), true) }
            }
            map.invalidate()
        },
    )
}
