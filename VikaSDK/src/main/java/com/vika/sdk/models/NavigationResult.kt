package com.vika.sdk.models

/**
 * Result from navigation query
 */
data class NavigationResult(
    val screenId: String,
    val deepLink: String,
    val confidence: Float,
    val metadata: NavigationMetadata? = null
)

/**
 * Additional metadata for navigation result
 */
data class NavigationMetadata(
    val processingTime: Long,
    val alternativeScreens: List<AlternativeScreen> = emptyList(),
    val extractedEntities: List<Entity> = emptyList()
)

/**
 * Alternative screen suggestions
 */
data class AlternativeScreen(
    val screenId: String,
    val confidence: Float
)

/**
 * Extracted entity from query
 */
data class Entity(
    val type: String,
    val value: String,
    val confidence: Float
)