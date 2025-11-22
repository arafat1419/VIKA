package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName

/**
 * Event emitted when transcription is completed
 * This arrives before conversation_processed event
 */
data class TranscriptionCompletedEvent(
    @SerializedName("conversation_id")
    val conversationId: String,

    @SerializedName("transcription")
    val transcription: String
)
