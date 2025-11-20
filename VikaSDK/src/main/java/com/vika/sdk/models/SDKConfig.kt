package com.vika.sdk.models

/**
 * Configuration for VIKA Navigation SDK.
 *
 * Use [Builder] to create an instance with custom settings:
 * ```kotlin
 * val config = SDKConfig.Builder("your-api-key")
 *     .minConfidenceThreshold(0.8f)
 *     .debugMode(true)
 *     .timeout(15000)
 *     .certificatePinning("sha256/AAAA...")
 *     .build()
 * ```
 *
 * @property apiKey API key for authentication with VIKA backend
 * @property minConfidenceThreshold Minimum confidence score (0.0-1.0) required for navigation
 * @property analyticsEnabled Whether to track navigation analytics
 * @property debugMode Enable debug logging (disable in production)
 * @property timeoutMillis Network request timeout in milliseconds
 * @property maxRetries Maximum retry attempts for failed requests
 * @property cacheEnabled Enable local caching of screen registrations
 * @property certificatePinning Certificate pinning configuration for enhanced security
 * @property allowedDeepLinkSchemes Whitelist of allowed deep link schemes
 * @property language Language for the SDK UI (English or Indonesian)
 * @property socketReconnectionAttempts Maximum number of socket reconnection attempts
 * @property socketReconnectionDelay Initial delay between reconnection attempts in milliseconds
 */
