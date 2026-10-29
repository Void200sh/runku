package com.void200.runku.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.void200.runku.location.Geo
import com.void200.runku.map.CanvasRouteMap
import com.void200.runku.map.OfflineMapStore
import com.void200.runku.ui.RunkuViewModel
import com.void200.runku.ui.components.Chip
import com.void200.runku.ui.components.Label
import com.void200.runku.ui.components.Metric
import com.void200.runku.ui.components.NavTab
import com.void200.runku.ui.components.RunkuBottomBar
import com.void200.runku.ui.components.RunkuCard
import com.void200.runku.ui.components.StatGrid
import com.void200.runku.ui.theme.runku

@Composable
fun HomeScreen(vm: RunkuViewModel, nav: NavController) {
    val c = MaterialTheme.runku
    val activities by vm.activities.collectAsState()
    val settings by vm.settingsFlow.collectAsState()
    val mapIsTiles = settings.mapMode == "tiles"
    val context = LocalContext.current

    Scaffold(
        containerColor = c.bg,
        bottomBar = {
            RunkuBottomBar(NavTab.HOME, onSelect = { tab -> nav.navigate(tab.route) { launchSingleTop = true } })
        },
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 18.dp)
                .padding(top = 8.dp),
        ) {
            val offlineStore = androidx.compose.runtime.remember(context) { OfflineMapStore(context) }
            val packs = androidx.compose.runtime.remember(offlineStore) { offlineStore.available() }
            val pack = packs.firstOrNull()
            val cityName = pack?.name ?: "runku"

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                com.void200.runku.ui.components.BrandMark(showCredit = true, iconSize = 34)
                Chip(if (pack != null) "Peta" else "Canvas", selected = pack != null)
            }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Label(if (pack != null) "Peta aktif" else "Mode rute")
                    Text(cityName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.text)
                }
            }

            Spacer(Modifier.height(16.dp))

            // map / canvas
            RunkuCard(Modifier.fillMaxWidth().height(190.dp)) {
                Box(Modifier.fillMaxSize()) {
                    CanvasRouteMap(
                        points = emptyList(),
                        colors = c,
                        modifier = Modifier.fillMaxSize(),
                        showGrid = true,
                    )
                    Chip(
                        if (pack != null) "${pack.name} · ${pack.sizeLabel()}" else "Canvas · tanpa peta",
                        selected = pack != null,
                        modifier = Modifier.align(Alignment.TopStart),
                    )
                }
            }

            if (pack == null) {
                Spacer(Modifier.height(12.dp))
                RunkuCard(Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Peta kota belum diunduh", fontWeight = FontWeight.SemiBold, color = c.text)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Rute tetap terekam (canvas). Unduh peta biar ada basemap.",
                                fontSize = 13.sp, color = c.muted,
                            )
                        }
                        Chip("Unduh", selected = true, onClick = { nav.navigate("maps") })
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
                RunkuCard(Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${packs.size} peta offline aktif", fontWeight = FontWeight.SemiBold, color = c.text)
                            Spacer(Modifier.height(4.dp))
                            Text("Auto-deteksi via GPS saat lari.", fontSize = 13.sp, color = c.muted)
                        }
                        Chip("Kelola", selected = true, onClick = { nav.navigate("maps") })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { vm.startRun(); nav.navigate("live") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.accent, contentColor = c.accentInk),
            ) {
                Text("Mulai Lari", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            val last = activities.firstOrNull()
            StatGrid(
                items = listOf(
                    "Terakhir" to (last?.let { Geo.formatDistance(it.distanceMeters) + " km" } ?: "—"),
                    "Pace rata²" to (last?.let { Geo.formatPace(it.avgPaceSecPerKm) } ?: "—'—\""),
                ),
            )
        }
    }
}
