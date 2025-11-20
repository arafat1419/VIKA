package com.vika.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import com.vika.sdk.network.models.ConversationProcessedEvent
import com.vika.sdk.network.models.ConversationResponse
import com.vika.sdk.network.socket.SocketEventListener
import com.vika.sdk.network.socket.SocketManager
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

    private val socketManager: SocketManager by lazy {
        SocketManager(config)
    }

    private val sessionManager = SessionManager()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var conversationListener: ConversationListener? = null

    @Volatile
    private var isBackendInitialized = false

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
         * This will call the backend to validate credentials and connect to Socket.IO.
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

                        // Call backend to validate credentials and get session ID
                        sdk.scope.launch {
                            try {
                                val data = sdk.apiClient.initialize()
                                sdk.sessionManager.setSession(data.sessionId)
                                sdk.isBackendInitialized = true
                                sdk.logDebug("Backend initialized: session=${data.sessionId}")

                                // Connect to Socket.IO with the session ID
                                sdk.connectSocket(data.sessionId)

                                callback?.onSuccess()
                            } catch (e: Exception) {
                                sdk.isBackendInitialized = false
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
     * Connect to Socket.IO server with the session ID.
     */
    private fun connectSocket(sessionId: String) {
        socketManager.setEventListener(object : SocketEventListener {
            override fun onConnected(sessionId: String) {
                logDebug("Socket connected with session: $sessionId")
            }

            override fun onDisconnected() {
                logDebug("Socket disconnected")
            }

            override fun onConversationProcessed(event: ConversationProcessedEvent) {
                logDebug("Conversation processed: ${event.conversationId}")
                scope.launch {
                    conversationListener?.onConversationProcessed(event)
                }
            }

            override fun onError(error: String) {
                logError("Socket error: $error")
                scope.launch {
                    conversationListener?.onError(
                        VikaError.SocketConnection(error)
                    )
                }
            }

            override fun onReconnecting(attempt: Int) {
                logDebug("Socket reconnecting, attempt: $attempt")
            }
        })

        socketManager.connect(sessionId)
    }

    /**
     * Set listener for conversation processing results.
     *
     * @param listener Listener to receive conversation results from Socket.IO
     */
    fun setConversationListener(listener: ConversationListener?) {
        this.conversationListener = listener
    }

    /**
     * Check if socket is connected.
     *
     * @return True if socket is currently connected
     */
    fun isSocketConnected(): Boolean = socketManager.isConnected()

    /**
     * Disconnect from socket server.
     */
    fun disconnectSocket() {
        socketManager.disconnect()
        logDebug("Socket disconnected manually")
    }

    /**
     * Reconnect to socket server.
     */
    fun reconnectSocket() {
        val sessionId = sessionManager.getSessionId()
        if (sessionId != null) {
            socketManager.connect(sessionId)
            logDebug("Socket reconnecting")
        } else {
            logError("Cannot reconnect: no session ID")
        }
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

        // Save to backend
        scope.launch {
            try {
                val response = apiClient.saveScreens(listOf(screen))
                logDebug("Screen saved to backend: ${response.updatedScreenCount}")
                callback?.onSuccess(response.updatedScreenCount)
            } catch (e: Exception) {
                logError("Failed to save screen to backend", e)
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

        // Save all screens to backend
        scope.launch {
            try {
                val response = apiClient.saveScreens(screens)
                logDebug("Screens saved to backend: ${response.updatedScreenCount}")
                callback?.onSuccess(response.updatedScreenCount)
            } catch (e: Exception) {
                logError("Failed to save screens to backend", e)
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
     * Listener interface for conversation processing results.
     */
    interface ConversationListener {
        /**
         * Called when a conversation has been processed.
         *
         * @param event The processed conversation event with transcription and navigation
         */
        fun onConversationProcessed(event: ConversationProcessedEvent)

        /**
         * Called when an error occurs.
         *
         * @param error The error that occurred
         */
        fun onError(error: VikaError)
    }

    /**
     * Send audio for conversation processing.
     *
     * Results will be delivered through the ConversationListener via Socket.IO.
     *
     * @param audioFile Audio file to send (mp3, wav, m4a, ogg, webm)
     * @return ConversationResponse with conversation ID for tracking
     */
    suspend fun sendConversation(audioFile: File): ConversationResponse {
        return withContext(Dispatchers.IO) {
            apiClient.sendConversation(audioFile)
        }
    }

    /**
     * Send audio for conversation processing with callback.
     *
     * @param audioFile Audio file to send
     * @param callback Callback for immediate response (conversation ID)
     */
    fun sendConversation(
        audioFile: File,
        callback: ConversationCallback
    ) {
        scope.launch {
            try {
                callback.onStarted()

                val response = withContext(Dispatchers.IO) {
                    apiClient.sendConversation(audioFile)
                }

                logDebug("Conversation submitted: ${response.conversationId}")
                callback.onSuccess(response)
            } catch (e: Exception) {
                logError("Send conversation failed", e)
                callback.onError(e)
            }
        }
    }

    /**
     * Callback interface for conversation submission
     */
    interface ConversationCallback {
        fun onStarted()
        fun onSuccess(response: ConversationResponse)
        fun onError(error: Throwable)
    }

    /**
     * Execute navigation from deep link
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
        // Check if backend is initialized
        if (!isBackendInitialized) {
            logError("Cannot open VIKA SDK: Backend not initialized")
            Toast.makeText(
                context,
                "VIKA SDK not ready. Please check your network connection.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

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
     * Check if SDK backend is initialized.
     *
     * @return True if backend initialization was successful
     */
    fun isBackendReady(): Boolean = isBackendInitialized

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
     * Get the SDK configuration.
     */
    fun getConfig(): SDKConfig = config

    /**
     * Clean up resources
     */
    private fun cleanup() {
        socketManager.disconnect()
        apiClient.clearSession()
        sessionManager.clearSession()
        scope.cancel()
        registeredScreens.clear()
        navigationHandlers.clear()
        conversationListener = null
        isBackendInitialized = false
        logDebug("SDK cleaned up")
    }

    private fun logDebug(message: String) {
        SafeLogger.d(TAG, message)
    }

    private fun logError(message: String, throwable: Throwable? = null) {
        SafeLogger.e(TAG, message, throwable)
    }

    /**
     * Send conversation and get result with typed error handling.
     *
     * @param audioFile Audio file to send
     * @return Result containing ConversationResponse or VikaError
     */
    suspend fun sendConversationAsync(audioFile: File): VikaResult<ConversationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Check rate limiting
                if (securityManager.isRateLimited()) {
                    return@withContext VikaResult.failure(VikaError.RateLimited())
                }

                // Check session validity
                if (!sessionManager.hasSession()) {
                    return@withContext VikaResult.failure(
                        VikaError.Authentication("Session not available. Please reinitialize.")
                    )
                }

                val response = apiClient.sendConversation(audioFile)

                logDebug("Conversation submitted: ${response.conversationId}")
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
     * Save screens and get result with typed error handling.
     *
     * @param screens List of screens to save
     * @return Result containing updated count or VikaError
     */
    suspend fun saveScreensAsync(screens: List<ScreenRegistration>): VikaResult<Int> {
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

                val response = apiClient.saveScreens(screens)
                logDebug("Saved ${response.updatedScreenCount} screens to backend")
                VikaResult.success(response.updatedScreenCount)
            } catch (e: NetworkException) {
                VikaResult.failure(VikaError.Network(e.message ?: "Network error", e))
            } catch (e: Exception) {
                VikaResult.failure(VikaError.Unknown(e.message ?: "Unknown error", e))
            }
        }
    }
}
