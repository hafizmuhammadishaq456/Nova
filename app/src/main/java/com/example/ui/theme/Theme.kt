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
    primary = NovaCyan,
    onPrimary = NovaBackground,
    primaryContainer = NovaSurfaceElevated,
    onPrimaryContainer = NovaMint,
    secondary = NovaMint,
    onSecondary = NovaBackground,
    secondaryContainer = NovaSurface,
    onSecondaryContainer = NovaMint,
    tertiary = NovaViolet,
    background = NovaBackground,
    onBackground = TextPrimary,
    surface = NovaSurface,
    onSurface = TextPrimary,
    surfaceVariant = NovaSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = NovaBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Nova always prefers futuristic dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
