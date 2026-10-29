package com.void200.runku.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.void200.runku.map.OfflineMapStore
import com.void200.runku.ui.components.Chip
import com.void200.runku.ui.components.Label
import com.void200.runku.ui.components.RunkuCard
import com.void200.runku.ui.theme.runku

/**
 * Offline map manager. Lists installed city packs, explains how to add one.
 *
 * Packs are plain `.mbtiles` files placed in
 *   Android/data/com.void200.runku/files/maps/
 * Download sources: OpenAndroMaps (Indonesia regions) or any OSM MBTiles export.
 */
@Composable
fun MapsScreen(vm: androidx.lifecycle.ViewModel? = null, nav: NavController) {
    val c = MaterialTheme.runku
    val context = LocalContext.current
    val store = remember { OfflineMapStore(context) }
    val packs = remember { mutableStateOf(store.available()) }

    Scaffold(containerColor = c.bg) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp).padding(top = 8.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Label("Peta offline")
                    Text("Peta kota", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = c.text)
                }
                Chip("${packs.value.size} aktif", selected = true)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Auto-deteksi via GPS: kalau posisi kamu masuk area peta yang tersimpan, basemap dipakai otomatis. Di luar area → canvas.",
                fontSize = 13.sp, color = c.muted,
            )

            Spacer(Modifier.height(18.dp))
            Label("Tersimpan")
            Spacer(Modifier.height(10.dp))
            if (packs.value.isEmpty()) {
                RunkuCard(Modifier.fillMaxWidth()) {
                    Text("Belum ada peta", fontWeight = FontWeight.SemiBold, color = c.text)
                    Spacer(Modifier.height(4.dp))
                    Text("Rute tetap jalan pakai canvas.", fontSize = 13.sp, color = c.muted)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    packs.value.forEach { p ->
                        RunkuCard(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(p.name, fontWeight = FontWeight.SemiBold, color = c.text)
                                    Text(p.sizeLabel(), fontSize = 12.sp, color = c.muted)
                                }
                                Chip("Siap", selected = true)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Label("Cara menambah peta")
            Spacer(Modifier.height(10.dp))
            RunkuCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Step("1", "Unduh file .mbtiles region Indonesia (OpenAndroMaps / export OSM).")
                    Step("2", "Salin ke folder:", mono = true)
                    Text(store.mapsDir().absolutePath, fontSize = 12.sp, color = c.accent, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Step("3", "Buka lagi — peta langsung terdeteksi otomatis.")
                }
            }

            Spacer(Modifier.height(18.dp))
            Chip("Refresh", selected = true, onClick = { store.refresh(); packs.value = store.available() })
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Step(n: String, text: String, mono: Boolean = false) {
    val c = MaterialTheme.runku
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(n, fontWeight = FontWeight.Bold, color = c.accent, fontSize = 13.sp)
        Text(text, fontSize = 13.sp, color = c.text)
    }
}
