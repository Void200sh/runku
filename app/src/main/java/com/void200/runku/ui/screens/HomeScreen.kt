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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.void200.runku.location.Geo
import com.void200.runku.map.CanvasRouteMap
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

    Scaffold(
        containerColor = c.bg,
        bottomBar = {
            RunkuBottomBar(NavTab.HOME) { tab -> nav.navigate(tab.route) { launchSingleTop = true } }
        },
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 18.dp)
                .padding(top = 8.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Label("Kota aktif")
                    Text("Magelang", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = c.text)
                }
                Chip(if (mapIsTiles) "Mode: Peta" else "Mode: Canvas", selected = true)
            }

            Spacer(Modifier.height(16.dp))

            // map / canvas
            RunkuCard(Modifier.fillMaxWidth().height(190.dp)) {
                Box(Modifier.fillMaxSize()) {
                    CanvasRouteMap(
                        points = emptyList(),
                        colors = c,
                        modifier = Modifier.fillMaxSize(),
                        showGrid = !mapIsTiles,
                    )
                    Chip(
                        if (mapIsTiles) "mbTiles · zoom 15" else "Canvas · tanpa peta",
                        modifier = Modifier.align(Alignment.TopStart),
                    )
                }
            }

            if (!mapIsTiles) {
                Spacer(Modifier.height(12.dp))
                RunkuCard(Modifier.fillMaxWidth()) {
                    Text("Peta kota belum diunduh", fontWeight = FontWeight.SemiBold, color = c.text)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Rute tetap terekam (canvas). Unduh peta biar ada basemap.",
                        fontSize = 13.sp, color = c.muted,
                    )
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
