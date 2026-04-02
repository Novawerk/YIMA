package com.haodong.yimalaile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.GenericShape
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

// ---------- Colors ----------

object AppColors {
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

private val LightColors = lightColorScheme(
    primary = AppColors.DeepRose,
    onPrimary = Color.White,
    primaryContainer = AppColors.BlushPink,
    onPrimaryContainer = AppColors.DarkCoffee,
    secondary = AppColors.WarmPeach,
    secondaryContainer = AppColors.WarmPeach.copy(alpha = 0.5f),
    background = AppColors.SoftCream,
    surface = AppColors.SoftCream,
    surfaceVariant = AppColors.WarmCreamBeige,
    onBackground = AppColors.DarkCoffee,
    onSurface = AppColors.DarkCoffee,
    onSurfaceVariant = AppColors.DarkCoffee.copy(alpha = 0.6f),
    outline = AppColors.DeepRose.copy(alpha = 0.15f),
)

private val DarkColors = darkColorScheme(
    primary = AppColors.WarmPeach,
    onPrimary = AppColors.DarkChocolate,
    primaryContainer = AppColors.DeepRose.copy(alpha = 0.3f),
    onPrimaryContainer = AppColors.WarmPeach,
    secondary = AppColors.WarmPeach.copy(alpha = 0.7f),
    secondaryContainer = AppColors.DeepRose.copy(alpha = 0.2f),
    background = AppColors.DarkChocolate,
    surface = AppColors.DarkChocolate,
    surfaceVariant = AppColors.MidnightEspresso,
    onBackground = AppColors.WarmPeach,
    onSurface = AppColors.BlushPink,
    onSurfaceVariant = AppColors.BlushPink.copy(alpha = 0.6f),
    outline = AppColors.WarmPeach.copy(alpha = 0.15f),
)

// ---------- Blob Shapes ----------

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
        moveTo(r, 0f)
        lineTo(w - r, 0f)
        cubicTo(w, 0f, w, h, w - r, h)
        lineTo(r, h)
        cubicTo(0f, h, 0f, 0f, r, 0f)
        close()
    }
}

// ---------- Typography ----------

private val AppTypography = Typography(
    headlineLarge = Typography().headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        letterSpacing = (-2).sp,
    ),
    headlineMedium = Typography().headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = (-1).sp,
    ),
    titleLarge = Typography().titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = Typography().titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = Typography().bodyLarge.copy(
        fontWeight = FontWeight.Normal,
    ),
    labelMedium = Typography().labelMedium.copy(
        fontWeight = FontWeight.SemiBold,
    ),
)

// ---------- Composition Locals ----------

data class AppExtraColors(
    val warmPeach: Color = AppColors.WarmPeach,
    val blushPink: Color = AppColors.BlushPink,
    val deepRose: Color = AppColors.DeepRose,
    val darkCoffee: Color = AppColors.DarkCoffee,
    val sageGreen: Color = AppColors.SageGreen,
)

val LocalAppExtraColors = staticCompositionLocalOf { AppExtraColors() }

// ---------- Theme ----------

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppExtraColors provides AppExtraColors()) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            content = content,
        )
    }
}
