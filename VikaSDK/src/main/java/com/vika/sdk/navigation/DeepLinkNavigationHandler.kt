package com.vika.sdk.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.vika.sdk.models.NavigationType
import com.vika.sdk.models.ScreenRegistration

/**
 * Handles deep link navigation with security validation.
 *
 * Validates deep links against:
 * - Allowed URI schemes (whitelist)
 * - Registered screens in the SDK
 *
 * @param context Application context
 * @param allowedSchemes List of allowed URI schemes (e.g., "myapp", "sample")
 * @param registeredScreensProvider Function to get currently registered screens
 */
class DeepLinkNavigationHandler(
    private val context: Context,
    private val allowedSchemes: List<String> = emptyList(),
    private val registeredScreensProvider: () -> List<ScreenRegistration> = { emptyList() }
) : NavigationHandler {

    companion object {
        private const val TAG = "DeepLinkNavHandler"
    }

    /**
     * Navigate to a deep link after validation.
     *
     * @param type Navigation type containing the deep link URI
     * @param context Context to use for navigation
     */
    override fun navigate(type: NavigationType, context: Context) {
        if (type !is NavigationType.DeepLink) {
            Log.e(TAG, "Invalid navigation type for DeepLinkNavigationHandler")
            return
        }

        val uri = type.uri

        // Validate deep link
        val validationResult = validateDeepLink(uri)
        if (!validationResult.isValid) {
            Log.e(TAG, "Deep link validation failed: ${validationResult.error}")
            return
        }

        try {
            val parsedUri = Uri.parse(uri)
            val intent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Log.d(TAG, "Successfully navigated to: $uri")
            } else {
                Log.e(TAG, "No activity found to handle deep link: $uri")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate to deep link", e)
        }
    }

    override fun canHandle(type: NavigationType): Boolean {
        return type is NavigationType.DeepLink
    }

    /**
     * Validate a deep link against security rules.
     *
     * @param uri Deep link URI to validate
     * @return Validation result
     */
    private fun validateDeepLink(uri: String): ValidationResult {
        // Check for empty URI
        if (uri.isBlank()) {
            return ValidationResult(false, "Deep link URI is empty")
        }

        // Parse URI
        val parsedUri = try {
            Uri.parse(uri)
        } catch (e: Exception) {
            return ValidationResult(false, "Invalid URI format: ${e.message}")
        }

        // Validate scheme against whitelist (if whitelist is configured)
        if (allowedSchemes.isNotEmpty()) {
            val scheme = parsedUri.scheme ?: ""
            if (scheme !in allowedSchemes) {
                return ValidationResult(
                    false,
                    "URI scheme '$scheme' is not in allowed list: $allowedSchemes"
                )
            }
        }

        // Validate against registered screens
        val registeredScreens = registeredScreensProvider()
        if (registeredScreens.isNotEmpty()) {
            val isRegistered = registeredScreens.any { screen ->
                screen.deepLink == uri || uri.startsWith(screen.deepLink)
            }
            if (!isRegistered) {
                return ValidationResult(
                    false,
                    "Deep link '$uri' does not match any registered screen"
                )
            }
        }

        return ValidationResult(true, null)
    }

    /**
     * Result of deep link validation.
     */
    private data class ValidationResult(
        val isValid: Boolean,
        val error: String?
    )
}
