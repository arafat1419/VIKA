package com.arafat1419.vika.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JKNDarkColorScheme = darkColorScheme(
    primary = JKNGreen,
    onPrimary = JKNTextOnPrimary,
    primaryContainer = JKNGreenDark,
    onPrimaryContainer = JKNTextOnPrimary,
    secondary = JKNBlue,
    onSecondary = JKNTextOnPrimary,
    tertiary = JKNOrange,
    onTertiary = JKNTextOnPrimary,
    background = JKNBackgroundDark,
    onBackground = Color(0xFFE0E0E0),
    surface = JKNSurfaceDark,
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF3C3C3C),
    onSurfaceVariant = Color(0xFFBDBDBD),
    error = JKNError,
    onError = JKNTextOnPrimary,
    outline = JKNBorder,
    outlineVariant = JKNDivider
)

private val JKNLightColorScheme = lightColorScheme(
    primary = JKNGreen,
    onPrimary = JKNTextOnPrimary,
    primaryContainer = JKNGreenLight,
    onPrimaryContainer = JKNTextPrimary,
    secondary = JKNBlue,
    onSecondary = JKNTextOnPrimary,
    tertiary = JKNOrange,
    onTertiary = JKNTextOnPrimary,
    background = JKNBackgroundLight,
    onBackground = JKNTextPrimary,
    surface = JKNSurfaceLight,
    onSurface = JKNTextPrimary,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = JKNTextSecondary,
    error = JKNError,
    onError = JKNTextOnPrimary,
    outline = JKNBorder,
    outlineVariant = JKNDivider
)

@Composable
fun VIKATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        JKNDarkColorScheme
    } else {
        JKNLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
