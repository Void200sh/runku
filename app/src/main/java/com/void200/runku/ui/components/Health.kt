package com.void200.runku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.void200.runku.ui.theme.runku

@Composable
fun HeartRateCard(
    bpm: Int,
    zoneLabel: String,
    sourceLabel: String,
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.runku
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(c.surface2)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Filled.Favorite,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(26.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                "Detak jantung · $zoneLabel",
                fontSize = 11.sp, letterSpacing = 1.2.sp, color = c.muted,
                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$bpm", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = c.text)
                Spacer(Modifier.size(4.dp))
                Text("bpm", fontSize = 13.sp, color = c.muted)
            }
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(c.accentSoft)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                sourceLabel, fontSize = 11.sp, color = c.accent, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun ZoneBar(activeZone: Int, modifier: Modifier = Modifier) {
    val zoneColors = listOf(
        Color(0xFF94A3B8), Color(0xFF38BDF8), Color(0xFF34D399),
        Color(0xFFFBBF24), Color(0xFFEF4444),
    )
    Column(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(999.dp)),
        ) {
            zoneColors.forEachIndexed { i, col ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(if (i + 1 == activeZone) col else col.copy(alpha = 0.3f)),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Z1", "Z2", "Z3", "Z4", "Z5").forEach {
                Text(it, fontSize = 10.sp, color = c0())
            }
        }
    }
}

@Composable
private fun c0() = MaterialTheme.runku.muted

@Composable
fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val c = MaterialTheme.runku
    Box(
        modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(c.surface2),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(c.accentGradient),
        )
    }
}
