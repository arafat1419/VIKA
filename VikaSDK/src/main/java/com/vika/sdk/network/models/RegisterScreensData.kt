package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName

/**
 * Data payload for screen registration response.
 *
 * @property registeredCount Number of screens successfully registered
 */
data class RegisterScreensData(
    @SerializedName("registered_count")
    val registeredCount: Int
)
