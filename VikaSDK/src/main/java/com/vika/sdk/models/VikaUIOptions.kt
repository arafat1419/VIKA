package com.vika.sdk.models

import androidx.annotation.DrawableRes

/**
 * UI options for customizing the VIKA SDK appearance and behavior.
 *
 * @property displayMode How the SDK UI should be displayed (fullscreen, dialog, or bottom sheet)
 * @property appLogoResId Drawable resource ID for the app logo to display at top-left
 * @property appTitle Title text to display next to the app logo
 * @property themeConfig Custom theme colors for the UI
 * @property dismissOnTouchOutside Whether to dismiss dialog/bottom sheet when touching outside (only applies to DIALOG and BOTTOM_SHEET modes)
 */
data class VikaUIOptions(
    val displayMode: VikaDisplayMode = VikaDisplayMode.FULLSCREEN,
    @DrawableRes val appLogoResId: Int? = null,
    val appTitle: String? = null,
    val themeConfig: VikaThemeConfig? = null,
    val dismissOnTouchOutside: Boolean = true
) {
    /**
     * Builder class for constructing VikaUIOptions with a fluent API.
     */
    class Builder {
        private var displayMode: VikaDisplayMode = VikaDisplayMode.FULLSCREEN
        private var appLogoResId: Int? = null
        private var appTitle: String? = null
        private var themeConfig: VikaThemeConfig? = null
        private var dismissOnTouchOutside: Boolean = true

        /**
         * Sets the display mode for the SDK UI.
         *
         * @param mode Display mode (FULLSCREEN, DIALOG, or BOTTOM_SHEET)
         * @return This builder instance
         */
        fun displayMode(mode: VikaDisplayMode) = apply { this.displayMode = mode }

        /**
         * Sets the app logo drawable resource.
         *
         * @param resId Drawable resource ID for the logo
         * @return This builder instance
         */
        fun appLogo(@DrawableRes resId: Int) = apply { this.appLogoResId = resId }

        /**
         * Sets the app title to display.
         *
         * @param title Title text
         * @return This builder instance
         */
        fun appTitle(title: String) = apply { this.appTitle = title }

        /**
         * Sets the custom theme configuration.
         *
         * @param config Theme configuration with custom colors
         * @return This builder instance
         */
        fun theme(config: VikaThemeConfig) = apply { this.themeConfig = config }

        /**
         * Sets whether the dialog/bottom sheet can be dismissed by touching outside.
         *
         * @param dismissible True to allow dismissing by touching outside
         * @return This builder instance
         */
        fun dismissOnTouchOutside(dismissible: Boolean) =
            apply { this.dismissOnTouchOutside = dismissible }

        /**
         * Builds the VikaUIOptions instance.
         *
         * @return Configured VikaUIOptions
         */
        fun build() = VikaUIOptions(
            displayMode = displayMode,
            appLogoResId = appLogoResId,
            appTitle = appTitle,
            themeConfig = themeConfig,
            dismissOnTouchOutside = dismissOnTouchOutside
        )
    }

    companion object {
        /**
         * Creates a new Builder instance for constructing VikaUIOptions.
         *
         * @return New Builder instance
         */
        fun builder() = Builder()
    }
}
