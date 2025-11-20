package com.vika.sdk.network.api

import android.util.Base64
import com.vika.sdk.BuildConfig
import com.vika.sdk.models.SDKConfig
import com.vika.sdk.models.ScreenRegistration
import com.vika.sdk.network.exceptions.NetworkException
import com.vika.sdk.network.interceptors.AuthenticationInterceptor
import com.vika.sdk.network.interceptors.RetryInterceptor
import com.vika.sdk.network.models.ConversationResponse
import com.vika.sdk.network.models.InitializeData
import com.vika.sdk.network.models.InitializeRequest
import com.vika.sdk.network.models.ScreenData
import com.vika.sdk.network.models.ScreenRequest
import okhttp3.CertificatePinner
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Secure API client for VIKA backend communication.
 *
 * Handles all network communication including initialization,
 * screen registration, and audio conversation submission.
 *
 * @param config SDK configuration
 */
internal class SecureNavigationApiClient(
    private val config: SDKConfig
) {
    private val okHttpClient = OkHttpClient.Builder().apply {
        // Timeouts
        connectTimeout(config.timeoutMillis, TimeUnit.MILLISECONDS)
        readTimeout(config.timeoutMillis, TimeUnit.MILLISECONDS)
        writeTimeout(config.timeoutMillis, TimeUnit.MILLISECONDS)

        // Certificate pinning
        config.certificatePinning?.let { pinConfig ->
            if (pinConfig.enabled && pinConfig.certificates.isNotEmpty()) {
                certificatePinner(
                    CertificatePinner.Builder().apply {
                        val domain = extractDomain(BuildConfig.BASE_URL)
                        pinConfig.certificates.forEach { cert ->
                            add(domain, cert)
                        }
                    }.build()
                )
            }
        }

        // Authentication interceptor (handles Bearer token for protected endpoints)
        addInterceptor(AuthenticationInterceptor(config.apiKey, ::getSessionId))

        // Retry interceptor
        addInterceptor(RetryInterceptor(config.maxRetries))

        // Logging (only in debug mode)
        if (config.debugMode) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
    }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL + "/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(NavigationApi::class.java)

    // Session ID from initialize response
    private var sessionId: String? = null

    /**
     * Initialize SDK with backend.
     *
     * @return InitializeData with session ID
     * @throws NetworkException if initialization fails
     */
    suspend fun initialize(): InitializeData {
        val timestamp = System.currentTimeMillis()
        val signature = generateSignature(config.apiKey + timestamp)

        val request = InitializeRequest(
            apiKey = config.apiKey,
            timestamp = timestamp,
            signature = signature
        )

        val response = api.initialize(request)
        val data = handleResponse(response, "Initialize")

        sessionId = data.sessionId
        return data
    }

    /**
     * Save screens with backend.
     *
     * @param screens List of screen registrations
     * @return ScreenData with updated count
     * @throws NetworkException if saving fails
     */
    suspend fun saveScreens(screens: List<ScreenRegistration>): ScreenData {
        if (sessionId == null) {
            throw NetworkException("SDK not initialized")
        }

        val request = ScreenRequest(
            screens = screens.map { it.toSecureScreen() }
        )

        val response = api.saveScreens(request)
        return handleResponse(response, "Save screens")
    }

    /**
     * Send audio for conversation processing.
     *
     * Results are delivered through Socket.IO 'conversation_processed' event.
     *
     * @param audioFile Audio file to send (mp3, wav, m4a, ogg, webm)
     * @return ConversationResponse with conversation ID for tracking
     * @throws NetworkException if sending fails
     */
    suspend fun sendConversation(audioFile: File): ConversationResponse {
        if (sessionId == null) {
            throw NetworkException("SDK not initialized")
        }

        val mediaType = getAudioMediaType(audioFile.extension)
        val requestBody = audioFile.asRequestBody(mediaType.toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, requestBody)

        val response = api.sendConversation(audioPart)
        return handleResponse(response, "Send conversation")
    }

    /**
     * Handle API response and extract body with error handling.
     *
     * @param response Retrofit response
     * @param operation Operation name for error messages
     * @return Response body
     * @throws NetworkException if response is unsuccessful or empty
     */
    private fun <T> handleResponse(
        response: retrofit2.Response<T>,
        operation: String
    ): T {
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw NetworkException(
                "$operation failed: ${response.code()} - ${errorBody ?: response.message()}"
            )
        }

        return response.body() ?: throw NetworkException("Empty $operation response")
    }

    /**
     * Get current session ID.
     */
    fun getSessionId(): String? = sessionId

    /**
     * Check if SDK is initialized with backend.
     */
    fun isInitializedWithBackend(): Boolean = sessionId != null

    /**
     * Clear session data.
     */
    fun clearSession() {
        sessionId = null
    }

    /**
     * Get appropriate media type for audio file extension.
     */
    private fun getAudioMediaType(extension: String): String {
        return when (extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "ogg" -> "audio/ogg"
            "webm" -> "audio/webm"
            else -> "audio/mpeg"
        }
    }

    private fun generateSignature(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(content.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun extractDomain(url: String): String {
        return url.replace("https://", "")
            .replace("http://", "")
            .split("/").first()
    }
}
