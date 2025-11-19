package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName
import com.vika.sdk.models.SecureScreen

/**
 * Request model for registering screens with the backend.
 *
 * @property sessionId Current session ID
 * @property screens List of screens to register
 * @property timestamp Request timestamp in milliseconds
 */
internal data class RegisterScreensRequest(
    @SerializedName("session_id")
    val sessionId: String,
    @SerializedName("screens")
    val screens: List<SecureScreen>,
    @SerializedName("timestamp")
    val timestamp: Long
)
