package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName
import com.vika.sdk.models.SecureScreen

/**
 * Request model for saving screens with the backend.
 *
 * @property screens List of screens to save
 */
internal data class ScreenRequest(
    @SerializedName("screens")
    val screens: List<SecureScreen>
)
