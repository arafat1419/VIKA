package com.vika.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.vika.sdk.VikaSDK.Companion.initialize
import com.vika.sdk.analytics.AnalyticsTracker
import com.vika.sdk.callbacks.NavigationCallback
import com.vika.sdk.callbacks.NavigationError
import com.vika.sdk.models.NavigationOptions
import com.vika.sdk.models.NavigationResult
import com.vika.sdk.models.NavigationType
import com.vika.sdk.models.SDKConfig
import com.vika.sdk.models.ScreenRegistration
import com.vika.sdk.models.VikaDisplayMode
import com.vika.sdk.models.VikaError
import com.vika.sdk.models.VikaLanguage
import com.vika.sdk.models.VikaResult
import com.vika.sdk.models.VikaUIOptions
import com.vika.sdk.navigation.DeepLinkNavigationHandler
import com.vika.sdk.navigation.NavigationHandler
import com.vika.sdk.network.api.SecureNavigationApiClient
import com.vika.sdk.network.exceptions.NetworkException
import com.vika.sdk.network.exceptions.SecurityException
import com.vika.sdk.network.models.RecordingData
import com.vika.sdk.security.LicenseValidator
import com.vika.sdk.security.SDKSecurityManager
import com.vika.sdk.session.SessionManager
import com.vika.sdk.ui.VikaBottomSheetActivity
import com.vika.sdk.ui.VikaDialogActivity
import com.vika.sdk.ui.VikaMainActivity
import com.vika.sdk.utils.SafeLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Main entry point for the VIKA Navigation SDK.
 *
 * VIKA enables voice-powered navigation in Android applications, allowing users
 * to navigate to screens using natural language voice commands.
 *
 * ## Quick Start
 *
 * ### 1. Initialize the SDK
 * ```kotlin
 * val config = SDKConfig.Builder("your-api-key")
 *     .minConfidenceThreshold(0.75f)
 *     .debugMode(BuildConfig.DEBUG)
 *     .allowedDeepLinkSchemes("myapp")
 *     .build()
 *
 * VikaSDK.initialize(context, config) {
 *     // SDK ready
 * }
 * ```
 *
 * ### 2. Register Screens
 * ```kotlin
 * VikaSDK.getInstance().registerScreens(listOf(
 *     ScreenRegistration(
 *         screenId = "home",
 *         screenName = "Home",
 *         description = "Main home screen",
 *         deepLink = "myapp://home",
 *         navigationType = NavigationType.DeepLink("myapp://home"),
 *         keywords = listOf("home", "main")
 *     )
 * ))
 * ```
 *
 * ### 3. Open Voice UI
 * ```kotlin
 * VikaSDK.getInstance().openVikaSDK(context)
 * ```
 *
 * ## Threading
 * All public methods are safe to call from any thread. Callbacks are invoked on the Main thread.
 *
 * ## Security
 * The SDK includes:
 * - AES-256 encryption for sensitive data
 * - Certificate pinning support
 * - Deep link validation
 * - Rate limiting
 * - App signature verification
 *
 * @see SDKConfig
 * @see ScreenRegistration
 * @see initialize
 */
