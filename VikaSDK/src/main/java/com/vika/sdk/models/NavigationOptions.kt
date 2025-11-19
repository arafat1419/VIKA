package com.vika.sdk.models

/**
 * Navigation options
 */
data class NavigationOptions(
    val executeImmediately: Boolean = true,
    val useCache: Boolean = true,
    val timeout: Long? = null
)