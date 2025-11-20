package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName

/**
 * Socket event model for conversation processed results.
 *
 * Received through the 'conversation_processed' socket event.
 *
 * @property conversationId UUID of the conversation
 * @property status Processing status (e.g., "completed")
 * @property result The result data containing transcription, reply, and navigation
 */
data class ConversationProcessedEvent(
    @SerializedName("conversation_id")
    val conversationId: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("result")
    val result: ConversationResult
)

/**
 * Result data from conversation processing.
 *
 * @property transcription Transcribed text from user's voice
 * @property replyText AI-generated text reply
 * @property replyAudioUrl Relative URL path to download AI voice reply audio
 * @property navigation Navigation instructions if a screen match was found
 */
data class ConversationResult(
    @SerializedName("transcription")
    val transcription: String,
    @SerializedName("reply_text")
    val replyText: String,
    @SerializedName("reply_audio_url")
    val replyAudioUrl: String? = null,
    @SerializedName("navigation")
    val navigation: NavigationData? = null
)
