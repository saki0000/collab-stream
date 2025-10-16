package org.example.project.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using kotlinx.serialization.
 *
 * These routes are used with Navigation Compose's type-safe navigation APIs.
 * Each route represents a destination in the app's navigation graph.
 */

/**
 * Home route - Initial screen with group selection and search options
 */
@Serializable
data object HomeRoute

/**
 * Streamer search route - Bottom sheet for searching streamers (Main or Sub)
 *
 * @param searchMode Mode indicating whether searching for MAIN or SUB streamer
 */
@Serializable
data class StreamerSearchRoute(
    val searchMode: String, // "MAIN" or "SUB"
)

/**
 * Main player route - Main video player screen with sync functionality
 *
 * @param mainStreamId Video ID of the main stream
 * @param mainChannelId Channel ID of the main streamer
 * @param mainChannelName Channel name of the main streamer
 * @param mainServiceType Service type (YOUTUBE or TWITCH)
 * @param mainThumbnailUrl Thumbnail URL
 * @param mainTitle Video title
 * @param mainChannelIconUrl Channel icon URL
 * @param mainIsLive Whether stream is live
 * @param mainPublishedAt Published timestamp (epoch seconds)
 */
@Serializable
data class MainPlayerRoute(
    val mainStreamId: String,
    val mainChannelId: String,
    val mainChannelName: String,
    val mainServiceType: String,
    val mainThumbnailUrl: String = "",
    val mainTitle: String = "",
    val mainChannelIconUrl: String = "",
    val mainIsLive: Boolean = false,
    val mainPublishedAt: Long = 0L, // epoch seconds
)

/**
 * Video search route - Bottom sheet for searching videos (DEPRECATED, use StreamerSearchRoute)
 *
 * @param initialQuery Optional initial search query to populate the search field
 */
@Serializable
data class VideoSearchRoute(
    val initialQuery: String = "",
)

/**
 * Enum representing streamer search mode
 */
enum class StreamerSearchMode {
    MAIN,
    SUB,
}
