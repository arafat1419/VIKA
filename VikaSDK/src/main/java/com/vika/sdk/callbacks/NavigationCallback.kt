package com.vika.sdk.callbacks

import com.vika.sdk.models.NavigationResult

/**
 * Callback interface for navigation operations
 */
interface NavigationCallback {
    /**
     * Called when navigation starts
     */
    fun onNavigationStarted()

    /**
     * Called when navigation succeeds
     */
    fun onNavigationSuccess(result: NavigationResult)

    /**
     * Called when navigation fails
     */
    fun onNavigationFailed(error: NavigationError)
}

/**
 * Simple implementation of NavigationCallback
 */
open class SimpleNavigationCallback : NavigationCallback {
    override fun onNavigationStarted() {
        // Default empty implementation
    }

    override fun onNavigationSuccess(result: NavigationResult) {
        // Default empty implementation
    }

    override fun onNavigationFailed(error: NavigationError) {
        // Default empty implementation
    }
}