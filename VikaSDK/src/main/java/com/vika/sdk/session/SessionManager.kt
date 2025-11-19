package com.vika.sdk.session

/**
 * Manages SDK session lifecycle including authentication and expiration.
 *
 * Handles session token storage, expiration checking, and automatic refresh.
 */
internal class SessionManager {
    private var sessionId: String? = null
    private var expiresAt: Long? = null

    /**
     * Store a new session.
     *
     * @param id Session ID from backend
     * @param expirationTime Session expiration timestamp in milliseconds
     */
    fun setSession(id: String, expirationTime: Long?) {
        sessionId = id
        expiresAt = expirationTime
    }

    /**
     * Get current session ID.
     *
     * @return Session ID or null if not initialized
     */
    fun getSessionId(): String? = sessionId

    /**
     * Check if a valid session exists.
     *
     * @return True if session exists and has not expired
     */
    fun hasValidSession(): Boolean {
        val id = sessionId ?: return false
        val expiry = expiresAt ?: return true // No expiry means valid

        return System.currentTimeMillis() < expiry
    }

    /**
     * Check if session is expired.
     *
     * @return True if session exists but has expired
     */
    fun isSessionExpired(): Boolean {
        sessionId ?: return false // No session = not expired
        val expiry = expiresAt ?: return false // No expiry = not expired

        return System.currentTimeMillis() >= expiry
    }

    /**
     * Check if session needs refresh (within grace period before expiration).
     *
     * @param gracePeriodMs Milliseconds before expiration to trigger refresh
     * @return True if session should be refreshed
     */
    fun needsRefresh(gracePeriodMs: Long = REFRESH_GRACE_PERIOD_MS): Boolean {
        val expiry = expiresAt ?: return false

        return System.currentTimeMillis() >= (expiry - gracePeriodMs)
    }

    /**
     * Clear the current session.
     */
    fun clearSession() {
        sessionId = null
        expiresAt = null
    }

    /**
     * Get time until session expires.
     *
     * @return Milliseconds until expiration, or null if no expiration set
     */
    fun getTimeUntilExpiration(): Long? {
        val expiry = expiresAt ?: return null
        return (expiry - System.currentTimeMillis()).coerceAtLeast(0)
    }

    companion object {
        /** Default grace period before expiration to trigger refresh (5 minutes) */
        const val REFRESH_GRACE_PERIOD_MS = 5 * 60 * 1000L
    }
}
