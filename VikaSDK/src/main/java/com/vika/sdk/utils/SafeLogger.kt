package com.vika.sdk.utils

import android.util.Log

/**
 * Secure logging utility that redacts sensitive information.
 *
 * Provides safe logging methods that automatically redact API keys,
 * session tokens, and other sensitive data from log output.
 */
internal object SafeLogger {
    private var isEnabled: Boolean = false

    /**
     * Enable or disable debug logging.
     *
     * @param enabled True to enable logging
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    /**
     * Log debug message with automatic redaction of sensitive data.
     *
     * @param tag Log tag
     * @param message Message to log
     */
    fun d(tag: String, message: String) {
        if (isEnabled) {
            Log.d(tag, redactSensitiveData(message))
        }
    }

    /**
     * Log info message with automatic redaction of sensitive data.
     *
     * @param tag Log tag
     * @param message Message to log
     */
    fun i(tag: String, message: String) {
        if (isEnabled) {
            Log.i(tag, redactSensitiveData(message))
        }
    }

    /**
     * Log warning message with automatic redaction of sensitive data.
     *
     * @param tag Log tag
     * @param message Message to log
     */
    fun w(tag: String, message: String) {
        if (isEnabled) {
            Log.w(tag, redactSensitiveData(message))
        }
    }

    /**
     * Log error message with automatic redaction of sensitive data.
     *
     * @param tag Log tag
     * @param message Message to log
     * @param throwable Optional throwable
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isEnabled) {
            if (throwable != null) {
                Log.e(tag, redactSensitiveData(message), throwable)
            } else {
                Log.e(tag, redactSensitiveData(message))
            }
        }
    }

    /**
     * Redact sensitive data from message.
     *
     * Replaces patterns matching API keys, tokens, and other sensitive
     * information with redacted placeholders.
     *
     * @param message Original message
     * @return Message with sensitive data redacted
     */
    private fun redactSensitiveData(message: String): String {
        var redacted = message

        // Redact API keys (common patterns)
        redacted = redacted.replace(
            Regex("apiKey[=:]\\s*[\"']?([^\"'\\s,}]+)[\"']?", RegexOption.IGNORE_CASE),
            "apiKey=***REDACTED***"
        )

        // Redact session IDs (UUID pattern)
        redacted = redacted.replace(
            Regex("sessionId[=:]\\s*[\"']?([a-f0-9-]{36})[\"']?", RegexOption.IGNORE_CASE),
            "sessionId=***REDACTED***"
        )

        // Redact Bearer tokens
        redacted = redacted.replace(
            Regex("Bearer\\s+[A-Za-z0-9._-]+", RegexOption.IGNORE_CASE),
            "Bearer ***REDACTED***"
        )

        // Redact X-API-Key header values
        redacted = redacted.replace(
            Regex("X-API-Key[=:]\\s*[\"']?([^\"'\\s,}]+)[\"']?", RegexOption.IGNORE_CASE),
            "X-API-Key=***REDACTED***"
        )

        // Redact encrypted data (Base64 blocks > 32 chars)
        redacted = redacted.replace(
            Regex(
                "encryptedQuery[=:]\\s*[\"']?([A-Za-z0-9+/=]{32,})[\"']?",
                RegexOption.IGNORE_CASE
            ),
            "encryptedQuery=***REDACTED***"
        )

        return redacted
    }

    /**
     * Get a redacted string representation of SDKConfig.
     *
     * @param configString Config toString() output
     * @return Redacted config string
     */
    fun redactConfig(configString: String): String {
        return redactSensitiveData(configString)
    }
}
