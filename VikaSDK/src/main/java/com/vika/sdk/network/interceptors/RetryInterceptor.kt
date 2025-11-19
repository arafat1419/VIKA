package com.vika.sdk.network.interceptors

import android.util.Log
import com.vika.sdk.network.exceptions.NetworkException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that retries failed requests with exponential backoff.
 *
 * Automatically retries requests that fail due to network errors or
 * specific HTTP status codes (408, 429, 500, 502, 503, 504).
 *
 * @param maxRetries Maximum number of retry attempts
 * @param initialDelayMs Initial delay before first retry in milliseconds
 */
internal class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000
) : Interceptor {

    companion object {
        private const val TAG = "RetryInterceptor"
        private val RETRYABLE_STATUS_CODES = setOf(408, 429, 500, 502, 503, 504)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response: Response? = null
        var lastException: Exception? = null

        while (attempt < maxRetries) {
            try {
                // Close previous response if exists
                response?.close()

                // Make request
                response = chain.proceed(chain.request())

                // Check if successful
                if (response.isSuccessful) {
                    return response
                }

                // Check if should retry
                if (!shouldRetry(response.code)) {
                    return response
                }

                Log.w(
                    TAG,
                    "Request failed with code ${response.code}, attempt ${attempt + 1}/$maxRetries"
                )

            } catch (e: Exception) {
                lastException = e
                Log.e(TAG, "Request failed with exception, attempt ${attempt + 1}/$maxRetries", e)
            }

            attempt++

            if (attempt < maxRetries) {
                val delay = calculateBackoff(attempt)
                Log.d(TAG, "Retrying after ${delay}ms")
                Thread.sleep(delay)
            }
        }

        // All retries exhausted
        response?.close()

        if (lastException != null) {
            throw NetworkException("Request failed after $maxRetries attempts", lastException)
        }

        throw NetworkException("Request failed after $maxRetries attempts")
    }

    private fun shouldRetry(statusCode: Int): Boolean {
        return statusCode in RETRYABLE_STATUS_CODES
    }

    private fun calculateBackoff(attempt: Int): Long {
        // Exponential backoff with jitter
        val exponentialDelay = initialDelayMs * Math.pow(2.0, (attempt - 1).toDouble()).toLong()
        val jitter = (Math.random() * 1000).toLong()
        return Math.min(exponentialDelay + jitter, 30000L) // Cap at 30 seconds
    }
}
