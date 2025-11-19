package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName

/**
 * Request model for SDK initialization.
 *
 * @property apiKey API key for authentication
 * @property appPackage Application package name
 * @property sdkVersion SDK version string
 * @property timestamp Request timestamp in milliseconds
 */
internal data class InitializeRequest(
    @SerializedName("api_key")
    val apiKey: String,
    @SerializedName("app_package")
    val appPackage: String,
    @SerializedName("sdk_version")
    val sdkVersion: String,
    @SerializedName("timestamp")
    val timestamp: Long
)
