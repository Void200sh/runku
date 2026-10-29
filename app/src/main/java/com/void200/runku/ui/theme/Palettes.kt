package com.void200.runku.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * runku theme system.
 *
 * Nine visual themes, each with a light + dark palette. The theme only swaps
 * colors + accent + shape scale; layout/visual structure never changes.
 * Mirrors the HTML preview (preview/index.html).
 */
enum class RunkuTheme(val id: String, val label: String, val vibe: String) {
    PIXEL("pixel", "Pixel", "Material You"),
    EMERALD("emerald", "Emerald", "Sporty"),
    MIDNIGHT("midnight", "Midnight", "Biru gelap"),
    SUNSET("sunset", "Sunset", "Oranye"),
    NEON("neon", "Neon", "Cyberpunk"),
    AURORA("aurora", "Aurora", "Gradien utara"),
    CARBON("carbon", "Carbon", "Racing"),
    SAKURA("sakura", "Sakura", "Soft pink"),
    MONO("mono", "Mono", "Hitam-putih");

    companion object {
        fun fromId(id: String): RunkuTheme = entries.firstOrNull { it.id == id } ?: PIXEL
    }
}

/** One theme palette (already resolved for a specific light/dark mode). */
data class RunkuColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val line: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val accent2: Color,
    val accentInk: Color,
    val accentSoft: Color,
    val danger: Color,
    val warn: Color,
    val glow: Boolean = false,
) {
    val accentGradient: Brush
        get() = Brush.horizontalGradient(listOf(accent, accent2))
}

object Palettes {

