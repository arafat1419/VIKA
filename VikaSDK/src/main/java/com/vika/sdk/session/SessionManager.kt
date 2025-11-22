package com.vika.sdk.session

import android.content.Context
import android.content.SharedPreferences
import com.vika.sdk.utils.SafeLogger

/**
 * Listener for session lifecycle events
 */
internal interface SessionChangeListener {
    fun onSessionChanged(oldSessionId: String?, newSessionId: String?)
    fun onSessionCleared(sessionId: String?)
}

/**
 * Manages SDK session lifecycle.
 *
 * Handles session token storage with persistence. Session validity is determined
 * by socket connection status rather than expiration time.
 */
internal class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private var sessionId: String? = null
    private val listeners = mutableListOf<SessionChangeListener>()

    init {
        sessionId = prefs.getString(KEY_SESSION_ID, null)
        SafeLogger.d(TAG, "SessionManager initialized with session: ${sessionId?.take(8)}")
    }

    /**
     * Add a listener for session changes
     */
    fun addSessionChangeListener(listener: SessionChangeListener) {
        listeners.add(listener)
    }

    /**
     * Remove a listener for session changes
     */
    fun removeSessionChangeListener(listener: SessionChangeListener) {
        listeners.remove(listener)
    }

    /**
     * Store a new session.
     *
     * @param id Session ID from backend
     */
    fun setSession(id: String) {
        val oldSessionId = sessionId
        sessionId = id

        prefs.edit().putString(KEY_SESSION_ID, id).apply()

        if (oldSessionId != id) {
            SafeLogger.d(TAG, "Session changed: ${oldSessionId?.take(8)} -> ${id.take(8)}")
            listeners.forEach { it.onSessionChanged(oldSessionId, id) }
        }
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
        val oldSessionId = sessionId
        sessionId = null

        prefs.edit().remove(KEY_SESSION_ID).apply()

        SafeLogger.d(TAG, "Session cleared: ${oldSessionId?.take(8)}")
        listeners.forEach { it.onSessionCleared(oldSessionId) }
    }

    companion object {
        private const val TAG = "SessionManager"
        private const val PREFS_NAME = "vika_session_prefs"
        private const val KEY_SESSION_ID = "session_id"
    }
}
