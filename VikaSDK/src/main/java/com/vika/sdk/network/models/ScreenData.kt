package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName

/**
 * Data payload for screen registration response.
 *
 * @property updatedScreenCount Number of screens successfully updated
 */
data class ScreenData(
    @SerializedName("updated_screen_count")
    val updatedScreenCount: Int
)
