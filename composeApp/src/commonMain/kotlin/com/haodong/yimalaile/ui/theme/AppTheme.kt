package com.haodong.yimalaile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key.Companion.M
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.comfortaa_regular

@Composable
private fun appTypography(): Typography {
    val comfortaa = FontFamily(
        Font(Res.font.comfortaa_regular, FontWeight.Light),
        Font(Res.font.comfortaa_regular, FontWeight.Normal),
        Font(Res.font.comfortaa_regular, FontWeight.Medium),
        Font(Res.font.comfortaa_regular, FontWeight.SemiBold),
        Font(Res.font.comfortaa_regular, FontWeight.Bold),
    )
    val d = Typography()
    return Typography(
        displayLarge = d.displayLarge.copy(fontFamily = comfortaa),
        displayMedium = d.displayMedium.copy(fontFamily = comfortaa),
        displaySmall = d.displaySmall.copy(fontFamily = comfortaa),
        headlineLarge = d.headlineLarge.copy(fontFamily = comfortaa, fontWeight = FontWeight.Bold, fontSize = 56.sp, letterSpacing = (-2).sp),
        headlineMedium = d.headlineMedium.copy(fontFamily = comfortaa, fontWeight = FontWeight.Bold, fontSize = 36.sp, letterSpacing = (-1).sp),
        headlineSmall = d.headlineSmall.copy(fontFamily = comfortaa),
        titleLarge = d.titleLarge.copy(fontFamily = comfortaa, fontWeight = FontWeight.SemiBold),
        titleMedium = d.titleMedium.copy(fontFamily = comfortaa, fontWeight = FontWeight.SemiBold),
        titleSmall = d.titleSmall.copy(fontFamily = comfortaa),
        bodyLarge = d.bodyLarge.copy(fontFamily = comfortaa),
        bodyMedium = d.bodyMedium.copy(fontFamily = comfortaa),
        bodySmall = d.bodySmall.copy(fontFamily = comfortaa),
        labelLarge = d.labelLarge.copy(fontFamily = comfortaa),
        labelMedium = d.labelMedium.copy(fontFamily = comfortaa, fontWeight = FontWeight.SemiBold),
        labelSmall = d.labelSmall.copy(fontFamily = comfortaa),
    )
}

// ============================================================
// Theme composable
// ============================================================

@Composable
fun AppTheme(
    darkMode: String = "system", // "system" | "light" | "dark"
    content: @Composable () -> Unit
) {


    val isDark = when (darkMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }



    MaterialTheme(
            colorScheme = colorScheme,
            typography = appTypography(),
            content = content,
        )

}