class VikaSDK private constructor(
    private val context: Context,
    private val config: SDKConfig
) {
    private val registeredScreens = ConcurrentHashMap<String, ScreenRegistration>()
    private val navigationHandlers = ConcurrentHashMap<String, NavigationHandler>()

    private val analyticsTracker: AnalyticsTracker by lazy {
        AnalyticsTracker(context, config)
    }

    private val apiClient: SecureNavigationApiClient by lazy {
        SecureNavigationApiClient(config)
    }

    private val securityManager: SDKSecurityManager by lazy {
        SDKSecurityManager(context)
    }

    private val licenseValidator: LicenseValidator by lazy {
        LicenseValidator(securityManager)
    }

    private val sessionManager = SessionManager()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "VikaSDK"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: VikaSDK? = null

        @Volatile
        internal var currentUIOptions: VikaUIOptions? = null
            private set

        /**
         * Get current UI options for the SDK UI.
         *
         * @return Current UI options or null if not set
         */
        @JvmStatic
        fun getCurrentUIOptions(): VikaUIOptions? = currentUIOptions

        /**
         * Initialize the SDK with configuration.
         * This will also call the backend to validate credentials.
         *
         * @param context Application context
         * @param config SDK configuration
         * @param callback Optional callback for initialization result
         */
        @JvmStatic
        fun initialize(
            context: Context,
            config: SDKConfig,
            callback: InitCallback? = null
        ) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        val sdk = VikaSDK(context.applicationContext, config)
                        instance = sdk

                        // Configure safe logging
                        SafeLogger.setEnabled(config.debugMode)
                        SafeLogger.d(TAG, "SDK initialized with config: $config")

                        // Validate license
                        val validationResult = sdk.licenseValidator.validate(
                            apiKey = config.apiKey,
                            allowDebug = true,
                            allowEmulator = true
                        )

                        if (!validationResult.isValid) {
                            SafeLogger.e(
                                TAG,
                                "License validation failed: ${validationResult.message}"
                            )
                            callback?.onError(IllegalStateException(validationResult.message))
                            return
                        }

                        // Call backend to validate credentials
                        sdk.scope.launch {
                            try {
                                val data = sdk.apiClient.initialize(
                                    context.applicationContext.packageName
                                )
                                sdk.sessionManager.setSession(data.sessionId, data.expiresAt)
                                sdk.logDebug("Backend initialized: session active")
                                callback?.onSuccess()
                            } catch (e: Exception) {
                                sdk.logError("Backend initialization failed", e)
                                callback?.onError(e)
                            }
                        }
                    }
                }
            } else {
                callback?.onSuccess()
            }
        }

        /**
         * Callback interface for SDK initialization
         */
        interface InitCallback {
            fun onSuccess()
            fun onError(error: Throwable)
        }

        /**
         * Get SDK instance
         */
        @JvmStatic
        fun getInstance(): VikaSDK {
            return instance ?: throw IllegalStateException(
                "SDK not initialized. Call VikaSDK.initialize() first."
            )
        }

        /**
         * Check if SDK is initialized
         */
        @JvmStatic
        fun isInitialized(): Boolean = instance != null

        /**
         * Destroy SDK instance and clean up resources
         */
        @JvmStatic
        fun destroy() {
            instance?.cleanup()
            instance = null
        }
    }

    init {
        // Initialize deep link handler with validation
        navigationHandlers["deeplink"] = DeepLinkNavigationHandler(
            context = context,
            allowedSchemes = config.allowedDeepLinkSchemes,
            registeredScreensProvider = { registeredScreens.values.toList() }
        )
    }

    /**
     * Register a navigation handler
     */
    fun registerNavigationHandler(type: String, handler: NavigationHandler) {
        navigationHandlers[type] = handler
        logDebug("Registered navigation handler: $type")
    }

    /**
     * Register a single screen
     *
     * @param screen Screen registration
     * @param callback Optional callback for registration result
     */
    fun registerScreen(
        screen: ScreenRegistration,
        callback: RegisterCallback? = null
    ): VikaSDK {
        registeredScreens[screen.screenId] = screen
        logDebug("Registered screen locally: ${screen.screenId}")

        // Register with backend
        scope.launch {
            try {
                val response = apiClient.registerScreens(listOf(screen))
                logDebug("Screen registered with backend: ${response.registeredCount}")
                callback?.onSuccess(response.registeredCount)
            } catch (e: Exception) {
                logError("Failed to register screen with backend", e)
                callback?.onError(e)
            }
        }

        return this
    }

    /**
     * Register multiple screens
     *
     * @param screens List of screen registrations
     * @param callback Optional callback for registration result
     */
    fun registerScreens(
        screens: List<ScreenRegistration>,
        callback: RegisterCallback? = null
    ): VikaSDK {
        screens.forEach { screen ->
            registeredScreens[screen.screenId] = screen
            logDebug("Registered screen locally: ${screen.screenId}")
        }

        // Register all screens with backend
        scope.launch {
            try {
                val response = apiClient.registerScreens(screens)
                logDebug("Screens registered with backend: ${response.registeredCount}")
                callback?.onSuccess(response.registeredCount)
            } catch (e: Exception) {
                logError("Failed to register screens with backend", e)
                callback?.onError(e)
            }
        }

        return this
    }

    /**
     * Callback interface for screen registration
     */
    interface RegisterCallback {
        fun onSuccess(count: Int)
        fun onError(error: Throwable)
    }

    /**
     * Send voice recording to backend and get response
     *
     * @param audioFile Audio file to send
     * @param callback Callback for recording response
     */
    suspend fun sendRecording(
        audioFile: File,
        callback: RecordingCallback
    ) {
        withContext(Dispatchers.Main) {
            try {
                callback.onStarted()

                val response = withContext(Dispatchers.IO) {
                    apiClient.sendRecording(audioFile, registeredScreens.values.toList())
                }

                logDebug("Recording response: transcription=${response.transcription}")

                // Handle navigation if present
                response.navigation?.let { nav ->
                    val screen = registeredScreens[nav.screenId]
                    if (screen != null && nav.confidence >= config.minConfidenceThreshold) {
                        logDebug("Will navigate to: ${nav.deepLink}")
                    }
                }

                callback.onSuccess(response)

            } catch (e: Exception) {
                logError("Send recording failed", e)
                callback.onError(e)
            }
        }
    }

    /**
     * Execute navigation from recording response
     *
     * @param deepLink Deep link to navigate to
     */
    fun executeDeepLinkNavigation(deepLink: String) {
        navigationHandlers["deeplink"]?.navigate(
            NavigationType.DeepLink(deepLink),
            context
        )
    }

    /**
     * Callback interface for recording response
     */
    interface RecordingCallback {
        fun onStarted()
        fun onSuccess(response: RecordingData)
        fun onError(error: Throwable)
    }

    /**
     * Open VikaSDK UI with default options.
     *
     * @param context Context to start the activity
     */
    fun openVikaSDK(context: Context) {
        openVikaSDK(context, VikaUIOptions())
    }

    /**
     * Open VikaSDK UI with custom options.
     *
     * @param context Context to start the activity
     * @param options UI customization options including display mode, theme, and branding
     */
    fun openVikaSDK(context: Context, options: VikaUIOptions) {
        // Store options for Activity to access
        currentUIOptions = options

        val activityClass = when (options.displayMode) {
            VikaDisplayMode.FULLSCREEN -> VikaMainActivity::class.java
            VikaDisplayMode.DIALOG -> VikaDialogActivity::class.java
            VikaDisplayMode.BOTTOM_SHEET -> VikaBottomSheetActivity::class.java
        }

        Intent(context, activityClass).also { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * Navigate from natural language query
     */
    suspend fun navigateFromQuery(
        query: String,
        callback: NavigationCallback? = null,
        options: NavigationOptions = NavigationOptions()
    ) {
        withContext(Dispatchers.Main) {
            try {
                callback?.onNavigationStarted()

                // Track query attempt
                analyticsTracker.trackQueryAttempt(query)

                // Validate query
                if (query.isBlank()) {
                    val error = NavigationError.InvalidQuery("Query cannot be empty")
                    callback?.onNavigationFailed(error)
                    analyticsTracker.trackNavigationError(query, error)
                    return@withContext
                }

                // Call API
                /*val result = withContext(Dispatchers.IO) {
                    apiClient.queryNavigation(
                        query = query,
                        screens = registeredScreens.values.toList(),
                        options = options
                    )
                }*/

                val result = NavigationResult(
                    screenId = "home",
                    deepLink = "sample://home",
                    confidence = 0.9F,
                )

                // Check confidence threshold
                if (result.confidence < config.minConfidenceThreshold) {
                    val error = NavigationError.LowConfidence(result.confidence)
                    callback?.onNavigationFailed(error)
                    analyticsTracker.trackNavigationError(query, error)
                    return@withContext
                }

                // Get screen registration
                val screen = registeredScreens[result.screenId]
                if (screen == null) {
                    val error = NavigationError.ScreenNotFound(result.screenId)
                    callback?.onNavigationFailed(error)
                    analyticsTracker.trackNavigationError(query, error)
                    return@withContext
                }

                // Execute navigation
                if (options.executeImmediately) {
                    executeNavigation(screen, result)
                }

                // Track success
                analyticsTracker.trackNavigationSuccess(query, result)

                // Notify callback
                callback?.onNavigationSuccess(result)

            } catch (e: Exception) {
                val error = when (e) {
                    is NetworkException -> NavigationError.NetworkError(e)
                    is SecurityException -> NavigationError.SecurityError(
                        e.message ?: "Security error"
                    )

                    else -> NavigationError.UnknownError(e)
                }
                callback?.onNavigationFailed(error)
                analyticsTracker.trackNavigationError(query, error)
                logError("Navigation failed", e)
            }
        }
    }

    /**
     * Execute navigation using appropriate handler
     */
    private fun executeNavigation(screen: ScreenRegistration, result: NavigationResult) {
        when (screen.navigationType) {
            is NavigationType.DeepLink -> {
                navigationHandlers["deeplink"]?.navigate(
                    NavigationType.DeepLink(result.deepLink),
                    context
                )
            }
        }
    }

    /**
     * Get all registered screens
     */
    fun getRegisteredScreens(): List<ScreenRegistration> {
        return registeredScreens.values.toList()
    }

    /**
     * Get the configured language for the SDK UI.
     */
    fun getLanguage(): VikaLanguage {
        return config.language
    }

    /**
     * Clean up resources
     */
    private fun cleanup() {
        scope.cancel()
        registeredScreens.clear()
        navigationHandlers.clear()
        logDebug("SDK cleaned up")
    }

    private fun logDebug(message: String) {
        SafeLogger.d(TAG, message)
    }

    private fun logError(message: String, throwable: Throwable? = null) {
        SafeLogger.e(TAG, message, throwable)
    }

    /**
     * Send recording and get result with typed error handling.
     *
     * @param audioFile Audio file to send
     * @return Result containing RecordingData or VikaError
     */
    suspend fun sendRecordingAsync(audioFile: File): VikaResult<RecordingData> {
        return withContext(Dispatchers.IO) {
            try {
                // Check rate limiting
                if (securityManager.isRateLimited()) {
                    return@withContext VikaResult.failure(VikaError.RateLimited())
                }

                // Check session validity
                if (!sessionManager.hasValidSession()) {
                    return@withContext VikaResult.failure(
                        VikaError.Authentication("Session expired. Please reinitialize.")
                    )
                }

                val response = apiClient.sendRecording(
                    audioFile,
                    registeredScreens.values.toList()
                )

                logDebug("Recording response received")
                VikaResult.success(response)
            } catch (e: NetworkException) {
                VikaResult.failure(VikaError.Network(e.message ?: "Network error", e))
            } catch (e: SecurityException) {
                VikaResult.failure(VikaError.Security(e.message ?: "Security error", e))
            } catch (e: Exception) {
                VikaResult.failure(VikaError.Unknown(e.message ?: "Unknown error", e))
            }
        }
    }

    /**
     * Register screens and get result with typed error handling.
     *
     * @param screens List of screens to register
     * @return Result containing registered count or VikaError
     */
    suspend fun registerScreensAsync(screens: List<ScreenRegistration>): VikaResult<Int> {
        return withContext(Dispatchers.IO) {
            try {
                // Register locally first
                screens.forEach { screen ->
                    registeredScreens[screen.screenId] = screen
                }

                // Check rate limiting
                if (securityManager.isRateLimited()) {
                    return@withContext VikaResult.failure(VikaError.RateLimited())
                }

                val response = apiClient.registerScreens(screens)
                logDebug("Registered ${response.registeredCount} screens with backend")
                VikaResult.success(response.registeredCount)
            } catch (e: NetworkException) {
                VikaResult.failure(VikaError.Network(e.message ?: "Network error", e))
            } catch (e: Exception) {
                VikaResult.failure(VikaError.Unknown(e.message ?: "Unknown error", e))
            }
        }
    }
}