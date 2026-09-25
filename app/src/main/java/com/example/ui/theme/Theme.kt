package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NexusCyan,
    onPrimary = Color.Black,
    primaryContainer = NexusCyanDim,
    onPrimaryContainer = Color.White,
    secondary = NexusViolet,
    onSecondary = Color.Black,
    tertiary = NexusEmerald,
    onTertiary = Color.Black,
    background = NexusBgDark,
    onBackground = NexusTextLight,
    surface = NexusSurfaceDark,
    onSurface = NexusTextLight,
    surfaceVariant = NexusCardDark,
    onSurfaceVariant = NexusTextMuted,
    outline = NexusBorderDark
)

private val LightColorScheme = DarkColorScheme // NEXUS is designed with a sci-fi dark universe aesthetic

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
