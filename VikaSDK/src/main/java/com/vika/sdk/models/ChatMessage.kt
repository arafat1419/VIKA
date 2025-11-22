package com.vika.sdk.models

/**
 * Represents a single chat message in the conversation
 */
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val message: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis(),
    val isPlaceholder: Boolean = false
)

/**
 * Indicates the sender of a chat message
 */
enum class MessageSender {
    USER,
    AI
}
