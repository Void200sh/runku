package com.void200.runku.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.void200.runku.map.MapCatalog
import com.void200.runku.map.OfflineMapStore
import com.void200.runku.ui.components.Chip
import com.void200.runku.ui.components.Label
import com.void200.runku.ui.components.RunkuCard
import com.void200.runku.ui.theme.runku

@Composable
fun MapsScreen(nav: NavController) {
    val c = MaterialTheme.runku
    val context = LocalContext.current
    val store = remember { OfflineMapStore(context) }
    val statuses by MapCatalog.status.collectAsState()
    var installed by remember { mutableStateOf(store.available().map { it.name.lowercase() }.toSet()) }

    Scaffold(
        containerColor = c.bg,
        bottomBar = {
            com.void200.runku.ui.components.RunkuBottomBar(
                com.void200.runku.ui.components.NavTab.SETTINGS,
                onSelect = { tab -> nav.navigate(tab.route) { launchSingleTop = true } },
            )
        },
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad).padding(horizontal = 18.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Label("Peta offline")
                        Text("Unduh peta kota", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.text)
                    }
                    Chip("${installed.size} aktif", selected = installed.isNotEmpty())
                }
            }
            item {
                Text(
                    "Ketuk Unduh — peta tersimpan otomatis. Saat lari di area itu, basemap langsung dipakai. Di luar area pakai canvas.",
                    fontSize = 13.sp, color = c.muted,
                )
            }

            items(MapCatalog.regions) { region ->
                val message = statuses[region.id]
                val on = remember(message) { MapCatalog.installed(context, region) }
                RunkuCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(region.city, fontWeight = FontWeight.SemiBold, color = c.text)
                            Spacer(Modifier.height(2.dp))
                            Text(region.province, fontSize = 12.sp, color = c.muted)
                            Text(message ?: if (on) "Siap offline" else "Data jalan OSM · ukuran diketahui setelah unduh", fontSize = 12.sp, color = c.muted)
                        }
                        if (on) {
                            Chip("Tersimpan", selected = true)
                        } else {
                            Chip(if (message?.startsWith("Mengunduh") == true) "Proses…" else "Unduh", selected = true, onClick = {
                                runCatching {
                                    MapCatalog.download(context, region)
                                    Toast.makeText(context, "Mengunduh ${region.city}…", Toast.LENGTH_SHORT).show()
                                }.onFailure {
                                    Toast.makeText(context, "Gagal mulai unduh", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                }
            }

            item {
                Text("Paket tersimpan di HP:", color = c.text, fontSize = 13.sp)
                Text(store.mapsDir().absolutePath, color = c.muted, fontSize = 11.sp)
                Text("Basemap jalan sederhana, bukan peta satelit. Unduhan butuh internet sekali; sesudah siap dapat dipakai offline. © OpenStreetMap contributors, ODbL.", color = c.muted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Chip("Muat ulang", selected = true, onClick = {
                    store.refresh()
                    installed = store.available().map { it.name.lowercase() }.toSet()
                })
            }
        }
    }
}