    fun get(theme: RunkuTheme, dark: Boolean): RunkuColors = when (theme) {

        RunkuTheme.PIXEL -> if (dark) RunkuColors(
            bg = Color(0xFF0C0E13), surface = Color(0xFF171A21), surface2 = Color(0xFF1F2531),
            line = Color(0xFF2A3346), text = Color(0xFFEEF1F6), muted = Color(0xFF8B93A1),
            accent = Color(0xFF8AB4FF), accent2 = Color(0xFFC4B0FF), accentInk = Color(0xFF04121F),
            accentSoft = Color(0xFF1C2B47), danger = Color(0xFFF87171), warn = Color(0xFFFBBF24),
        ) else RunkuColors(
            bg = Color(0xFFF7F9FC), surface = Color(0xFFFFFFFF), surface2 = Color(0xFFE9EFFB),
            line = Color(0xFFE0E6F2), text = Color(0xFF12151B), muted = Color(0xFF68707E),
            accent = Color(0xFF3B6EF6), accent2 = Color(0xFF7D5CFF), accentInk = Color(0xFFFFFFFF),
            accentSoft = Color(0xFFDFE8FF), danger = Color(0xFFE0483B), warn = Color(0xFFE0A300),
        )

        RunkuTheme.EMERALD -> if (dark) RunkuColors(
            bg = Color(0xFF0B0D10), surface = Color(0xFF14171C), surface2 = Color(0xFF1B1F26),
            line = Color(0xFF262B33), text = Color(0xFFEEF1F5), muted = Color(0xFF8B93A1),
            accent = Color(0xFF34D399), accent2 = Color(0xFF22D3EE), accentInk = Color(0xFF062018),
            accentSoft = Color(0xFF123A2C), danger = Color(0xFFF87171), warn = Color(0xFFFBBF24),
        ) else RunkuColors(
            bg = Color(0xFFF4F8F6), surface = Color(0xFFFFFFFF), surface2 = Color(0xFFE9F3EE),
            line = Color(0xFFDCEBE4), text = Color(0xFF0E1714), muted = Color(0xFF5C6F68),
            accent = Color(0xFF34D399), accent2 = Color(0xFF22D3EE), accentInk = Color(0xFF062018),
            accentSoft = Color(0xFFDCF5EA), danger = Color(0xFFDC2626), warn = Color(0xFFD97706),
        )

        RunkuTheme.MIDNIGHT -> if (dark) RunkuColors(
            bg = Color(0xFF080C13), surface = Color(0xFF111826), surface2 = Color(0xFF18202F),
            line = Color(0xFF25324A), text = Color(0xFFEAF0FF), muted = Color(0xFF7E91AB),
            accent = Color(0xFF60A5FA), accent2 = Color(0xFFA78BFA), accentInk = Color(0xFF06132A),
            accentSoft = Color(0xFF132141), danger = Color(0xFFF87171), warn = Color(0xFFFBBF24),
        ) else RunkuColors(
            bg = Color(0xFFEEF4FB), surface = Color(0xFFFFFFFF), surface2 = Color(0xFFE3EDF9),
            line = Color(0xFFD8E5F2), text = Color(0xFF0D1826), muted = Color(0xFF5D6F83),
            accent = Color(0xFF60A5FA), accent2 = Color(0xFFA78BFA), accentInk = Color(0xFF06132A),
            accentSoft = Color(0xFFDCEBFD), danger = Color(0xFFDC2626), warn = Color(0xFFD97706),
        )

        RunkuTheme.SUNSET -> if (dark) RunkuColors(
            bg = Color(0xFF120D0C), surface = Color(0xFF1C1514), surface2 = Color(0xFF261C1A),
            line = Color(0xFF382824), text = Color(0xFFFBEEDD), muted = Color(0xFF9E857C),
            accent = Color(0xFFFB8A6F), accent2 = Color(0xFFFBBF24), accentInk = Color(0xFF2A0A12),
            accentSoft = Color(0xFF3A1E17), danger = Color(0xFFF87171), warn = Color(0xFFFBBF24),
        ) else RunkuColors(
            bg = Color(0xFFFDF5F4), surface = Color(0xFFFFFFFF), surface2 = Color(0xFFFBEAE6),
            line = Color(0xFFF4DDD6), text = Color(0xFF1E1310), muted = Color(0xFF7D6A64),
            accent = Color(0xFFF2705A), accent2 = Color(0xFFFBBF24), accentInk = Color(0xFF2A0A12),
            accentSoft = Color(0xFFFFE6DF), danger = Color(0xFFDC2626), warn = Color(0xFFD97706),
        )

        RunkuTheme.NEON -> if (dark) RunkuColors(
            bg = Color(0xFF05060C), surface = Color(0xFF0C0F1A), surface2 = Color(0xFF121629),
            line = Color(0xFF232A44), text = Color(0xFFE9EEFF), muted = Color(0xFF7F8BAD),
            accent = Color(0xFF22D3EE), accent2 = Color(0xFFF0ABFC), accentInk = Color(0xFF021317),
            accentSoft = Color(0xFF0E1C2E), danger = Color(0xFFFB7185), warn = Color(0xFFFBBF24),
            glow = true,
        ) else RunkuColors(
            bg = Color(0xFFF3F6FF), surface = Color(0xFFFFFFFF), surface2 = Color(0xFFE8ECFF),
            line = Color(0xFFD7E3F5), text = Color(0xFF0A1020), muted = Color(0xFF5B6B86),
            accent = Color(0xFF0891B2), accent2 = Color(0xFFC026D3), accentInk = Color(0xFFFFFFFF),
            accentSoft = Color(0xFFD8F6FB), danger = Color(0xFFDC2626), warn = Color(0xFFD97706),
        )

        RunkuTheme.AURORA -> if (dark) RunkuColors(
            bg = Color(0xFF060A12), surface = Color(0xFF0D1420), surface2 = Color(0xFF141D2E),
            line = Color(0xFF20304A), text = Color(0xFFEAF4FF), muted = Color(0xFF7E91AB),
            accent = Color(0xFF5EEAD4), accent2 = Color(0xFFA78BFA), accentInk = Color(0xFF04211C),
            accentSoft = Color(0xFF0E2230), danger = Color(0xFFF87171), warn = Color(0xFFFBBF24),
        ) else RunkuColors(
            bg = Color(0xFFF2F7FB), surface = Color(0xFFFFFFFF), surface2 = Color(0xFFE6F0F5),
            line = Color(0xFFD6E6EE), text = Color(0xFF07171C), muted = Color(0xFF587084),
            accent = Color(0xFF0D9488), accent2 = Color(0xFF7C3AED), accentInk = Color(0xFFFFFFFF),
            accentSoft = Color(0xFFD6F3EE), danger = Color(0xFFDC2626), warn = Color(0xFFD97706),
        )

        RunkuTheme.CARBON -> if (dark) RunkuColors(
            bg = Color(0xFF0A0A0B), surface = Color(0xFF141416), surface2 = Color(0xFF1C1C1F),
            line = Color(0xFF2B2B30), text = Color(0xFFF2F2F4), muted = Color(0xFF8F8F99),
            accent = Color(0xFFF43F5E), accent2 = Color(0xFFFBBF24), accentInk = Color(0xFFFFFFFF),
            accentSoft = Color(0xFF331119), danger = Color(0xFFF87171), warn = Color(0xFFFBBF24),
        ) else RunkuColors(
            bg = Color(0xFFF5F5F6), surface = Color(0xFFFFFFFF), surface2 = Color(0xFFECECEE),
            line = Color(0xFFE0E0E3), text = Color(0xFF0D0D0F), muted = Color(0xFF6B6B70),
            accent = Color(0xFFE11D48), accent2 = Color(0xFFD97706), accentInk = Color(0xFFFFFFFF),
            accentSoft = Color(0xFFFFE2E8), danger = Color(0xFFDC2626), warn = Color(0xFFD97706),
        )

        RunkuTheme.SAKURA -> if (dark) RunkuColors(
            bg = Color(0xFF130A10), surface = Color(0xFF1D121A), surface2 = Color(0xFF271824),
            line = Color(0xFF3A2333), text = Color(0xFFFDEEF7), muted = Color(0xFFA5869B),
            accent = Color(0xFFF472B6), accent2 = Color(0xFFC084FC), accentInk = Color(0xFF3A0722),
            accentSoft = Color(0xFF331428), danger = Color(0xFFF87171), warn = Color(0xFFFBBF24),
        ) else RunkuColors(
            bg = Color(0xFFFDF5FA), surface = Color(0xFFFFFFFF), surface2 = Color(0xFFFBE9F4),
            line = Color(0xFFF6DCEC), text = Color(0xFF24101C), muted = Color(0xFF7D6375),
            accent = Color(0xFFEC4899), accent2 = Color(0xFFC084FC), accentInk = Color(0xFFFFFFFF),
            accentSoft = Color(0xFFFFE4F1), danger = Color(0xFFDC2626), warn = Color(0xFFD97706),
        )

        RunkuTheme.MONO -> if (dark) RunkuColors(
            bg = Color(0xFF0A0A0A), surface = Color(0xFF151515), surface2 = Color(0xFF1D1D1D),
            line = Color(0xFF2A2A2A), text = Color(0xFFFAFAFA), muted = Color(0xFF9A9A9A),
            accent = Color(0xFFFAFAFA), accent2 = Color(0xFFBFBFBF), accentInk = Color(0xFF0A0A0A),
            accentSoft = Color(0xFF242424), danger = Color(0xFFF87171), warn = Color(0xFFFBBF24),
        ) else RunkuColors(
            bg = Color(0xFFFAFAFA), surface = Color(0xFFFFFFFF), surface2 = Color(0xFFF1F1F1),
            line = Color(0xFFE4E4E4), text = Color(0xFF0A0A0A), muted = Color(0xFF6B6B6B),
            accent = Color(0xFF18181B), accent2 = Color(0xFF52525B), accentInk = Color(0xFFFFFFFF),
            accentSoft = Color(0xFFE9E9E9), danger = Color(0xFFDC2626), warn = Color(0xFFD97706),
        )
    }

    fun shapes(theme: RunkuTheme): Shapes {
        val (large, medium, small) = when (theme) {
            RunkuTheme.PIXEL -> Triple(28.dp, 22.dp, 16.dp)
            RunkuTheme.SAKURA -> Triple(28.dp, 20.dp, 14.dp)
            RunkuTheme.CARBON -> Triple(14.dp, 10.dp, 8.dp)
            RunkuTheme.MONO -> Triple(18.dp, 12.dp, 8.dp)
            RunkuTheme.NEON -> Triple(16.dp, 12.dp, 8.dp)
            RunkuTheme.MIDNIGHT -> Triple(20.dp, 16.dp, 12.dp)
            else -> Triple(22.dp, 16.dp, 12.dp)
        }
        return Shapes(
            large = RoundedCornerShape(large),
            medium = RoundedCornerShape(medium),
            small = RoundedCornerShape(small),
        )
    }
}
