@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.VideoServiceType

/**
 * Sealed interface defining all possible user intents for Timeline Sync screen.
 * Following MVI architecture pattern for state management.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display), US-2 (Channel Add/Remove)
 */
sealed interface TimelineSyncIntent {
    /**
     * Load screen data (channels and streams).
     * Called when the screen is first displayed.
     */
    data object LoadScreen : TimelineSyncIntent

    /**
     * Select a specific date in the calendar.
     * Updates the timeline to show streams for the selected date.
     */
    data class SelectDate(val date: LocalDate) : TimelineSyncIntent

    /**
     * Navigate to the previous week in the calendar.
     * Does not change the selected date.
     */
    data object NavigateToPreviousWeek : TimelineSyncIntent

    /**
     * Navigate to the next week in the calendar.
     * Does not change the selected date.
     */
    data object NavigateToNextWeek : TimelineSyncIntent

    /**
     * Clear any error message.
     */
    data object ClearError : TimelineSyncIntent

    /**
     * Retry loading data after an error.
     */
    data object Retry : TimelineSyncIntent

    // ============================================
    // Story 3: Sync Time Selection
    // ============================================

    /**
     * Update sync time while dragging the sync line.
     * Updates the sync time in real-time during drag operation.
     */
    data class UpdateSyncTime(val syncTime: Instant) : TimelineSyncIntent

    /**
     * Start dragging the sync line.
     * Sets isDragging flag to true.
     */
    data object StartDragging : TimelineSyncIntent

    /**
     * Stop dragging the sync line.
     * Sets isDragging flag to false and finalizes the sync time.
     */
    data object StopDragging : TimelineSyncIntent

    // ============================================
    // Story 2: Channel Add/Remove
    // ============================================

    /**
     * チャンネル追加モーダルのプラットフォームを選択する。
     * 検索結果はクリアされ、クエリが空でない場合は再検索が実行される。
     * Story 5: Multi-Platform Search
     */
    data class SelectPlatform(val platform: VideoServiceType) : TimelineSyncIntent

    /**
     * Open the channel add modal (bottom sheet).
     */
    data object OpenChannelAddModal : TimelineSyncIntent

    /**
     * Close the channel add modal.
     * Resets search query and suggestions.
     */
    data object CloseChannelAddModal : TimelineSyncIntent

    /**
     * Update the channel search query.
     * Triggers debounced channel search.
     */
    data class UpdateChannelSearchQuery(val query: String) : TimelineSyncIntent

    /**
     * Add a channel to the timeline.
     * Converts ChannelInfo to SyncChannel.
     */
    data class AddChannel(val channel: ChannelInfo) : TimelineSyncIntent

    /**
     * Remove a channel from the timeline.
     * Stores the channel for undo functionality.
     */
    data class RemoveChannel(val channelId: String) : TimelineSyncIntent

    /**
     * Undo the most recent channel removal.
     * Restores the recently deleted channel.
     */
    data object UndoRemoveChannel : TimelineSyncIntent

    /**
     * Clear the channel add error message.
     */
    data object ClearChannelAddError : TimelineSyncIntent

    // ============================================
    // Story 4: External App Navigation
    // ============================================

    /**
     * 外部アプリでチャンネルの動画を開く。
     * READY または OPENED 状態のチャンネルで有効。
     */
    data class OpenExternalApp(val channelId: String) : TimelineSyncIntent

    // ============================================
    // Channel Follow (US-2)
    // ============================================

    /**
     * チャンネルをフォロー/アンフォローする。
     * フォロー済みの場合はアンフォロー、未フォローの場合はフォローを実行する。
     */
    data class ToggleFollow(val channel: ChannelInfo) : TimelineSyncIntent
}

/**
 * Sealed interface defining side effects for one-time events.
 * Used for navigation, snackbars, and other one-time actions.
 */
sealed interface TimelineSyncSideEffect {
    /**
     * Show an error message to the user via snackbar.
     */
    data class ShowError(val message: String) : TimelineSyncSideEffect

    /**
     * 外部アプリを起動する。
     * DeepLink URIを先に試行し、失敗時にフォールバックURLを使用する。
     */
    data class NavigateToExternalApp(
        val deepLinkUri: String,
        val fallbackUrl: String,
    ) : TimelineSyncSideEffect

    // ============================================
    // Story 2: Channel Add/Remove
    // ============================================

    /**
     * Show undo snackbar after channel removal.
     * The snackbar should be displayed for 3 seconds.
     */
    data class ShowUndoSnackbar(val channelName: String) : TimelineSyncSideEffect

    /**
     * Show channel add error message.
     * Auto-dismisses after 2 seconds.
     */
    data class ShowChannelAddError(val message: String) : TimelineSyncSideEffect

    // ============================================
    // Channel Follow (US-2)
    // ============================================

    /**
     * フォロー/アンフォロー後のフィードバックSnackbarを表示する。
     */
    data class ShowFollowFeedback(val message: String) : TimelineSyncSideEffect
}
