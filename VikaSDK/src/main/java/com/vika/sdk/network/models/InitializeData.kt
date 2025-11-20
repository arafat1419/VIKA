package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName

/**
 * Data payload for initialization response.
 *
 * @property sessionId Session ID for subsequent requests (used as Bearer token)
 */
data class InitializeData(
    @SerializedName("session_id")
    val sessionId: String
)
