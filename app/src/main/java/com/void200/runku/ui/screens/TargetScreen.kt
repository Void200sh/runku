package com.void200.runku.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.void200.runku.data.TargetType
import com.void200.runku.ui.RunkuViewModel
import com.void200.runku.ui.components.Chip
import com.void200.runku.ui.components.Label
import com.void200.runku.ui.components.NavTab
import com.void200.runku.ui.components.RunkuBottomBar
import com.void200.runku.ui.components.RunkuCard
import com.void200.runku.ui.theme.runku

@Composable
fun TargetScreen(vm: RunkuViewModel, nav: NavController) {
    val c = MaterialTheme.runku
    var type by remember { mutableStateOf(TargetType.DISTANCE) }
    var value by remember { mutableStateOf(5.0) }

    Scaffold(
        containerColor = c.bg,
        bottomBar = {
            RunkuBottomBar(NavTab.TARGET, onSelect = { tab -> nav.navigate(tab.route) { launchSingleTop = true } })
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(horizontal = 18.dp).padding(top = 8.dp),
        ) {
            Label("Sesi lari")
            Text("Target", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = c.text)
            Spacer(Modifier.height(4.dp))
            Text("Pilih mau lari berapa km / pace berapa / berapa lama.", fontSize = 13.sp, color = c.muted)

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("Jarak", type == TargetType.DISTANCE, onClick = { type = TargetType.DISTANCE; value = 5.0 })
                Chip("Durasi", type == TargetType.DURATION, onClick = { type = TargetType.DURATION; value = 30.0 })
                Chip("Bebas", type == TargetType.FREE, onClick = { type = TargetType.FREE; value = 0.0 })
            }

            Spacer(Modifier.height(20.dp))
            RunkuCard(Modifier.fillMaxWidth()) {
                Column {
                    Label(if (type == TargetType.FREE) "Mode" else "Nilai target")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        when (type) {
                            TargetType.DISTANCE -> "%.1f km".format(value)
                            TargetType.DURATION -> "%.0f menit".format(value)
                            TargetType.PACE -> "%.0f s/km".format(value)
                            TargetType.FREE -> "Bebas sepuasnya"
                        },
                        fontSize = 34.sp, fontWeight = FontWeight.Bold, color = c.accent,
                    )
                    if (type != TargetType.FREE) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val steps = if (type == TargetType.DISTANCE) listOf(1.0, 3.0, 5.0, 10.0, 21.0)
                            else listOf(15.0, 30.0, 45.0, 60.0, 90.0)
                            steps.forEach { v ->
                                val label = if (type == TargetType.DISTANCE) {
                                    if (v >= 1) "${v.toInt()}k" else "${v}k"
                                } else {
                                    "${v.toInt()}'"
                                }
                                Chip(label, selected = value == v, onClick = { value = v })
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.startRun(targetType = type, targetValue = value); nav.navigate("live") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.accent, contentColor = c.accentInk),
            ) { Text("Mulai dengan Target", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        }
    }
}
