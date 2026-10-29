package com.void200.runku.map

import org.osmdroid.tileprovider.MapTileProviderArray
import org.osmdroid.tileprovider.modules.MBTilesFileArchive
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider
import org.osmdroid.tileprovider.modules.TileWriter
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.views.MapView
import java.io.File

/**
 * Attach a local MBTiles archive to an osmdroid MapView so tiles come from the
 * file on disk. Network access is disabled on the MapView separately.
 */
object OfflineTiles {

    fun attach(map: MapView, mbtiles: File) {
        runCatching {
            val ctx = map.context
            val source = map.tileProvider.tileSource
            val archive = MBTilesFileArchive.getDatabaseFileArchive(mbtiles)

            val arrayProvider = MapTileProviderArray(source, SimpleRegisterReceiver(ctx), null)
            arrayProvider.registerTileProvider(
                MapTileFileArchiveProvider(SimpleRegisterReceiver(ctx), source, listOf(archive))
            )
            arrayProvider.registerTileProvider(
                MapTileFilesystemProvider(SimpleRegisterReceiver(ctx), source)
            )
            arrayProvider.tileCache = TileWriter()

            map.overlayManager.tilesOverlay.tileProvider = arrayProvider
        }
    }
}
