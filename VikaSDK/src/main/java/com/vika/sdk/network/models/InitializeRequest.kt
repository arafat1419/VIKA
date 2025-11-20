package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName

/**
 * Request model for SDK initialization.
 *
 * @property apiKey API key for authentication
 * @property timestamp Request timestamp in milliseconds
 * @property signature Request signature for verification
 */
internal data class InitializeRequest(
    @SerializedName("api_key")
    val apiKey: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("signature")
    val signature: String
)
