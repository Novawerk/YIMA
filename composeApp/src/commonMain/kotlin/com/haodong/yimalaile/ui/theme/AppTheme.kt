package com.haodong.yimalaile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ============================================================
// Color Palettes
// ============================================================

/** Palette-agnostic semantic color roles used by UI components directly. */
data class AppExtraColors(
    val accent: Color,
    val accentMuted: Color,
    val cardBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val pageBg: Color,
)

val LocalAppExtraColors = staticCompositionLocalOf {
    AppExtraColors(
        accent = Color.Unspecified, accentMuted = Color.Unspecified,
        cardBg = Color.Unspecified, textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified, pageBg = Color.Unspecified,
    )
}

// Shortcut used everywhere instead of raw hex
object AppColors {
    // Resolved at runtime via LocalAppExtraColors — these are the warm defaults
    // for compile-time references (e.g. previews). Real values come from the theme.
    val DeepRose = Color(0xFFa66d6d)
    val DarkCoffee = Color(0xFF5d4037)
    val WarmPeach = Color(0xFFF7CDC3)
    val BlushPink = Color(0xFFF4DCD6)
    val SoftCream = Color(0xFFFFF9F8)
    val WarmCreamBeige = Color(0xFFFDF8F5)
    val DarkChocolate = Color(0xFF2D2420)
    val MidnightEspresso = Color(0xFF221610)
    val SageGreen = Color(0xFF90A17D)
}

// ---------- Palette: Warm (default) ----------

private val WarmLight = lightColorScheme(
    primary = Color(0xFFa66d6d),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF4DCD6),
    onPrimaryContainer = Color(0xFF5d4037),
    secondary = Color(0xFFF7CDC3),
    secondaryContainer = Color(0xFFF7CDC3).copy(alpha = 0.5f),
    background = Color(0xFFFFF9F8),
    surface = Color(0xFFFFF9F8),
    surfaceVariant = Color(0xFFFDF8F5),
    onBackground = Color(0xFF5d4037),
    onSurface = Color(0xFF5d4037),
    onSurfaceVariant = Color(0xFF5d4037).copy(alpha = 0.6f),
    outline = Color(0xFFa66d6d).copy(alpha = 0.15f),
)
private val WarmDark = darkColorScheme(
    primary = Color(0xFFF7CDC3),
    onPrimary = Color(0xFF2D2420),
    primaryContainer = Color(0xFFa66d6d).copy(alpha = 0.3f),
    onPrimaryContainer = Color(0xFFF7CDC3),
    secondary = Color(0xFFF7CDC3).copy(alpha = 0.7f),
    secondaryContainer = Color(0xFFa66d6d).copy(alpha = 0.2f),
    background = Color(0xFF2D2420),
    surface = Color(0xFF2D2420),
    surfaceVariant = Color(0xFF221610),
    onBackground = Color(0xFFF7CDC3),
    onSurface = Color(0xFFF4DCD6),
    onSurfaceVariant = Color(0xFFF4DCD6).copy(alpha = 0.6f),
    outline = Color(0xFFF7CDC3).copy(alpha = 0.15f),
)
private val WarmExtraLight = AppExtraColors(
    accent = Color(0xFFa66d6d), accentMuted = Color(0xFFF7CDC3),
    cardBg = Color(0xFFF4DCD6).copy(alpha = 0.45f),
    textPrimary = Color(0xFF5d4037), textSecondary = Color(0xFF5d4037).copy(alpha = 0.5f),
    pageBg = Color(0xFFFFF9F8),
)
private val WarmExtraDark = AppExtraColors(
    accent = Color(0xFFF7CDC3), accentMuted = Color(0xFFa66d6d).copy(alpha = 0.4f),
    cardBg = Color(0xFFa66d6d).copy(alpha = 0.2f),
    textPrimary = Color(0xFFF4DCD6), textSecondary = Color(0xFFF4DCD6).copy(alpha = 0.5f),
    pageBg = Color(0xFF2D2420),
)

// ---------- Palette: Vivid (Lemon Yellow + Hot Pink) ----------

private val VividLight = lightColorScheme(
    primary = Color(0xFFE91E63),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCE4EC),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFFFFEB3B),
    secondaryContainer = Color(0xFFFFF9C4),
    background = Color(0xFFFFFDE7),
    surface = Color(0xFFFFFDE7),
    surfaceVariant = Color(0xFFFFF9C4),
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF1A1A1A).copy(alpha = 0.6f),
    outline = Color(0xFFE91E63).copy(alpha = 0.2f),
)
private val VividDark = darkColorScheme(
    primary = Color(0xFFFF80AB),
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFFE91E63).copy(alpha = 0.25f),
    onPrimaryContainer = Color(0xFFFCE4EC),
    secondary = Color(0xFFFFEB3B).copy(alpha = 0.8f),
    secondaryContainer = Color(0xFFE91E63).copy(alpha = 0.2f),
    background = Color(0xFF1A1210),
    surface = Color(0xFF1A1210),
    surfaceVariant = Color(0xFF2A1A18),
    onBackground = Color(0xFFFCE4EC),
    onSurface = Color(0xFFFCE4EC),
    onSurfaceVariant = Color(0xFFFCE4EC).copy(alpha = 0.6f),
    outline = Color(0xFFFF80AB).copy(alpha = 0.15f),
)
private val VividExtraLight = AppExtraColors(
    accent = Color(0xFFE91E63), accentMuted = Color(0xFFFCE4EC),
    cardBg = Color(0xFFFCE4EC).copy(alpha = 0.5f),
    textPrimary = Color(0xFF1A1A1A), textSecondary = Color(0xFF1A1A1A).copy(alpha = 0.5f),
    pageBg = Color(0xFFFFFDE7),
)
private val VividExtraDark = AppExtraColors(
    accent = Color(0xFFFF80AB), accentMuted = Color(0xFFE91E63).copy(alpha = 0.4f),
    cardBg = Color(0xFFE91E63).copy(alpha = 0.2f),
    textPrimary = Color(0xFFFCE4EC), textSecondary = Color(0xFFFCE4EC).copy(alpha = 0.5f),
    pageBg = Color(0xFF1A1210),
)

