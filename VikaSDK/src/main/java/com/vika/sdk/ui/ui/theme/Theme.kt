package com.vika.sdk.ui.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.vika.sdk.models.VikaThemeColors
import com.vika.sdk.models.VikaThemeConfig

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Default VIKA theme colors for dark mode.
 */
internal val DefaultVikaColors = VikaThemeColors(
    primary = Color.White,
    secondary = Color(0xFFFF4444),
    background = Color.Black,
    text = Color.White,
    surface = Color(0xFF1A1A1A),
    waveform = Color.White
)

/**
 * CompositionLocal to provide custom VIKA theme colors throughout the UI.
 */
internal val LocalVikaColors = staticCompositionLocalOf { DefaultVikaColors }

@Composable
internal fun VIKATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

/**
 * Custom VIKA theme with configurable colors.
 *
 * @param themeConfig Custom theme configuration with colors
 * @param content Composable content to wrap
 */
@Composable
internal fun VikaCustomTheme(
    themeConfig: VikaThemeConfig?,
    content: @Composable () -> Unit
) {
    val vikaColors = themeConfig?.toColors() ?: DefaultVikaColors

    CompositionLocalProvider(LocalVikaColors provides vikaColors) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = vikaColors.primary,
                secondary = vikaColors.secondary,
                background = vikaColors.background,
                surface = vikaColors.surface,
                onPrimary = vikaColors.background,
                onSecondary = vikaColors.background,
                onBackground = vikaColors.text,
                onSurface = vikaColors.text
            ),
            typography = Typography,
            content = content
        )
    }
}

/**
 * Access the current VIKA theme colors.
 */
internal object VikaTheme {
    val colors: VikaThemeColors
        @Composable
        get() = LocalVikaColors.current
}
