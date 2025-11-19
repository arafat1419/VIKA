package com.vika.sdk.network.api

import android.util.Base64
import com.vika.sdk.BuildConfig
import com.vika.sdk.models.SDKConfig
import com.vika.sdk.models.ScreenRegistration
import com.vika.sdk.network.exceptions.NetworkException
import com.vika.sdk.network.exceptions.SecurityException
import com.vika.sdk.network.interceptors.AuthenticationInterceptor
import com.vika.sdk.network.interceptors.RequestSigningInterceptor
import com.vika.sdk.network.interceptors.ResponseValidationInterceptor
import com.vika.sdk.network.interceptors.RetryInterceptor
import com.vika.sdk.network.models.ApiResponse
import com.vika.sdk.network.models.InitializeData
import com.vika.sdk.network.models.InitializeRequest
import com.vika.sdk.network.models.NavigationData
import com.vika.sdk.network.models.RecordingData
import com.vika.sdk.network.models.RegisterScreensData
import com.vika.sdk.network.models.RegisterScreensRequest
import kotlinx.coroutines.delay
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
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Secure API client with encryption and certificate pinning.
 *
 * Handles all network communication with the VIKA backend including
 * initialization, screen registration, and voice recording submission.
 *
 * @param config SDK configuration
 */
internal class SecureNavigationApiClient(
    private val config: SDKConfig
) {
    private val encryptionKey = generateEncryptionKey(config.apiKey)

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

        // Authentication
        addInterceptor(AuthenticationInterceptor(config.apiKey))

        // Request signing
        addInterceptor(RequestSigningInterceptor(config.apiKey))

        // Response validation
        addInterceptor(ResponseValidationInterceptor())

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
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(NavigationApi::class.java)

    // Session ID from initialize response
    private var sessionId: String? = null

    // Flag to use mock responses (set to true until BE is ready)
    private val useMock = true

    /**
     * Initialize SDK with backend.
     *
     * @param appPackage Application package name
     * @return InitializeData with session ID
     * @throws NetworkException if initialization fails
     */
    suspend fun initialize(appPackage: String): InitializeData {
        if (useMock) {
            // Mock response for testing
            delay(500)
            val mockSessionId = UUID.randomUUID().toString()
            sessionId = mockSessionId
            return InitializeData(
                sessionId = mockSessionId,
                expiresAt = System.currentTimeMillis() + 3600000
            )
        }

        val request = InitializeRequest(
            apiKey = config.apiKey,
            appPackage = appPackage,
            sdkVersion = BuildConfig.SDK_VERSION,
            timestamp = System.currentTimeMillis()
        )

        val response = api.initialize(request)
        val apiResponse = handleResponse(response, "Initialize")

        val data = apiResponse.data ?: throw NetworkException("Empty initialize data")
        sessionId = data.sessionId
        return data
    }

    /**
     * Register screens with backend.
     *
     * @param screens List of screen registrations
     * @return RegisterScreensData with count
     * @throws NetworkException if registration fails
     */
    suspend fun registerScreens(screens: List<ScreenRegistration>): RegisterScreensData {
        val currentSessionId = sessionId ?: throw NetworkException("SDK not initialized")

        if (useMock) {
            // Mock response for testing
            delay(300)
            return RegisterScreensData(
                registeredCount = screens.size
            )
        }

        val request = RegisterScreensRequest(
            sessionId = currentSessionId,
            screens = screens.map { it.toSecureScreen() },
            timestamp = System.currentTimeMillis()
        )

        val response = api.registerScreens(request)
        val apiResponse = handleResponse(response, "Register screens")

        return apiResponse.data ?: throw NetworkException("Empty register data")
    }

    /**
     * Send voice recording and get response.
     *
     * @param audioFile Audio file to send
     * @param registeredScreens List of registered screens for mock navigation
     * @return RecordingData with transcription, reply audio, and navigation
     * @throws NetworkException if sending fails
     */
    suspend fun sendRecording(
        audioFile: File,
        registeredScreens: List<ScreenRegistration> = emptyList()
    ): RecordingData {
        val currentSessionId = sessionId ?: throw NetworkException("SDK not initialized")

        if (useMock) {
            // Mock response for testing
            delay(1000)

            // Pick a random screen for mock navigation if available
            val mockNavigation = if (registeredScreens.isNotEmpty()) {
                val screen = registeredScreens.random()
                NavigationData(
                    screenId = screen.screenId,
                    deepLink = screen.deepLink,
                    confidence = 0.95f
                )
            } else {
                null
            }

            return RecordingData(
                transcription = "Mock transcription of user voice",
                replyText = "I'll help you navigate to the requested screen.",
                replyAudioUrl = null,
                navigation = mockNavigation
            )
        }

        val requestBody = audioFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, requestBody)

        val response = api.sendRecording(audioPart, currentSessionId)
        val apiResponse = handleResponse(response, "Send recording")

        return apiResponse.data ?: throw NetworkException("Empty recording data")
    }

    /**
     * Handle API response and extract body with error handling.
     *
     * @param response Retrofit response
     * @param operation Operation name for error messages
     * @return ApiResponse body
     * @throws NetworkException if response is unsuccessful or empty
     */
    private fun <T> handleResponse(
        response: retrofit2.Response<ApiResponse<T>>,
        operation: String
    ): ApiResponse<T> {
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

    private fun encryptData(data: String): String {
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val keySpec = SecretKeySpec(encryptionKey, "AES")
            val ivSpec = IvParameterSpec(ByteArray(16))

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(data.toByteArray())

            return Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw SecurityException("Encryption failed", e)
        }
    }

    private fun decryptData(encryptedData: String): String {
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val keySpec = SecretKeySpec(encryptionKey, "AES")
            val ivSpec = IvParameterSpec(ByteArray(16))

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decrypted = cipher.doFinal(
                Base64.decode(encryptedData, Base64.NO_WRAP)
            )

            return String(decrypted)
        } catch (e: Exception) {
            throw SecurityException("Decryption failed", e)
        }
    }

    private fun generateEncryptionKey(apiKey: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(apiKey.toByteArray()).copyOf(16)
    }

    private fun generateNonce(): String {
        return System.currentTimeMillis().toString() +
                (0..999999).random().toString()
    }

    private fun generateSignature(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest("$content${config.apiKey}".toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun extractDomain(url: String): String {
        return url.replace("https://", "")
            .replace("http://", "")
            .split("/").first()
    }
}
