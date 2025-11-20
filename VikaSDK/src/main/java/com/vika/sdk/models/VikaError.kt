package com.vika.sdk.models

/**
 * SDK error types with detailed information.
 *
 * Provides specific error types for different failure scenarios,
 * enabling precise error handling in client applications.
 */
sealed class VikaError {
    /** Error message */
    abstract val message: String

    /** Original exception, if any */
    abstract val cause: Throwable?

    /**
     * SDK not initialized error.
     */
    data class NotInitialized(
        override val message: String = "SDK not initialized. Call VikaSDK.initialize() first.",
        override val cause: Throwable? = null
    ) : VikaError()

    /**
     * Invalid configuration error.
     */
    data class InvalidConfiguration(
        override val message: String,
        override val cause: Throwable? = null
    ) : VikaError()

    /**
     * Network error.
     */
    data class Network(
        override val message: String,
        override val cause: Throwable? = null
    ) : VikaError()

    /**
     * Authentication error.
     */
    data class Authentication(
        override val message: String = "Authentication failed. Check your API key.",
        override val cause: Throwable? = null
    ) : VikaError()

    /**
     * Rate limiting error.
     */
    data class RateLimited(
        override val message: String = "Too many requests. Please wait before trying again.",
        override val cause: Throwable? = null
    ) : VikaError()

    /**
     * Security error.
     */
    data class Security(
        override val message: String,
        override val cause: Throwable? = null
    ) : VikaError()

    /**
     * Invalid query error.
     */
    data class InvalidQuery(
        override val message: String,
        override val cause: Throwable? = null
    ) : VikaError()

    /**
     * Low confidence error.
     */
    data class LowConfidence(
        val confidence: Float,
        override val message: String = "Navigation confidence too low: $confidence",
        override val cause: Throwable? = null
    ) : VikaError()

    /**
     * Screen not found error.
     */
    data class ScreenNotFound(
        val screenId: String,
        override val message: String = "Screen not registered: $screenId",
        override val cause: Throwable? = null
    ) : VikaError()

    /**
     * Socket connection error.
     */
    data class SocketConnection(
        override val message: String = "Socket connection failed",
        override val cause: Throwable? = null
    ) : VikaError()

    /**
     * Conversation processing failed error.
     */
    data class ConversationFailed(
        val conversationId: String,
        override val message: String = "Conversation processing failed",
        override val cause: Throwable? = null
    ) : VikaError()

    /**
     * Unknown error.
     */
    data class Unknown(
        override val message: String = "An unknown error occurred",
        override val cause: Throwable? = null
    ) : VikaError()
}
