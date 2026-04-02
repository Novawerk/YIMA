package com.haodong.yimalaile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Primary = Color(0xFF8B5E5E)
private val OnPrimary = Color.White
private val PrimaryContainer = Color(0xFFF2D5C4)
private val OnPrimaryContainer = Color(0xFF3B1F1F)
private val Secondary = Color(0xFFD4A373)
private val SecondaryContainer = Color(0xFFFFE0C8)
private val Background = Color(0xFFFDF8F5)
private val Surface = Color.White
private val Error = Color(0xFFBA1A1A)

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    secondaryContainer = SecondaryContainer,
    background = Background,
    surface = Surface,
    error = Error,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE0B0A0),
    onPrimary = Color(0xFF3B1F1F),
    primaryContainer = Color(0xFF5C3A3A),
    onPrimaryContainer = Color(0xFFF2D5C4),
    secondary = Color(0xFFD4A373),
    secondaryContainer = Color(0xFF5C3A2A),
    background = Color(0xFF1A1110),
    surface = Color(0xFF1A1110),
    error = Color(0xFFFFB4AB),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
