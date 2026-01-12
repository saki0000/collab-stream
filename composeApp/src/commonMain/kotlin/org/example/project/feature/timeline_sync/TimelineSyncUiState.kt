@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.example.project.domain.model.SyncChannel

/**
 * Data class representing the UI state for Timeline Sync screen.
 *
 * Contains all necessary state information for timeline display and
 * week/date navigation.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
 */
data class TimelineSyncUiState(
    /**
     * Whether data is currently being loaded.
     */
    val isLoading: Boolean = false,

    /**
     * List of channels with their stream information.
     */
    val channels: List<SyncChannel> = emptyList(),

    /**
     * Currently selected date for timeline display.
     * Defaults to today.
     */
    val selectedDate: LocalDate = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault()),

    /**
     * Start date of the currently displayed week.
     * Used for calendar week navigation.
     */
    val displayedWeekStart: LocalDate = selectedDate.startOfWeek(),

    /**
     * Global sync time for timeline synchronization.
     * Null when no sync time is set (initial state).
     * Story 1: Display only, selection is Story 3.
     */
    val syncTime: Instant? = null,

    /**
     * Error message to display, if any.
     */
    val errorMessage: String? = null,
) {
    /**
     * Whether the channel list is empty (and not loading).
     */
    val isEmpty: Boolean
        get() = channels.isEmpty() && !isLoading

    /**
     * Number of active channels (channels with selected streams).
     */
    val activeChannelCount: Int
        get() = channels.count { it.selectedStream != null }

    /**
     * List of dates in the currently displayed week.
     */
    val weekDays: List<LocalDate>
        get() = (0..6).map { displayedWeekStart.plus(it, DateTimeUnit.DAY) }
}

/**
 * Extension function to get the start of the week (Monday) for a given date.
 * Uses ISO week definition where Monday is the first day of the week.
 */
fun LocalDate.startOfWeek(): LocalDate {
    val daysFromMonday = this.dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber
    return this.plus(-daysFromMonday, DateTimeUnit.DAY)
}

/**
 * Data class representing timeline bar display information for a channel.
 *
 * Contains calculated positions for rendering timeline bars based on
 * the selected date's 0:00-24:00 time axis.
 */
data class TimelineBarInfo(
    /**
     * Channel ID this bar belongs to.
     */
    val channelId: String,

    /**
     * Start position as fraction (0.0 - 1.0) of the day.
     * 0.0 = 00:00, 1.0 = 24:00
     */
    val startFraction: Float,

    /**
     * End position as fraction (0.0 - 1.0) of the day.
     */
    val endFraction: Float,

    /**
     * Display start time in HH:MM format.
     */
    val displayStartTime: String,

    /**
     * Display end time in HH:MM format.
     */
    val displayEndTime: String,

    /**
     * Whether this is a live stream (endTime is null).
     */
    val isLive: Boolean = false,

    /**
     * Whether this stream is upcoming (hasn't started yet).
     */
    val isUpcoming: Boolean = false,

    /**
     * Minutes until stream starts (for upcoming streams).
     */
    val minutesToStart: Long? = null,
)
