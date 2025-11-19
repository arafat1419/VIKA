package com.vika.sdk.navigation

import android.content.Context
import com.vika.sdk.models.NavigationType

/**
 * Interface for handling navigation to different screen types.
 *
 * Implement this interface to create custom navigation handlers for
 * different navigation mechanisms (deep links, activities, fragments, etc.).
 *
 * ## Example Implementation
 * ```kotlin
 * class MyCustomHandler : NavigationHandler {
 *     override fun navigate(type: NavigationType, context: Context) {
 *         if (type is NavigationType.DeepLink) {
 *             // Handle navigation
 *         }
 *     }
 *
 *     override fun canHandle(type: NavigationType): Boolean {
 *         return type is NavigationType.DeepLink
 *     }
 * }
 * ```
 *
 * @see NavigationType
 * @see DeepLinkNavigationHandler
 */
interface NavigationHandler {
    /**
     * Navigate to the specified destination.
     *
     * @param type Navigation type containing destination information
     * @param context Android context for starting activities/intents
     */
    fun navigate(type: NavigationType, context: Context)

    /**
     * Check if this handler can handle the given navigation type.
     *
     * @param type Navigation type to check
     * @return True if this handler can process the navigation type
     */
    fun canHandle(type: NavigationType): Boolean
}
