package com.vika.sdk.models

import androidx.compose.ui.graphics.Color

/**
 * Configuration for customizing the VIKA SDK UI theme colors.
 *
 * @property primaryColor Primary color used for main buttons and highlights
 * @property secondaryColor Secondary/accent color for secondary elements
 * @property backgroundColor Background color for the screen
 * @property textColor Primary text color
 * @property surfaceColor Color for surface elements like cards
 * @property waveformColor Color for the audio waveform visualization
 */
data class VikaThemeConfig(
    val primaryColor: Long? = null,
    val secondaryColor: Long? = null,
    val backgroundColor: Long? = null,
    val textColor: Long? = null,
    val surfaceColor: Long? = null,
    val waveformColor: Long? = null
) {
    companion object {
        /**
         * Default dark theme configuration.
         */
        val DEFAULT_DARK = VikaThemeConfig(
            primaryColor = 0xFFFFFFFF,
            secondaryColor = 0xFFFF4444,
            backgroundColor = 0xFF000000,
            textColor = 0xFFFFFFFF,
            surfaceColor = 0xFF1A1A1A,
            waveformColor = 0xFFFFFFFF
        )

        /**
         * Default light theme configuration.
         */
        val DEFAULT_LIGHT = VikaThemeConfig(
            primaryColor = 0xFF000000,
            secondaryColor = 0xFFE53935,
            backgroundColor = 0xFFFFFFFF,
            textColor = 0xFF000000,
            surfaceColor = 0xFFF5F5F5,
            waveformColor = 0xFF000000
        )
    }

    /**
     * Converts the theme configuration to Compose Color objects.
     *
     * @return VikaThemeColors with resolved colors
     */
    internal fun toColors(): VikaThemeColors {
        val defaults = DEFAULT_DARK
        return VikaThemeColors(
            primary = Color(primaryColor ?: defaults.primaryColor!!),
            secondary = Color(secondaryColor ?: defaults.secondaryColor!!),
            background = Color(backgroundColor ?: defaults.backgroundColor!!),
            text = Color(textColor ?: defaults.textColor!!),
            surface = Color(surfaceColor ?: defaults.surfaceColor!!),
            waveform = Color(waveformColor ?: defaults.waveformColor!!)
        )
    }
}

/**
 * Internal representation of theme colors as Compose Color objects.
 */
internal data class VikaThemeColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val text: Color,
    val surface: Color,
    val waveform: Color
)
