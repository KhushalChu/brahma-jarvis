package com.brahma.jarvis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrahmaCyan = Color(0xFF00E5FF)
private val BrahmaDarkBg = Color(0xFF0B1220)
private val BrahmaSurface = Color(0xFF141C2E)

private val DarkColors = darkColorScheme(
    primary = BrahmaCyan,
    background = BrahmaDarkBg,
    surface = BrahmaSurface,
    onPrimary = Color.Black,
    onBackground = Color(0xFFE6F1FF),
    onSurface = Color(0xFFE6F1FF)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0091A8),
    background = Color(0xFFF5F9FF),
    surface = Color.White
)

@Composable
fun BrahmaJarvisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
