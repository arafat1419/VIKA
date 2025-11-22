package com.vika.sdk.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vika.sdk.models.ChatMessage
import com.vika.sdk.models.MessageSender
import com.vika.sdk.session.SessionChangeListener
import com.vika.sdk.storage.ChatRepository
import com.vika.sdk.utils.SafeLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * ViewModel for managing chat messages and state
 */
internal class VikaViewModel(
    private val context: Context,
    private val chatRepository: ChatRepository
) : ViewModel(), SessionChangeListener {

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private var currentSessionId: String? = null

    /**
     * Load messages for a session
     */
    fun loadMessages(sessionId: String) {
        if (currentSessionId != sessionId) {
            currentSessionId = sessionId
            val messages = chatRepository.getMessagesForSession(sessionId)
            _chatMessages.value = messages
            SafeLogger.d(TAG, "Loaded ${messages.size} messages for session: ${sessionId.take(8)}")
        }
    }

    /**
     * Add user message and placeholder AI message
     */
    fun addUserMessage(sessionId: String, conversationId: String, userTranscription: String) {
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            message = userTranscription,
            sender = MessageSender.USER
        )

        val placeholderMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            message = "...",
            sender = MessageSender.AI,
            isPlaceholder = true
        )

        chatRepository.saveMessage(sessionId, userMessage)
        chatRepository.saveMessage(sessionId, placeholderMessage)

        _chatMessages.value = chatRepository.getMessagesForSession(sessionId)
        SafeLogger.d(
            TAG,
            "Added user message and placeholder for conversation: ${conversationId.take(8)}"
        )
    }

    /**
     * Update placeholder AI message with actual reply
     */
    fun updateAIMessage(sessionId: String, conversationId: String, aiReply: String) {
        chatRepository.updatePlaceholder(sessionId, conversationId, aiReply)
        _chatMessages.value = chatRepository.getMessagesForSession(sessionId)
        SafeLogger.d(TAG, "Updated AI message for conversation: ${conversationId.take(8)}")
    }

    /**
     * Clear chat messages for current session
     */
    fun clearChat(sessionId: String) {
        chatRepository.clearSession(sessionId)
        _chatMessages.value = emptyList()
        SafeLogger.d(TAG, "Chat cleared for session: ${sessionId.take(8)}")
    }

    override fun onSessionChanged(oldSessionId: String?, newSessionId: String?) {
        oldSessionId?.let { chatRepository.clearSession(it) }
        _chatMessages.value = emptyList()
        currentSessionId = newSessionId
        SafeLogger.d(TAG, "Session changed, chat cleared")
    }

    override fun onSessionCleared(sessionId: String?) {
        sessionId?.let { chatRepository.clearSession(it) }
        _chatMessages.value = emptyList()
        currentSessionId = null
        SafeLogger.d(TAG, "Session cleared, chat cleared")
    }

    companion object {
        private const val TAG = "VikaViewModel"
    }
}

/**
 * Factory for creating VikaViewModel instances
 */
internal class VikaViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VikaViewModel::class.java)) {
            val chatRepository = ChatRepository(context)
            return VikaViewModel(context, chatRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
