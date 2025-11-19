package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName

/**
 * Data payload for initialization response.
 *
 * @property sessionId Session ID for subsequent requests
 * @property expiresAt Session expiration timestamp in milliseconds
 */
data class InitializeData(
    @SerializedName("session_id")
    val sessionId: String,
    @SerializedName("expires_at")
    val expiresAt: Long? = null
)