// ---------- Palette: Mono ----------

private val MonoLight = lightColorScheme(
    primary = Color(0xFF333333),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E8E8),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF9E9E9E),
    secondaryContainer = Color(0xFFF0F0F0),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF1A1A1A).copy(alpha = 0.6f),
    outline = Color(0xFF333333).copy(alpha = 0.12f),
)
private val MonoDark = darkColorScheme(
    primary = Color(0xFFE0E0E0),
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = Color(0xFFE0E0E0),
    secondary = Color(0xFF9E9E9E),
    secondaryContainer = Color(0xFF333333).copy(alpha = 0.5f),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFFE0E0E0).copy(alpha = 0.6f),
    outline = Color(0xFFE0E0E0).copy(alpha = 0.12f),
)
private val MonoExtraLight = AppExtraColors(
    accent = Color(0xFF333333), accentMuted = Color(0xFFE0E0E0),
    cardBg = Color(0xFFE8E8E8).copy(alpha = 0.5f),
    textPrimary = Color(0xFF1A1A1A), textSecondary = Color(0xFF1A1A1A).copy(alpha = 0.5f),
    pageBg = Color(0xFFFFFFFF),
)
private val MonoExtraDark = AppExtraColors(
    accent = Color(0xFFE0E0E0), accentMuted = Color(0xFF333333),
    cardBg = Color(0xFF333333).copy(alpha = 0.4f),
    textPrimary = Color(0xFFE0E0E0), textSecondary = Color(0xFFE0E0E0).copy(alpha = 0.5f),
    pageBg = Color(0xFF121212),
)

// ============================================================
// Resolve palette
// ============================================================

fun resolveColorScheme(palette: String, isDark: Boolean): ColorScheme = when (palette) {
    "vivid" -> if (isDark) VividDark else VividLight
    "mono" -> if (isDark) MonoDark else MonoLight
    else -> if (isDark) WarmDark else WarmLight
}

fun resolveExtraColors(palette: String, isDark: Boolean): AppExtraColors = when (palette) {
    "vivid" -> if (isDark) VividExtraDark else VividExtraLight
    "mono" -> if (isDark) MonoExtraDark else MonoExtraLight
    else -> if (isDark) WarmExtraDark else WarmExtraLight
}

// ============================================================
// Blob Shapes (unchanged)
// ============================================================

object AppShapes {
    val HeroBlob: Shape = GenericShape { size, _ ->
        val w = size.width; val h = size.height
        moveTo(w * 0.3f, 0f)
        cubicTo(w * 0.7f, 0f, w, h * 0.15f, w, h * 0.4f)
        cubicTo(w, h * 0.7f, w * 0.8f, h, w * 0.5f, h)
        cubicTo(w * 0.2f, h, 0f, h * 0.85f, 0f, h * 0.6f)
        cubicTo(0f, h * 0.3f, 0f, 0f, w * 0.3f, 0f)
        close()
    }
    val CardBlob: Shape = GenericShape { size, _ ->
        val w = size.width; val h = size.height
        moveTo(w * 0.05f, h * 0.1f)
        cubicTo(w * 0.15f, -h * 0.02f, w * 0.85f, -h * 0.02f, w * 0.95f, h * 0.1f)
        cubicTo(w * 1.02f, h * 0.3f, w * 1.02f, h * 0.7f, w * 0.95f, h * 0.9f)
        cubicTo(w * 0.85f, h * 1.02f, w * 0.15f, h * 1.02f, w * 0.05f, h * 0.9f)
        cubicTo(-w * 0.02f, h * 0.7f, -w * 0.02f, h * 0.3f, w * 0.05f, h * 0.1f)
        close()
    }
    val PillBlob: Shape = GenericShape { size, _ ->
        val w = size.width; val h = size.height
        val r = h / 2
        moveTo(r, 0f); lineTo(w - r, 0f)
        cubicTo(w, 0f, w, h, w - r, h); lineTo(r, h)
        cubicTo(0f, h, 0f, 0f, r, 0f); close()
    }
}

// ============================================================
// Typography (unchanged)
// ============================================================

private val AppTypography = Typography(
    headlineLarge = Typography().headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 56.sp, letterSpacing = (-2).sp),
    headlineMedium = Typography().headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 36.sp, letterSpacing = (-1).sp),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = Typography().bodyLarge.copy(fontWeight = FontWeight.Normal),
    labelMedium = Typography().labelMedium.copy(fontWeight = FontWeight.SemiBold),
)

// ============================================================
// Theme composable
// ============================================================

@Composable
fun AppTheme(
    palette: String = "warm",
    darkMode: String = "system", // "system" | "light" | "dark"
    content: @Composable () -> Unit
) {
    val isDark = when (darkMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = resolveColorScheme(palette, isDark)
    val extraColors = resolveExtraColors(palette, isDark)

    CompositionLocalProvider(LocalAppExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
