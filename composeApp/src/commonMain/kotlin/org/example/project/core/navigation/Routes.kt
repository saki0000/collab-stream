package org.example.project.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using kotlinx.serialization.
 *
 * These routes are used with Navigation Compose's type-safe navigation APIs.
 * Each route represents a destination in the app's navigation graph.
 */

/**
 * Home route - Main video player screen with multi-video sync support
 *
 * @param mainVideoId Main video ID (primary video for sync)
 * @param mainServiceType Main video service type (YOUTUBE or TWITCH)
 * @param subVideoId Sub video ID (secondary video to be synced)
 * @param subServiceType Sub video service type (YOUTUBE or TWITCH)
 */
@Serializable
data class HomeRoute(
    val mainVideoId: String? = null,
    val mainServiceType: String? = null,
    val subVideoId: String? = null,
    val subServiceType: String? = null,
)

/**
 * Video search route - Bottom sheet for searching videos
 *
 * @param initialQuery Optional initial search query to populate the search field
 * @param selectionTarget Target for video selection: "MAIN" for main video, "SUB" for sub video
 */
@Serializable
data class VideoSearchRoute(
    val initialQuery: String = "",
    val selectionTarget: String = "MAIN", // "MAIN" or "SUB"
)
