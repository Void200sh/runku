package com.void200.runku.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.void200.runku.R
import com.void200.runku.ui.theme.runku

/** runku brand: logo tile + wordmark + author credit. */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    showCredit: Boolean = true,
    iconSize: Int = 30,
) {
    val c = MaterialTheme.runku
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = "runku",
            modifier = Modifier.size(iconSize.dp),
        )
        Column {
            Text(
                buildAnnotatedString {
                    append("run")
                    withStyle(SpanStyle(color = c.accent, fontWeight = FontWeight.ExtraBold)) { append("ku") }
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
                color = c.text,
            )
            if (showCredit) {
                Text(
                    "oleh prawira dwimadya",
                    fontSize = 10.sp,
                    letterSpacing = 0.4.sp,
                    color = c.muted,
                )
            }
        }
    }
}

/** Plain credit line for footers. */
@Composable
fun CreditLine(modifier: Modifier = Modifier) {
    val c = MaterialTheme.runku
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("runku · v1.0", fontSize = 12.sp, color = c.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.width(0.dp))
        Text("oleh prawira dwimadya", fontSize = 11.sp, color = c.muted)
    }
}
