package com.vika.sdk.network.interceptors

import com.vika.sdk.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds authentication headers to requests.
 *
 * Uses Bearer token (session_id) for protected endpoints.
 * Skips authentication for initialize endpoint.
 *
 * @param apiKey API key for authentication
 * @param sessionIdProvider Provider function to get current session ID
 */
internal class AuthenticationInterceptor(
    private val apiKey: String,
    private val sessionIdProvider: () -> String?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        val requestBuilder = originalRequest.newBuilder()
            .addHeader("X-SDK-Version", BuildConfig.SDK_VERSION)
            .addHeader("X-Platform", "Android")

        // Add Bearer token for protected endpoints
        if (!path.contains("auth/initialize")) {
            val sessionId = sessionIdProvider()
            if (sessionId != null) {
                requestBuilder.addHeader("Authorization", "Bearer $sessionId")
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}
