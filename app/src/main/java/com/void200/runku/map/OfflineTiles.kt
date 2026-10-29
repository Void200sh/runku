package com.void200.runku.map

import org.osmdroid.tileprovider.MapTileProviderArray
import org.osmdroid.tileprovider.modules.MBTilesFileArchive
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.views.MapView
import java.io.File

/**
 * Attach a local MBTiles archive to an osmdroid MapView so tiles come from the
 * file on disk. Network access is disabled separately on the MapView.
 */
object OfflineTiles {

    fun attach(map: MapView, mbtiles: File) {
        runCatching {
            val ctx = map.context
            val source = map.tileProvider.tileSource
            val archive = MBTilesFileArchive.getDatabaseFileArchive(mbtiles)

            val archiveProvider = MapTileFileArchiveProvider(
                SimpleRegisterReceiver(ctx), source, arrayOf(archive),
            )
            val fsProvider = MapTileFilesystemProvider(
                SimpleRegisterReceiver(ctx), source,
            )

            val provider = MapTileProviderArray(
                source,
                SimpleRegisterReceiver(ctx),
                arrayOf(archiveProvider, fsProvider),
            )

            map.setTileProvider(provider)
        }
    }
}
