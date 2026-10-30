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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.os.Build
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.navigation.NavController
import com.void200.runku.location.Geo
import com.void200.runku.map.RouteMap
import com.void200.runku.ui.RunkuViewModel
import com.void200.runku.ui.components.Chip
import com.void200.runku.ui.components.HeartRateCard
import com.void200.runku.ui.components.Label
import com.void200.runku.ui.components.Metric
import com.void200.runku.ui.components.NavTab
import com.void200.runku.ui.components.ProgressBar
import com.void200.runku.ui.components.RunkuBottomBar
import com.void200.runku.ui.components.RunkuCard
import com.void200.runku.ui.components.StatGrid
import com.void200.runku.ui.components.ZoneBar
import com.void200.runku.ui.theme.runku

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LiveRunScreen(vm: RunkuViewModel, nav: NavController) {
    val c = MaterialTheme.runku
    val s by vm.live.collectAsState()
    val settings by vm.settingsFlow.collectAsState()
    val context = LocalContext.current

    val needed = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (settings.hrSource == "ble") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        if (settings.hrSource == "camera") {
            add(Manifest.permission.CAMERA)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val multiple = rememberMultiplePermissionsState(needed)
    val locationGranted = multiple.permissions.any {
        it.permission == Manifest.permission.ACCESS_FINE_LOCATION && it.status.isGranted
    }
    val bluetoothGranted = Build.VERSION.SDK_INT < 31 ||
        (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
         ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
    val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        if (!multiple.allPermissionsGranted) multiple.launchMultiplePermissionRequest()
        // Do NOT auto-start here: "Stop" must stay stopped. Start is triggered
        // explicitly from Home / Target, or restored if a run is already active.
    }
    LaunchedEffect(locationGranted, bluetoothGranted, cameraGranted, s.running, settings.hrSource) {
        if (locationGranted && s.running) {
            val source = when {
                settings.hrSource == "camera" && cameraGranted -> "camera"
                settings.hrSource == "ble" && bluetoothGranted -> "ble"
                else -> "off"
            }
            vm.bindSensors(context, source, lifecycleOwner)
        }
    }
    DisposableEffect(Unit) { onDispose { /* keep bound; unbind on stop */ } }

    if (!s.running) {
        Scaffold(
            containerColor = c.bg,
            bottomBar = {
                RunkuBottomBar(NavTab.HOME, onSelect = { tab -> nav.navigate(tab.route) { launchSingleTop = true } })
            },
        ) { pad ->
            Column(
                Modifier.fillMaxSize().padding(pad).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (s.saving) {
                    androidx.compose.material3.CircularProgressIndicator(color = c.accent)
                    Spacer(Modifier.height(16.dp))
                    Text("Menyimpan lari…", fontSize = 15.sp, color = c.text, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Belum ada lari aktif", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.text)
                    Spacer(Modifier.height(8.dp))
                    Text("Mulai dari Beranda atau Target.", fontSize = 13.sp, color = c.muted)
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { if (locationGranted) vm.startRun() else multiple.launchMultiplePermissionRequest() },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = c.accent, contentColor = c.accentInk),
                    ) { Text("Mulai Lari", fontWeight = FontWeight.Bold) }
                }
            }
        }
        return
    }

    Scaffold(
        containerColor = c.bg,
        bottomBar = {
            RunkuBottomBar(NavTab.HOME, onSelect = { tab -> nav.navigate(tab.route) { launchSingleTop = true } })
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Chip("● Live", selected = true)
                Chip(if (multiple.allPermissionsGranted) "Offline OK" else "Izin…")
            }

            Spacer(Modifier.height(20.dp))
            Label("Jarak")
            Metric(Geo.formatDistance(s.distanceMeters), "km", size = 60, valueColor = c.accent)

            if (s.targetType != com.void200.runku.data.TargetType.FREE && s.targetValue > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Target ${s.targetValue} · ${(s.progress * 100).toInt()}%",
                    fontSize = 13.sp, color = c.muted,
                )
                Spacer(Modifier.height(10.dp))
                ProgressBar(s.progress, Modifier.padding(horizontal = 4.dp))
            }

            Spacer(Modifier.height(18.dp))
            StatGrid(
                items = listOf(
                    "Durasi" to Geo.formatDuration(s.elapsedMillis),
                    "Pace" to Geo.formatPace(s.paceSecPerKm),
                    "Kecepatan" to "%.1f km/h".format(s.speedKmh),
                    "Elevasi" to "+%d m".format(s.elevationGain.toInt()),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))
            // HR card ALWAYS visible — shows "--" until a sensor reports.
            HeartRateCard(
                bpm = s.currentBpm,
                zoneLabel = if (s.currentBpm > 0) vm.zone(s.currentBpm).label else "—",
                sourceLabel = when (settings.hrSource) {
                    "ble" -> "Strap BLE"
                    "camera" -> "Kamera"
                    else -> "Mati"
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            ZoneBar(if (s.currentBpm > 0) vm.zone(s.currentBpm).ordinal + 1 else 0, Modifier.fillMaxWidth())
            if (s.currentBpm == 0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    if (settings.hrSource == "off") "Sensor detak jantung dimatikan (Pengaturan → Sensor)."
                    else "Mencari sensor detak jantung… pastikan strap BLE menyala & terpasang.",
                    fontSize = 11.sp, color = c.muted,
                )
            }

            Spacer(Modifier.height(16.dp))
            RunkuCard(Modifier.fillMaxWidth().height(150.dp)) {
                RouteMap(s.points, Modifier.fillMaxSize())
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { if (s.paused) vm.resume() else vm.pause() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.surface2, contentColor = c.accent),
                ) { Text(if (s.paused) "Lanjut" else "Jeda", fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { vm.stopRun { id -> nav.navigate("detail/$id") { popUpTo("home") } } },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.danger, contentColor = androidx.compose.ui.graphics.Color.White),
                ) { Text("Stop", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
