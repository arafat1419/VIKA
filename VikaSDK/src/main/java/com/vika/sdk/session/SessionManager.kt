package com.vika.sdk.session

/**
 * Manages SDK session lifecycle.
 *
 * Handles session token storage. Session validity is determined
 * by socket connection status rather than expiration time.
 */
internal class SessionManager {
    private var sessionId: String? = null

    /**
     * Store a new session.
     *
     * @param id Session ID from backend
     */
    fun setSession(id: String) {
        sessionId = id
    }

    /**
     * Get current session ID.
     *
     * @return Session ID or null if not initialized
     */
    fun getSessionId(): String? = sessionId

    /**
     * Check if a session exists.
     *
     * @return True if session exists
     */
    fun hasSession(): Boolean = sessionId != null

    /**
     * Clear the current session.
     */
    fun clearSession() {
        sessionId = null
    }
}
