package com.vika.sdk.network.interceptors

import com.vika.sdk.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds authentication headers to all requests.
 *
 * Adds API key, SDK version, platform, and timestamp headers.
 *
 * @param apiKey API key for authentication
 */
internal class AuthenticationInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("X-API-Key", apiKey)
            .addHeader("X-SDK-Version", BuildConfig.SDK_VERSION)
            .addHeader("X-Platform", "Android")
            .addHeader("X-Timestamp", System.currentTimeMillis().toString())
            .build()

        return chain.proceed(request)
    }
}
