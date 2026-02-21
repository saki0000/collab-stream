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
 * Timeline sync route - Main screen for timeline synchronization
 *
 * presetDate: ISO日付文字列（例: "2024-01-15"）。null の場合は今日を使用する。
 * presetChannelsJson: JSON配列文字列（PresetChannelリスト）。null の場合はプリセットなし。
 *
 * US-4: ArchiveHome -> TimelineSync 遷移時にプリセット情報を渡す。
 */
@Serializable
data class TimelineSyncRoute(
    val presetDate: String? = null,
    val presetChannelsJson: String? = null,
)

/**
 * アーカイブHome → TimelineSync 遷移時に渡すチャンネルプリセット情報。
 *
 * ArchiveItem → PresetChannel → SyncChannel の変換チェーンに使用する。
 *
 * US-4: Archive Selection
 */
@Serializable
data class PresetChannel(
    val channelId: String,
    val channelName: String,
    val channelIconUrl: String,
    val serviceType: String,
)

/**
 * Archive Home route - Main screen for viewing followed channels' archives
 *
 * Displays archives from followed channels for a selected date.
 *
 * Epic: Channel Follow & Archive Home (US-3)
 * Story: US-3 (Archive Home Display)
 */
@Serializable
data object ArchiveHomeRoute

/**
 * Subscription route - Subscription management screen
 *
 * Displays current subscription status (Free/Pro) and allows the user
 * to upgrade to Pro or restore past purchases.
 *
 * Feature: サブスクリプション管理 (US-4)
 */
@Serializable
data object SubscriptionRoute

/**
 * Enum representing streamer search mode
 */
enum class StreamerSearchMode {
    MAIN,
    SUB,
}
