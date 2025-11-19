package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper.
 *
 * Standard response format for all API endpoints.
 *
 * @param T Type of data payload
 * @property status Response status string
 * @property message Human-readable message
 * @property data Response payload
 */
data class ApiResponse<T>(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T? = null
)
