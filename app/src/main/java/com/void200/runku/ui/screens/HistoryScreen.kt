package com.void200.runku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.void200.runku.ui.RunkuViewModel
import com.void200.runku.ui.components.Chip
import com.void200.runku.ui.components.Label
import com.void200.runku.ui.components.NavTab
import com.void200.runku.ui.components.RunkuBottomBar
import com.void200.runku.ui.components.RunkuCard
import com.void200.runku.ui.theme.runku
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(vm: RunkuViewModel, nav: NavController) {
    val c = MaterialTheme.runku
    val activities by vm.activities.collectAsState()

    Scaffold(
        containerColor = c.bg,
        bottomBar = {
            RunkuBottomBar(NavTab.HISTORY, onSelect = { tab -> nav.navigate(tab.route) { launchSingleTop = true } })
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(horizontal = 18.dp).padding(top = 8.dp),
        ) {
            Label("Riwayat")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Aktivitas", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = c.text)
                Chip("${activities.size} lari", selected = true)
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("Semua", selected = true)
                Chip("Lari")
                Chip("Jalan")
                Chip("Sepeda")
            }

            Spacer(Modifier.height(14.dp))
            if (activities.isEmpty()) {
                RunkuCard(Modifier.fillMaxWidth()) {
                    Text("Belum ada riwayat", fontWeight = FontWeight.SemiBold, color = c.text)
                    Spacer(Modifier.height(4.dp))
                    Text("Selesaikan satu lari — hasilnya muncul di sini.", fontSize = 13.sp, color = c.muted)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(activities) { a ->
                        RunkuCard(Modifier.fillMaxWidth().clickable { nav.navigate("detail/${a.id}") }) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Box(
                                    Modifier.size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(c.accentSoft),
                                    contentAlignment = Alignment.Center,
                                ) { Text("RUN", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = c.accent) }
                                Column(Modifier.weight(1f)) {
                                    Text(a.name, fontWeight = FontWeight.SemiBold, color = c.text)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "${SimpleDateFormat("d MMM", Locale("id")).format(Date(a.startTime))} · " +
                                            "${Geo.formatDistance(a.distanceMeters)} km · ${Geo.formatPace(a.avgPaceSecPerKm)}",
                                        fontSize = 12.sp, color = c.muted,
                                    )
                                }
                                Text(Geo.formatDuration(a.durationMillis), fontSize = 13.sp, color = c.muted)
                            }
                        }
                    }
                }
            }
        }
    }
}
