package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName

/**
 * Response model for conversation endpoint.
 *
 * @property status Audio reception status
 * @property message Response message
 * @property conversationId UUID of created conversation record
 */
data class ConversationResponse(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("conversation_id")
    val conversationId: String
)
