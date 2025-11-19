package com.vika.sdk.models

/**
 * Represents a screen that can be navigated to via voice commands.
 *
 * Register screens with the SDK to enable voice-based navigation:
 * ```kotlin
 * val screen = ScreenRegistration(
 *     screenId = "product_detail",
 *     screenName = "Product Detail",
 *     description = "Shows detailed product information with images and reviews",
 *     deepLink = "myapp://product/{id}",
 *     navigationType = NavigationType.DeepLink("myapp://product/{id}"),
 *     keywords = listOf("product", "item", "details", "info")
 * )
 *
 * VikaSDK.getInstance().registerScreen(screen)
 * ```
 *
 * @property screenId Unique identifier for this screen
 * @property screenName Human-readable display name
 * @property description Detailed description to help AI understand when to navigate here
 * @property deepLink Deep link URI for navigation
 * @property navigationType Navigation mechanism to use
 * @property keywords Additional keywords to improve voice matching accuracy
 * @property customNavigationHandler Optional custom handler for complex navigation scenarios
 *
 * @see NavigationType
 * @see VikaSDK.registerScreen
 */
data class ScreenRegistration(
    val screenId: String,
    val screenName: String,
    val description: String,
    val deepLink: String,
    val navigationType: NavigationType,
    val keywords: List<String> = emptyList(),
    val customNavigationHandler: ((NavigationResult) -> Unit)? = null
) {
    /**
     * Convert to secure screen representation for API transmission.
     *
     * Excludes sensitive data like custom handlers.
     *
     * @return Secure screen for API communication
     */
    internal fun toSecureScreen(): SecureScreen {
        return SecureScreen(
            screenId = screenId,
            screenName = screenName,
            description = description,
            deepLink = deepLink,
            keywords = keywords
        )
    }
}

/**
 * Secure screen representation for API transmission.
 *
 * Contains only the data needed by the backend for navigation matching.
 *
 * @property screenId Unique identifier
 * @property screenName Display name
 * @property description Screen description
 * @property deepLink Navigation deep link
 * @property keywords Matching keywords
 */
internal data class SecureScreen(
    val screenId: String,
    val screenName: String,
    val description: String,
    val deepLink: String,
    val keywords: List<String>
)
