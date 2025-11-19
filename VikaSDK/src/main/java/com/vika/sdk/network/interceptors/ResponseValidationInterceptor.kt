package com.vika.sdk.network.interceptors

import com.vika.sdk.network.exceptions.SecurityException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that validates response security headers.
 *
 * Checks for required security headers and validates response timestamps
 * to prevent replay attacks.
 */
internal class ResponseValidationInterceptor : Interceptor {

    companion object {
        private const val MAX_TIME_DIFF_MS = 5 * 60 * 1000L // 5 minutes
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        // Validate response headers
        validateResponseHeaders(response)

        // Validate timestamp
        validateTimestamp(response)

        return response
    }

    private fun validateResponseHeaders(response: Response) {
        if (!response.isSuccessful) {
            return // Skip validation for error responses
        }

        val signature = response.header("X-Response-Signature")
        if (signature == null) {
            throw SecurityException("Missing response signature")
        }

        val contentType = response.header("Content-Type")
        if (contentType == null || !contentType.contains("application/json")) {
            throw SecurityException("Invalid content type: $contentType")
        }
    }

    private fun validateTimestamp(response: Response) {
        val timestamp = response.header("X-Timestamp")?.toLongOrNull()
        if (timestamp == null) {
            return // Skip if no timestamp header
        }

        val currentTime = System.currentTimeMillis()
        val timeDiff = Math.abs(currentTime - timestamp)

        if (timeDiff > MAX_TIME_DIFF_MS) {
            throw SecurityException("Response timestamp is too old: ${timeDiff}ms difference")
        }
    }
}
