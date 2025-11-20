package com.vika.sdk.network.socket

import com.vika.sdk.network.models.ConversationProcessedEvent

/**
 * Interface for receiving Socket.IO events from the VIKA backend.
 *
 * Implement this interface to handle real-time events such as
 * connection status changes and conversation processing results.
 */
interface SocketEventListener {
    /**
     * Called when successfully connected to the socket server.
     *
     * @param sessionId The session ID used for connection
     */
    fun onConnected(sessionId: String)

    /**
     * Called when disconnected from the socket server.
     */
    fun onDisconnected()

    /**
     * Called when a conversation has been processed by the backend.
     *
     * Contains transcription, AI reply, and navigation data.
     *
     * @param event The processed conversation result
     */
    fun onConversationProcessed(event: ConversationProcessedEvent)

    /**
     * Called when a socket error occurs.
     *
     * @param error Error message describing what went wrong
     */
    fun onError(error: String)

    /**
     * Called when attempting to reconnect to the socket server.
     *
     * @param attempt Current reconnection attempt number
     */
    fun onReconnecting(attempt: Int)
}
