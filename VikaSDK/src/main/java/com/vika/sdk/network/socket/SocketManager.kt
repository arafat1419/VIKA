package com.vika.sdk.network.socket

import com.google.gson.Gson
import com.vika.sdk.BuildConfig
import com.vika.sdk.models.SDKConfig
import com.vika.sdk.network.models.ConversationProcessedEvent
import com.vika.sdk.utils.SafeLogger
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URI

/**
 * Manages Socket.IO connection to the VIKA backend.
 *
 * Handles connection lifecycle, auto-reconnection with exponential backoff,
 * and event dispatching to registered listeners.
 *
 * @param config SDK configuration for socket settings
 */
internal class SocketManager(
    private val config: SDKConfig
) {
    private var socket: Socket? = null
    private var listener: SocketEventListener? = null
    private var sessionId: String? = null
    private var isConnecting = false
    private val gson = Gson()

    companion object {
        private const val TAG = "SocketManager"
        private const val EVENT_CONNECT = Socket.EVENT_CONNECT
        private const val EVENT_DISCONNECT = Socket.EVENT_DISCONNECT
        private const val EVENT_CONNECT_ERROR = Socket.EVENT_CONNECT_ERROR
        private const val EVENT_CONNECTED = "connected"
        private const val EVENT_CONVERSATION_PROCESSED = "conversation_processed"
    }

    /**
     * Connect to the socket server with the given session ID.
     *
     * @param sessionId Session ID from initialization (used for authentication)
     */
    fun connect(sessionId: String) {
        if (socket?.connected() == true) {
            return
        }

        this.sessionId = sessionId
        isConnecting = true

        SafeLogger.d(TAG, "Connecting to socket: ${BuildConfig.SOCKET_URL}")

        try {
            val options = IO.Options().apply {
                auth = mapOf("session_id" to sessionId)
                reconnection = true
                reconnectionAttempts = config.socketReconnectionAttempts
                reconnectionDelay = config.socketReconnectionDelay
                reconnectionDelayMax = config.socketReconnectionDelay * 10
                timeout = config.timeoutMillis
            }

            socket = IO.socket(URI.create(BuildConfig.SOCKET_URL), options).apply {
                on(EVENT_CONNECT, onConnect)
                on(EVENT_DISCONNECT, onDisconnect)
                on(EVENT_CONNECT_ERROR, onConnectError)
                on(EVENT_CONNECTED, onConnected)
                on(EVENT_CONVERSATION_PROCESSED, onConversationProcessed)
            }

            // Listen for reconnect attempts on the manager
            socket?.io()?.on("reconnect_attempt", onReconnectAttempt)

            socket?.connect()
            SafeLogger.d(TAG, "Socket connect() called")
        } catch (e: Exception) {
            isConnecting = false
            SafeLogger.e(TAG, "Socket connection failed", e)
            listener?.onError("Failed to connect: ${e.message}")
        }
    }

    /**
     * Disconnect from the socket server.
     */
    fun disconnect() {
        SafeLogger.d(TAG, "Disconnecting socket")
        socket?.apply {
            off()
            disconnect()
        }
        socket = null
        sessionId = null
        isConnecting = false
    }

    /**
     * Check if the socket is currently connected.
     */
    fun isConnected(): Boolean = socket?.connected() == true

    /**
     * Set the event listener for socket events.
     *
     * @param listener Listener to receive socket events
     */
    fun setEventListener(listener: SocketEventListener?) {
        this.listener = listener
    }

    /**
     * Get the current session ID.
     */
    fun getSessionId(): String? = sessionId

    private val onConnect = Emitter.Listener {
        isConnecting = false
        SafeLogger.d(TAG, "Socket EVENT_CONNECT received")
    }

    private val onDisconnect = Emitter.Listener {
        SafeLogger.d(TAG, "Socket EVENT_DISCONNECT received")
        listener?.onDisconnected()
    }

    private val onConnectError = Emitter.Listener { args ->
        isConnecting = false
        val error = if (args.isNotEmpty()) {
            args[0]?.toString() ?: "Unknown error"
        } else {
            "Connection error"
        }
        SafeLogger.e(TAG, "Socket EVENT_CONNECT_ERROR: $error")
        listener?.onError(error)
    }

    private val onConnected = Emitter.Listener { args ->
        SafeLogger.d(TAG, "Socket 'connected' event received")
        if (args.isNotEmpty()) {
            try {
                val data = args[0] as? JSONObject
                SafeLogger.d(TAG, "Connected data: $data")
                val responseSessionId = data?.optString("session_id") ?: sessionId ?: ""
                listener?.onConnected(responseSessionId)
            } catch (e: Exception) {
                SafeLogger.e(TAG, "Failed to parse connected data", e)
                listener?.onConnected(sessionId ?: "")
            }
        } else {
            listener?.onConnected(sessionId ?: "")
        }
    }

    private val onConversationProcessed = Emitter.Listener { args ->
        SafeLogger.d(TAG, "Socket 'conversation_processed' event received")
        if (args.isNotEmpty()) {
            try {
                val jsonData = args[0].toString()
                SafeLogger.d(TAG, "Conversation data: $jsonData")
                val event = gson.fromJson(jsonData, ConversationProcessedEvent::class.java)
                listener?.onConversationProcessed(event)
            } catch (e: Exception) {
                SafeLogger.e(TAG, "Failed to parse conversation result", e)
                listener?.onError("Failed to parse conversation result: ${e.message}")
            }
        }
    }

    private val onReconnectAttempt = Emitter.Listener { args ->
        val attempt = if (args.isNotEmpty()) {
            (args[0] as? Number)?.toInt() ?: 1
        } else {
            1
        }
        SafeLogger.d(TAG, "Socket reconnect attempt: $attempt")
        listener?.onReconnecting(attempt)
    }
}
