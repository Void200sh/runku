package com.void200.runku.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Access the active runku palette from any composable. */
val LocalRunkuColors = staticCompositionLocalOf<RunkuColors> {
    error("RunkuColors not provided — wrap in RunkuTheme{}")
}

/** Convenience: MaterialTheme.runku */
val MaterialTheme.runku: RunkuColors
    @Composable get() = LocalRunkuColors.current

@Composable
fun RunkuTheme(
    theme: RunkuTheme = RunkuTheme.PIXEL,
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val c = Palettes.get(theme, dark)

    val scheme = (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = c.accent,
        onPrimary = c.accentInk,
        primaryContainer = c.accentSoft,
        onPrimaryContainer = c.accent,
        secondary = c.accent2,
        onSecondary = c.accentInk,
        background = c.bg,
        onBackground = c.text,
        surface = c.surface,
        onSurface = c.text,
        surfaceVariant = c.surface2,
        onSurfaceVariant = c.muted,
        outline = c.line,
        error = c.danger,
        onError = Color.White,
    )

    CompositionLocalProvider(LocalRunkuColors provides c) {
        MaterialTheme(
            colorScheme = scheme,
            shapes = Palettes.shapes(theme),
            typography = RunkuTypography,
            content = content,
        )
    }
}
