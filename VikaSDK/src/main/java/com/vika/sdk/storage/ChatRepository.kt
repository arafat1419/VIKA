package com.vika.sdk.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vika.sdk.models.ChatMessage
import com.vika.sdk.utils.SafeLogger

/**
 * Repository for managing chat message persistence using SharedPreferences
 */
internal class ChatRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    /**
     * Save a chat message for the current session
     */
    fun saveMessage(sessionId: String, message: ChatMessage) {
        val messages = getMessagesForSession(sessionId).toMutableList()
        messages.add(message)
        saveMessages(sessionId, messages)
        SafeLogger.d(TAG, "Message saved for session: $sessionId")
    }

    /**
     * Update a placeholder message with actual content
     */
    fun updatePlaceholder(sessionId: String, conversationId: String, newMessage: String) {
        val messages = getMessagesForSession(sessionId).toMutableList()
        val index = messages.indexOfFirst {
            it.conversationId == conversationId && it.isPlaceholder
        }

        if (index != -1) {
            messages[index] = messages[index].copy(
                message = newMessage,
                isPlaceholder = false
            )
            saveMessages(sessionId, messages)
            SafeLogger.d(TAG, "Placeholder updated for conversation: $conversationId")
        }
    }

    /**
     * Get all messages for a specific session
     */
    fun getMessagesForSession(sessionId: String): List<ChatMessage> {
        val key = getChatKey(sessionId)
        val json = prefs.getString(key, null) ?: return emptyList()

        return try {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            SafeLogger.e(TAG, "Error parsing messages for session: $sessionId", e)
            emptyList()
        }
    }

    /**
     * Clear all messages for a specific session
     */
    fun clearSession(sessionId: String) {
        val key = getChatKey(sessionId)
        prefs.edit().remove(key).apply()
        SafeLogger.d(TAG, "Chat cleared for session: $sessionId")
    }

    /**
     * Clear all chat data
     */
    fun clearAll() {
        prefs.edit().clear().apply()
        SafeLogger.d(TAG, "All chat data cleared")
    }

    private fun saveMessages(sessionId: String, messages: List<ChatMessage>) {
        val key = getChatKey(sessionId)
        val json = gson.toJson(messages)
        prefs.edit().putString(key, json).apply()
    }

    private fun getChatKey(sessionId: String): String {
        return "${CHAT_KEY_PREFIX}${sessionId}"
    }

    companion object {
        private const val TAG = "ChatRepository"
        private const val PREFS_NAME = "vika_chat_prefs"
        private const val CHAT_KEY_PREFIX = "vika_chat_"
    }
}
