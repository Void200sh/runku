package com.void200.runku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.void200.runku.ui.theme.runku

enum class NavTab(val label: String, val icon: ImageVector, val route: String) {
    HOME("Beranda", Icons.Filled.Home, "home"),
    TARGET("Target", Icons.Filled.TrackChanges, "target"),
    MAP("Peta", Icons.Filled.LocationOn, "live"),
    SETTINGS("Atur", Icons.Filled.Settings, "settings"),
}

@Composable
fun RunkuBottomBar(current: NavTab, onSelect: (NavTab) -> Unit, modifier: Modifier = Modifier) {
    val c = MaterialTheme.runku
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(c.bg)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavTab.entries.forEach { tab ->
            val on = tab == current
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (on) c.accentSoft else c.bg)
                        .padding(horizontal = 18.dp, vertical = 5.dp),
                ) {
                    Icon(
                        tab.icon, tab.label,
                        tint = if (on) c.accent else c.muted,
                        modifier = Modifier.size(23.dp),
                    )
                }
                Text(
                    tab.label,
                    fontSize = 10.sp,
                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (on) c.accent else c.muted,
                )
            }
        }
    }
}
