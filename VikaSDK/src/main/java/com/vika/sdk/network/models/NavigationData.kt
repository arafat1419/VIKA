package com.vika.sdk.network.models

import com.google.gson.annotations.SerializedName

/**
 * Navigation data from conversation response.
 *
 * Contains the matched screen information and confidence score.
 *
 * @property screenId ID of the matched screen
 * @property screenName Name of the matched screen
 * @property deepLink Deep link URI for navigation
 * @property confidence Confidence score of the match (0.0 to 1.0)
 */
data class NavigationData(
    @SerializedName("screen_id")
    val screenId: String,
    @SerializedName("screen_name")
    val screenName: String,
    @SerializedName("deep_link")
    val deepLink: String,
    @SerializedName("confidence")
    val confidence: Float
)
