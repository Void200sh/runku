package com.void200.runku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.void200.runku.ui.theme.runku
import androidx.compose.material3.MaterialTheme

@Composable
fun Label(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.runku.muted,
        modifier = modifier,
    )
}

@Composable
fun RunkuCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val c = MaterialTheme.runku
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(c.surface)
            .border(1.dp, c.line, MaterialTheme.shapes.medium)
            .padding(16.dp),
    ) { content() }
}

@Composable
fun Metric(
    value: String,
    unit: String? = null,
    size: Int = 52,
    valueColor: Color? = null,
) {
    val c = MaterialTheme.runku
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            value,
            fontSize = size.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp,
            color = valueColor ?: c.text,
        )
        if (unit != null) {
            Spacer(Modifier.size(4.dp))
            Text(unit, fontSize = (size / 3).sp, color = c.muted, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StatTile(
    key: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null,
) {
    val c = MaterialTheme.runku
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(c.surface2)
            .border(1.dp, c.line, MaterialTheme.shapes.small)
            .padding(14.dp),
    ) {
        Label(key)
        Spacer(Modifier.height(6.dp))
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = valueColor ?: c.text)
    }
}

@Composable
fun StatGrid(
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (k, v) -> StatTile(k, v, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun Chip(
    text: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.runku
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) c.accentSoft else c.surface2)
            .border(1.dp, if (selected) c.accent else c.line, RoundedCornerShape(999.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) Icon(icon, null, tint = if (selected) c.accent else c.text, modifier = Modifier.size(15.dp))
        Text(text, fontSize = 12.sp, color = if (selected) c.accent else c.text)
    }
}
