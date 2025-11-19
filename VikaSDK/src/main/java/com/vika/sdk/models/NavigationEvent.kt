package com.vika.sdk.models

/**
 * Analytics event for navigation
 */
internal data class NavigationEvent(
    val eventId: String = generateEventId(),
    val timestamp: Long = System.currentTimeMillis(),
    val userQuery: String,
    val screenId: String?,
    val success: Boolean,
    val parameters: Map<String, Any> = emptyMap(),
    val confidence: Float? = null,
    val errorType: String? = null,
    val processingTime: Long? = null,
    val sessionId: String? = null,
    val userId: String? = null
) {
    companion object {
        private fun generateEventId(): String {
            return "${System.currentTimeMillis()}_${(0..999999).random()}"
        }
    }
}