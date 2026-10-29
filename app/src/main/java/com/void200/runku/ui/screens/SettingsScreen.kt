package com.void200.runku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.void200.runku.ui.RunkuViewModel
import com.void200.runku.ui.components.Chip
import com.void200.runku.ui.components.Label
import com.void200.runku.ui.components.NavTab
import com.void200.runku.ui.components.RunkuBottomBar
import com.void200.runku.ui.theme.RunkuTheme
import com.void200.runku.ui.theme.runku

@Composable
fun SettingsScreen(vm: RunkuViewModel, nav: NavController) {
    val c = MaterialTheme.runku
    val settings by vm.settingsFlow.collectAsState()
    val hrOptions = listOf("ble" to "Strap Bluetooth (BLE)", "camera" to "Kamera HP (jari)", "off" to "Mati")
    val hrIdx = hrOptions.indexOfFirst { it.first == settings.hrSource }.coerceAtLeast(0)

    Scaffold(
        containerColor = c.bg,
        bottomBar = {
            RunkuBottomBar(NavTab.SETTINGS, onSelect = { tab -> nav.navigate(tab.route) { launchSingleTop = true } })
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(horizontal = 18.dp).padding(top = 8.dp),
        ) {
            Label("Pengaturan")
            Text("Tampilan", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = c.text)

            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Mode gelap", fontWeight = FontWeight.SemiBold, color = c.text)
                    Text("Ikut sistem atau manual", fontSize = 12.sp, color = c.muted)
                }
                Switch(
                    checked = settings.darkMode == "dark",
                    onCheckedChange = { vm.setDarkMode(if (it) "dark" else "light") },
                    colors = SwitchDefaults.colors(checkedTrackColor = c.accent),
                )
            }

            Spacer(Modifier.height(20.dp))
            Label("Pilih tema")
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(330.dp),
            ) {
                items(RunkuTheme.entries) { theme ->
                    ThemeCard(
                        theme = theme,
                        selected = settings.theme == theme,
                        onClick = { vm.setTheme(theme.id) },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Label("Sensor detak jantung")
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).background(c.surface).padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Sumber HR", fontWeight = FontWeight.SemiBold, color = c.text)
                    Text(hrOptions[hrIdx].second, fontSize = 12.sp, color = c.muted)
                }
                Chip("Ganti", selected = true, onClick = {
                    val next = hrOptions[(hrIdx + 1) % hrOptions.size].first
                    vm.setHrSource(next)
                })
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).background(c.surface).padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Status koneksi", fontWeight = FontWeight.SemiBold, color = c.text)
                    Text(
                        if (settings.hrSource == "off") "Nonaktif" else "Menunggu sensor…",
                        fontSize = 12.sp, color = c.muted,
                    )
                }
                Chip("●●●")
            }

            Spacer(Modifier.height(20.dp))
            Label("Peta")
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).background(c.surface).padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Mode peta", fontWeight = FontWeight.SemiBold, color = c.text)
                    Text(
                        if (settings.mapMode == "tiles") "Peta kota (MBTiles)" else "Canvas (default · ringan)",
                        fontSize = 12.sp, color = c.muted,
                    )
                }
                Chip("Ganti", selected = true, onClick = {
                    vm.setMapMode(if (settings.mapMode == "tiles") "canvas" else "tiles")
                })
            }

            Spacer(Modifier.height(22.dp))
            Label("Lainnya")
            Spacer(Modifier.height(10.dp))
            RunkuCardCompat {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Satuan", fontWeight = FontWeight.SemiBold, color = c.text)
                        Text("km · km/h", fontSize = 12.sp, color = c.muted)
                    }
                    Chip("Ubah")
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Ekspor data", fontWeight = FontWeight.SemiBold, color = c.text)
                        Text("GPX / JSON / CSV", fontSize = 12.sp, color = c.muted)
                    }
                    Chip("Ekspor")
                }
            }

            Spacer(Modifier.height(24.dp))
            com.void200.runku.ui.components.BrandMark(showCredit = true, iconSize = 34)
            Spacer(Modifier.height(10.dp))
            com.void200.runku.ui.components.CreditLine(Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ThemeCard(theme: RunkuTheme, selected: Boolean, onClick: () -> Unit) {
    val c = MaterialTheme.runku
    val pal = com.void200.runku.ui.theme.Palettes.get(theme, true)
    Box(
        Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(c.surface)
            .border(1.5.dp, if (selected) c.accent else c.line, MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(12.dp),
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(pal.accent, pal.accent2, pal.bg).forEach { col ->
                    Box(
                        Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(col)
                            .border(1.dp, c.line, RoundedCornerShape(6.dp)),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(theme.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = c.text)
            Text(theme.vibe, fontSize = 11.sp, color = c.muted)
        }
        if (selected) {
            Icon(
                Icons.Filled.Check, null, tint = c.accentInk,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(c.accent)
                    .padding(2.dp),
            )
        }
    }
}


@Composable
private fun RunkuCardCompat(content: @Composable () -> Unit) {
    val c = MaterialTheme.runku
    androidx.compose.foundation.layout.Box(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(c.surface)
            .padding(14.dp),
    ) { androidx.compose.foundation.layout.Column { content() } }
}
