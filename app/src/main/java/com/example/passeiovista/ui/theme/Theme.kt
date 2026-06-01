package com.example.passeiovista.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlueLight,
    primaryContainer = Color(0xFF003A5A),
    secondary = BrandGreen,
    secondaryContainer = Color(0xFF103B27),
    tertiary = BrandOrange,
    tertiaryContainer = Color(0xFF4A3200),
    background = Color(0xFF06121E),
    surface = Color(0xFF06121E),
    onPrimary = Color.White,
    onPrimaryContainer = Color(0xFFE6EEF5),
    onSecondary = Color.White,
    onSecondaryContainer = Color(0xFFE6EEF5),
    onTertiary = Color(0xFF1A1200),
    onTertiaryContainer = Color(0xFFFFF3D6),
    onBackground = Color(0xFFE6EEF5),
    onSurface = Color(0xFFE6EEF5),
    surfaceVariant = Color(0xFF102233),
    onSurfaceVariant = Color(0xFFBDD1E0),
    outline = Color(0xFF5F7C92),
    surfaceTint = BrandBlueLight,
    surfaceContainerLowest = Color(0xFF040B12),
    surfaceContainerLow = Color(0xFF06121E),
    surfaceContainer = Color(0xFF071626),
    surfaceContainerHigh = Color(0xFF0A1B2E),
    surfaceContainerHighest = Color(0xFF0D2035)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    primaryContainer = BrandBlueContainer,
    secondary = BrandGreen,
    secondaryContainer = BrandGreenContainer,
    tertiary = BrandOrange,
    tertiaryContainer = BrandOrangeContainer,
    background = BrandSurface,
    surface = BrandSurface,
    onPrimary = Color.White,
    onPrimaryContainer = BrandOnSurface,
    onSecondary = Color.White,
    onSecondaryContainer = BrandOnSurface,
    onTertiary = Color(0xFF1A1200),
    onTertiaryContainer = Color(0xFF1A1200),
    onBackground = BrandOnSurface,
    onSurface = BrandOnSurface,
    surfaceVariant = BrandSurfaceVariant,
    onSurfaceVariant = BrandOnSurface,
    outline = BrandOutline,
    surfaceTint = BrandBlue,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = BrandSurface,
    surfaceContainer = BrandSurfaceContainer,
    surfaceContainerHigh = Color(0xFFE6F0F9),
    surfaceContainerHighest = Color(0xFFDEEAF5)
)

@Composable
fun PasseioÀVistaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
