package com.vika.sdk.models

/**
 * Types of navigation supported
 */
sealed class NavigationType {
    data class DeepLink(val uri: String) : NavigationType()
}