package com.vika.sdk.callbacks

/**
 * Navigation error types
 */
sealed class NavigationError {
    data class InvalidQuery(val message: String) : NavigationError()
    data class LowConfidence(val confidence: Float) : NavigationError()
    data class ScreenNotFound(val screenId: String) : NavigationError()
    data class InvalidParameters(val parameters: Map<String, Any>) : NavigationError()
    data class NetworkError(val exception: Exception) : NavigationError()
    data class SecurityError(val message: String) : NavigationError()
    data class InvalidDeepLink(val deepLink: String) : NavigationError()
    data class UnknownError(val exception: Exception) : NavigationError()
    data class Timeout(val timeoutMillis: Long) : NavigationError()
    data class RateLimited(val retryAfter: Long?) : NavigationError()

    /**
     * Get user-friendly error message
     */
    fun getUserMessage(): String {
        return when (this) {
            is InvalidQuery -> "Invalid search query: $message"
            is LowConfidence -> "Could not understand your request clearly"
            is ScreenNotFound -> "The requested screen was not found"
            is InvalidParameters -> "Invalid navigation parameters"
            is NetworkError -> "Network connection error. Please try again."
            is SecurityError -> "Security error occurred"
            is InvalidDeepLink -> "Invalid navigation link"
            is UnknownError -> "An unexpected error occurred"
            is Timeout -> "Request timed out. Please try again."
            is RateLimited -> "Too many requests. Please wait and try again."
        }
    }
}