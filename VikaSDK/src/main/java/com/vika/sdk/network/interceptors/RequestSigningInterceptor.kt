package com.vika.sdk.network.interceptors

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.security.MessageDigest

/**
 * Interceptor that signs requests with HMAC-SHA256.
 *
 * Generates a signature based on request method, path, timestamp,
 * and body length to prevent request tampering.
 *
 * @param apiKey API key used for signature generation
 */
internal class RequestSigningInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val timestamp = System.currentTimeMillis()

        // Generate signature
        val signature = generateRequestSignature(request, timestamp)

        val signedRequest = request.newBuilder()
            .addHeader("X-Signature", signature)
            .addHeader("X-Timestamp", timestamp.toString())
            .build()

        return chain.proceed(signedRequest)
    }

    private fun generateRequestSignature(request: Request, timestamp: Long): String {
        val content = buildString {
            append(request.method)
            append(request.url.encodedPath)
            append(timestamp)
            request.body?.let {
                append(it.contentLength())
            }
        }

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest("$content$apiKey".toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}