class SDKConfig private constructor(
    val apiKey: String,
    val minConfidenceThreshold: Float,
    val analyticsEnabled: Boolean,
    val debugMode: Boolean,
    val timeoutMillis: Long,
    val maxRetries: Int,
    val cacheEnabled: Boolean,
    val certificatePinning: CertificatePinningConfig?,
    val allowedDeepLinkSchemes: List<String>,
    val language: VikaLanguage,
    val socketReconnectionAttempts: Int,
    val socketReconnectionDelay: Long
) {
    companion object {
        /** Default minimum confidence threshold */
        const val DEFAULT_CONFIDENCE_THRESHOLD = 0.7f

        /** Default network timeout in milliseconds */
        const val DEFAULT_TIMEOUT_MS = 10000L

        /** Default maximum retry attempts */
        const val DEFAULT_MAX_RETRIES = 3

        /** Default socket reconnection attempts */
        const val DEFAULT_SOCKET_RECONNECTION_ATTEMPTS = 5

        /** Default socket reconnection delay in milliseconds */
        const val DEFAULT_SOCKET_RECONNECTION_DELAY = 1000L
    }

    /**
     * Builder for creating [SDKConfig] instances.
     *
     * @param apiKey Required API key for VIKA backend authentication
     */
    class Builder(private val apiKey: String) {
        private var minConfidenceThreshold: Float = DEFAULT_CONFIDENCE_THRESHOLD
        private var analyticsEnabled: Boolean = true
        private var debugMode: Boolean = false
        private var timeoutMillis: Long = DEFAULT_TIMEOUT_MS
        private var maxRetries: Int = DEFAULT_MAX_RETRIES
        private var cacheEnabled: Boolean = true
        private var certificatePinning: CertificatePinningConfig? = null
        private var allowedDeepLinkSchemes: MutableList<String> = mutableListOf()
        private var language: VikaLanguage = VikaLanguage.ENGLISH
        private var socketReconnectionAttempts: Int = DEFAULT_SOCKET_RECONNECTION_ATTEMPTS
        private var socketReconnectionDelay: Long = DEFAULT_SOCKET_RECONNECTION_DELAY

        /**
         * Set minimum confidence threshold for navigation.
         *
         * @param threshold Value between 0.0 and 1.0
         * @return This builder for chaining
         * @throws IllegalArgumentException if threshold is not in valid range
         */
        fun minConfidenceThreshold(threshold: Float) = apply {
            require(threshold in 0f..1f) { "Confidence threshold must be between 0.0 and 1.0" }
            this.minConfidenceThreshold = threshold
        }

        /**
         * Enable or disable analytics tracking.
         *
         * @param enabled True to enable analytics
         * @return This builder for chaining
         */
        fun analyticsEnabled(enabled: Boolean) = apply {
            this.analyticsEnabled = enabled
        }

        /**
         * Enable or disable debug mode.
         *
         * Warning: Debug mode logs additional information. Disable in production.
         *
         * @param enabled True to enable debug logging
         * @return This builder for chaining
         */
        fun debugMode(enabled: Boolean) = apply {
            this.debugMode = enabled
        }

        /**
         * Set network request timeout.
         *
         * @param millis Timeout in milliseconds (must be positive)
         * @return This builder for chaining
         * @throws IllegalArgumentException if timeout is not positive
         */
        fun timeout(millis: Long) = apply {
            require(millis > 0) { "Timeout must be positive" }
            this.timeoutMillis = millis
        }

        /**
         * Set maximum retry attempts for failed requests.
         *
         * @param retries Number of retries (0 = no retries)
         * @return This builder for chaining
         * @throws IllegalArgumentException if retries is negative
         */
        fun maxRetries(retries: Int) = apply {
            require(retries >= 0) { "Max retries must be non-negative" }
            this.maxRetries = retries
        }

        /**
         * Enable or disable local caching.
         *
         * @param enabled True to enable caching
         * @return This builder for chaining
         */
        fun cacheEnabled(enabled: Boolean) = apply {
            this.cacheEnabled = enabled
        }

        /**
         * Configure certificate pinning for enhanced security.
         *
         * @param certificates SHA-256 certificate hashes (e.g., "sha256/AAAA...")
         * @return This builder for chaining
         * @throws IllegalArgumentException if no certificates provided or invalid format
         */
        fun certificatePinning(vararg certificates: String) = apply {
            require(certificates.isNotEmpty()) { "At least one certificate is required" }
            certificates.forEach { cert ->
                require(cert.startsWith("sha256/")) {
                    "Certificate must start with 'sha256/': $cert"
                }
            }
            this.certificatePinning = CertificatePinningConfig(
                enabled = true,
                certificates = certificates.toList()
            )
        }

        /**
         * Add allowed deep link schemes for navigation security.
         *
         * Only deep links with these schemes will be executed.
         *
         * @param schemes Allowed URI schemes (e.g., "myapp", "sample")
         * @return This builder for chaining
         */
        fun allowedDeepLinkSchemes(vararg schemes: String) = apply {
            this.allowedDeepLinkSchemes.addAll(schemes)
        }

        /**
         * Set the language for the SDK UI.
         *
         * @param language Language to use (ENGLISH or INDONESIAN)
         * @return This builder for chaining
         */
        fun language(language: VikaLanguage) = apply {
            this.language = language
        }

        /**
         * Set socket reconnection attempts.
         *
         * @param attempts Maximum number of reconnection attempts (0 = no retries)
         * @return This builder for chaining
         * @throws IllegalArgumentException if attempts is negative
         */
        fun socketReconnectionAttempts(attempts: Int) = apply {
            require(attempts >= 0) { "Socket reconnection attempts must be non-negative" }
            this.socketReconnectionAttempts = attempts
        }

        /**
         * Set socket reconnection delay.
         *
         * @param millis Initial delay between reconnection attempts in milliseconds
         * @return This builder for chaining
         * @throws IllegalArgumentException if delay is not positive
         */
        fun socketReconnectionDelay(millis: Long) = apply {
            require(millis > 0) { "Socket reconnection delay must be positive" }
            this.socketReconnectionDelay = millis
        }

        /**
         * Build the [SDKConfig] instance.
         *
         * @return Configured SDKConfig
         * @throws IllegalArgumentException if configuration is invalid
         */
        fun build(): SDKConfig {
            require(apiKey.isNotBlank()) { "API key cannot be empty" }

            return SDKConfig(
                apiKey = apiKey,
                minConfidenceThreshold = minConfidenceThreshold,
                analyticsEnabled = analyticsEnabled,
                debugMode = debugMode,
                timeoutMillis = timeoutMillis,
                maxRetries = maxRetries,
                cacheEnabled = cacheEnabled,
                certificatePinning = certificatePinning,
                allowedDeepLinkSchemes = allowedDeepLinkSchemes.toList(),
                language = language,
                socketReconnectionAttempts = socketReconnectionAttempts,
                socketReconnectionDelay = socketReconnectionDelay
            )
        }
    }

    override fun toString(): String {
        return "SDKConfig(apiKey=***REDACTED***, " +
                "minConfidenceThreshold=$minConfidenceThreshold, " +
                "analyticsEnabled=$analyticsEnabled, " +
                "debugMode=$debugMode, " +
                "timeoutMillis=$timeoutMillis, " +
                "maxRetries=$maxRetries, " +
                "cacheEnabled=$cacheEnabled, " +
                "certificatePinning=${certificatePinning != null}, " +
                "allowedDeepLinkSchemes=$allowedDeepLinkSchemes, " +
                "language=$language, " +
                "socketReconnectionAttempts=$socketReconnectionAttempts, " +
                "socketReconnectionDelay=$socketReconnectionDelay)"
    }
}

/**
 * Certificate pinning configuration for enhanced network security.
 *
 * @property enabled Whether certificate pinning is active
 * @property certificates List of SHA-256 certificate hashes
 */
data class CertificatePinningConfig(
    val enabled: Boolean = true,
    val certificates: List<String> = emptyList()
)
