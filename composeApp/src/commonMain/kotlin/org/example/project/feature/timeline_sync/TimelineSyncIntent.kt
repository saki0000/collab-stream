@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/**
 * Sealed interface defining all possible user intents for Timeline Sync screen.
 * Following MVI architecture pattern for state management.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
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
     * Navigate to external app (Story 4 - placeholder).
     */
    data class NavigateToExternalApp(
        val channelId: String,
        val seekPosition: Float,
    ) : TimelineSyncSideEffect
}
