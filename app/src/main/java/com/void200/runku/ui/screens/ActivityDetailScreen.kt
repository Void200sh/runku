package com.void200.runku.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
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
import com.void200.runku.data.Activity
import com.void200.runku.data.TrackPoint
import com.void200.runku.export.Exporter
import com.void200.runku.location.Geo
import com.void200.runku.location.LocationSample
import com.void200.runku.map.CanvasRouteMap
import com.void200.runku.ui.RunkuViewModel
import com.void200.runku.ui.components.Chip
import com.void200.runku.ui.components.HeartRateCard
import com.void200.runku.ui.components.Label
import com.void200.runku.ui.components.Metric
import com.void200.runku.ui.components.RunkuCard
import com.void200.runku.ui.components.StatGrid
import com.void200.runku.ui.theme.runku

@Composable
fun ActivityDetailScreen(vm: RunkuViewModel, id: Long, nav: NavController) {
    val c = MaterialTheme.runku
    val context = LocalContext.current
    var activity by remember { mutableStateOf<Activity?>(null) }
    var points by remember { mutableStateOf<List<TrackPoint>>(emptyList()) }
    val allActivities by vm.activities.collectAsState()

    LaunchedEffect(id) {
        activity = vm.getActivity(id)
        points = vm.pointsFor(id)
    }

    val act = activity
    if (act == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Memuat…", color = c.muted) }
        return
    }

    val samples = points.map {
        LocationSample(it.lat, it.lon, it.elevation, it.speedMps, it.bearing, it.accuracy, it.timestamp)
    }

    Scaffold(
        containerColor = c.bg,
        bottomBar = {
            DetailTabs(nav)
        },
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 8.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Label("Selesai · ${java.text.SimpleDateFormat("d MMM", java.util.Locale("id")).format(java.util.Date(act.startTime))}")
                    Text(act.name, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = c.text)
                }
                Chip("Baru", selected = true)
            }

            Spacer(Modifier.height(14.dp))

            RunkuCard(Modifier.fillMaxWidth()) {
                Column {
                    Box(Modifier.fillMaxWidth().height(200.dp)) {
                        CanvasRouteMap(samples, c, Modifier.fillMaxSize())
                        Chip("runku", modifier = Modifier.align(Alignment.BottomEnd))
                    }
                    Spacer(Modifier.height(14.dp))
                    Metric(Geo.formatDistance(act.distanceMeters), "km", size = 42, valueColor = c.accent)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card3("Pace", Geo.formatPace(act.avgPaceSecPerKm))
                        Card3("Waktu", Geo.formatDuration(act.durationMillis))
                        Card3("Elevasi", "+${act.elevationGainM.toInt()}m")
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        Exporter.writeAndShare(context, "runku-${act.id}.gpx", Exporter.gpx(act, points))
                    },
                    Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.accent, contentColor = c.accentInk),
                ) { Text("Bagikan", fontWeight = FontWeight.Bold) }
                Button(
                    onClick = {
                        val bmp = com.void200.runku.export.ShareCard.render(act, points)
                        val ok = com.void200.runku.export.ShareCard.saveToGallery(context, bmp, "runku-${act.id}")
                        android.widget.Toast.makeText(
                            context,
                            if (ok) "JPG tersimpan di Galeri (Pictures/runku)" else "Gagal simpan JPG",
                            android.widget.Toast.LENGTH_LONG,
                        ).show()
                    },
                    Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.surface2, contentColor = c.accent),
                ) { Text("Simpan Gambar", fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(20.dp))
            Label("Detak jantung")
            Spacer(Modifier.height(10.dp))
            RunkuCard(Modifier.fillMaxWidth()) {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("RATA²", fontSize = 11.sp, letterSpacing = 1.4.sp, color = c.muted)
                            Text(if (act.avgHr > 0) "${act.avgHr}" else "--", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = c.text)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("MAKS", fontSize = 11.sp, letterSpacing = 1.4.sp, color = c.muted)
                            Text(if (act.maxHr > 0) "${act.maxHr}" else "--", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.ui.graphics.Color(0xFFEF4444))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    com.void200.runku.ui.components.HrChart(points.map { it.hr }, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    com.void200.runku.ui.components.ZoneBar(
                        if (act.avgHr > 0) vm.zone(act.avgHr).ordinal + 1 else 0,
                        Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("Z1", "Z2", "Z3", "Z4", "Z5").forEach {
                            Text(it, fontSize = 10.sp, color = c.muted)
                        }
                    }
                }
            }

            // ---- splits per km ----
            val splits = com.void200.runku.data.Splits.from(points)
            if (splits.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Label("Split per km")
                Spacer(Modifier.height(10.dp))
                RunkuCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val maxSec = splits.maxOf { it.secPerKm }
                        splits.forEach { sp ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (sp.isPartial) "%.1f".format(activity!!.distanceMeters / 1000.0) else "${sp.km} km",
                                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = c.muted,
                                    modifier = Modifier.width(52.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    Modifier.weight(1f).height(8.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(c.surface2),
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth((sp.secPerKm / maxSec).toFloat().coerceIn(0.1f, 1f))
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(c.accentGradient),
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    Geo.formatPace(sp.secPerKm), fontSize = 12.sp,
                                    color = c.text, modifier = Modifier.width(56.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                )
                            }
                        }
                    }
                }
            }

            // ---- badges ----
            val badges = com.void200.runku.data.Badges.forActivity(act, allActivities)
            if (badges.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Label("Pencapaian")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    badges.forEach { b ->
                        Column(
                            Modifier.weight(1f)
                                .clip(MaterialTheme.shapes.small)
                                .background(c.surface2)
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(b.icon, fontSize = 20.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(b.label, fontSize = 11.sp, color = c.muted, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Label("Unduh / ekspor")
            Spacer(Modifier.height(10.dp))
            RunkuCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExportRow("GPX", "Rekam jejak GPS", c) {
                        Exporter.writeAndShare(context, "runku-${act.id}.gpx", Exporter.gpx(act, points))
                    }
                    ExportRow("JSON", "Semua data mentah", c) {
                        Exporter.writeAndShare(context, "runku-${act.id}.json", Exporter.json(act, points))
                    }
                    ExportRow("CSV", "Delimited · Excel/Sheets", c) {
                        Exporter.writeAndShare(context, "runku-${act.id}.csv", Exporter.csv(act, points))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Chip("Samarkan area", modifier = Modifier.weight(1f))
                Chip("Ubah nama", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailTabs(nav: NavController) {
    val c = MaterialTheme.runku
    Row(
        Modifier.fillMaxWidth().background(c.bg).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf("Detail" to true, "Split" to false, "Bagikan" to false).forEach { (label, on) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(23.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (on) c.accent else c.muted.copy(alpha = 0.45f)),
                )
                androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                Text(label, fontSize = 10.sp, color = if (on) c.accent else c.muted)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Card3(k: String, v: String) {
    val c = MaterialTheme.runku
    Column(
        Modifier
            .weight(1f)
            .padding(10.dp),
    ) {
        Label(k)
        Spacer(Modifier.height(4.dp))
        Text(v, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.text)
    }
}

@Composable
private fun ExportRow(label: String, desc: String, c: com.void200.runku.ui.theme.RunkuColors, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold, color = c.text)
            Text(desc, fontSize = 12.sp, color = c.muted)
        }
        Chip("Unduh", selected = true, onClick = onClick)
    }
}
