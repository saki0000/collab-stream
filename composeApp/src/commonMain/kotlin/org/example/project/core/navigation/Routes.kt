package org.example.project.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using kotlinx.serialization.
 *
 * These routes are used with Navigation Compose's type-safe navigation APIs.
 * Each route represents a destination in the app's navigation graph.
 */

/**
 * Home route - Main video player screen
 *
 * @param selectedVideoId Optional video ID from video search (used for backward navigation)
 * @param selectedServiceType Optional service type name (YOUTUBE or TWITCH) from video search
 */
@Serializable
data class HomeRoute(
    val selectedVideoId: String? = null,
    val selectedServiceType: String? = null,
)

/**
 * Video search route - Bottom sheet for searching videos
 *
 * @param initialQuery Optional initial search query to populate the search field
 */
@Serializable
data class VideoSearchRoute(
    val initialQuery: String = "",
)
