package org.example.project.feature.timeline_sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.example.project.domain.model.SelectedStreamInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.TimelineSyncRepository

/**
 * ViewModel for Timeline Sync screen following MVI architecture pattern.
 * Manages timeline display state and handles user intents for date/week navigation.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
 */
@OptIn(ExperimentalTime::class)
class TimelineSyncViewModel(
    private val timelineSyncRepository: TimelineSyncRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineSyncUiState())
    val uiState: StateFlow<TimelineSyncUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<TimelineSyncSideEffect>()
    val sideEffect: SharedFlow<TimelineSyncSideEffect> = _sideEffect.asSharedFlow()

    /**
     * Handles user intents and updates state accordingly.
     */
    fun handleIntent(intent: TimelineSyncIntent) {
        when (intent) {
            TimelineSyncIntent.LoadScreen -> loadScreen()
            is TimelineSyncIntent.SelectDate -> selectDate(intent.date)
            TimelineSyncIntent.NavigateToPreviousWeek -> navigateToPreviousWeek()
            TimelineSyncIntent.NavigateToNextWeek -> navigateToNextWeek()
            TimelineSyncIntent.ClearError -> clearError()
            TimelineSyncIntent.Retry -> retry()
            // Story 3: Sync Time Selection
            is TimelineSyncIntent.UpdateSyncTime -> updateSyncTime(intent.syncTime)
            TimelineSyncIntent.StartDragging -> startDragging()
            TimelineSyncIntent.StopDragging -> stopDragging()
        }
    }

    /**
     * Loads initial screen data.
     * Fetches channels and their streams for the selected date.
     */
    private fun loadScreen() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
        )

        viewModelScope.launch {
            loadChannelsData()
        }
    }

    /**
     * Loads channel data from repository.
     * For Story 1, we use mock data as channel management is Story 2.
     */
    private suspend fun loadChannelsData() {
        try {
            // Story 1: Use mock data for demonstration
            // Channel management will be implemented in Story 2
            val mockChannels = getMockChannels()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                channels = mockChannels,
                errorMessage = null,
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "データの読み込みに失敗しました: ${e.message}",
            )

            _sideEffect.emit(
                TimelineSyncSideEffect.ShowError("データの読み込みに失敗しました"),
            )
        }
    }

    /**
     * Selects a specific date in the calendar.
     * Updates the timeline to show streams for the selected date.
     */
    private fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
        )

        // Filter/recalculate streams for the selected date
        viewModelScope.launch {
            recalculateTimelineBars()
        }
    }

    /**
     * Navigates to the previous week in the calendar.
     * Does not change the selected date.
     */
    private fun navigateToPreviousWeek() {
        val currentWeekStart = _uiState.value.displayedWeekStart
        val newWeekStart = currentWeekStart.plus(-7, DateTimeUnit.DAY)

        _uiState.value = _uiState.value.copy(
            displayedWeekStart = newWeekStart,
        )
    }

    /**
     * Navigates to the next week in the calendar.
     * Does not change the selected date.
     */
    private fun navigateToNextWeek() {
        val currentWeekStart = _uiState.value.displayedWeekStart
        val newWeekStart = currentWeekStart.plus(7, DateTimeUnit.DAY)

        _uiState.value = _uiState.value.copy(
            displayedWeekStart = newWeekStart,
        )
    }

    /**
     * Clears any error message.
     */
    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Retries loading data after an error.
     */
    private fun retry() {
        loadScreen()
    }

    // ============================================
    // Story 3: Sync Time Selection
    // ============================================

    /**
     * Updates the sync time while dragging the sync line.
     * Called continuously during drag operation.
     */
    private fun updateSyncTime(syncTime: Instant) {
        _uiState.value = _uiState.value.copy(syncTime = syncTime)
    }

    /**
     * Starts dragging the sync line.
     * Sets isDragging flag to true.
     */
    private fun startDragging() {
        _uiState.value = _uiState.value.copy(isDragging = true)
    }

    /**
     * Stops dragging the sync line.
     * Sets isDragging flag to false.
     */
    private fun stopDragging() {
        _uiState.value = _uiState.value.copy(isDragging = false)
    }

    /**
     * Recalculates timeline bar positions for the selected date.
     */
    private suspend fun recalculateTimelineBars() {
        // Timeline bar positions are calculated in UI layer based on
        // channel streams and selected date.
        // This method can be used for additional server-side filtering if needed.
    }

    /**
     * Generates mock channel data for Story 1 demonstration.
     * Will be replaced with actual data from repository in Story 2.
     */
    private fun getMockChannels(): List<SyncChannel> {
        val now = kotlin.time.Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val timeZone = TimeZone.currentSystemDefault()

        // Mock YouTube channel with a stream
        val youtubeStreamStart = today.atStartOfDayIn(timeZone) + 10.hours
        val youtubeStreamEnd = today.atStartOfDayIn(timeZone) + 13.hours

        val youtubeChannel = SyncChannel(
            channelId = "UC_mock_youtube_channel",
            channelName = "Gaming Channel",
            channelIconUrl = "https://example.com/avatar1.jpg",
            serviceType = VideoServiceType.YOUTUBE,
            selectedStream = SelectedStreamInfo(
                id = "yt_video_001",
                title = "Morning Gaming Stream",
                thumbnailUrl = "https://example.com/thumb1.jpg",
                startTime = youtubeStreamStart,
                endTime = youtubeStreamEnd,
                duration = 3.hours,
            ),
            syncStatus = SyncStatus.READY,
        )

        // Mock Twitch channel with a live stream
        val twitchStreamStart = today.atStartOfDayIn(timeZone) + 14.hours

        val twitchChannel = SyncChannel(
            channelId = "twitch_mock_channel",
            channelName = "Esports Pro",
            channelIconUrl = "https://example.com/avatar2.jpg",
            serviceType = VideoServiceType.TWITCH,
            selectedStream = SelectedStreamInfo(
                id = "tw_video_001",
                title = "Afternoon Tournament",
                thumbnailUrl = "https://example.com/thumb2.jpg",
                startTime = twitchStreamStart,
                endTime = null, // Live stream
                duration = null,
            ),
            syncStatus = if (now < twitchStreamStart) SyncStatus.WAITING else SyncStatus.READY,
        )

        // Mock channel without stream (for empty state testing)
        val emptyChannel = SyncChannel(
            channelId = "UC_empty_channel",
            channelName = "Inactive Channel",
            channelIconUrl = "https://example.com/avatar3.jpg",
            serviceType = VideoServiceType.YOUTUBE,
            selectedStream = null,
            syncStatus = SyncStatus.NOT_SYNCED,
        )

        return listOf(youtubeChannel, twitchChannel, emptyChannel)
    }

    companion object {
        /**
         * Calculates timeline bar info for a channel's stream on the selected date.
         * Used by UI layer for rendering timeline bars.
         *
         * @param channel The channel with stream information
         * @param selectedDate The date to calculate bar position for
         * @param currentTime Current time for live stream calculations
         * @return TimelineBarInfo if stream is active on selected date, null otherwise
         */
        fun calculateTimelineBarInfo(
            channel: SyncChannel,
            selectedDate: LocalDate,
            currentTime: Instant = kotlin.time.Clock.System.now(),
        ): TimelineBarInfo? {
            val stream = channel.selectedStream ?: return null
            val startTime = stream.startTime ?: return null

            val timeZone = TimeZone.currentSystemDefault()
            val dayStart = selectedDate.atStartOfDayIn(timeZone)
            val dayEnd = dayStart + 1.days

            // Check if stream overlaps with selected date
            val streamEnd = stream.endTime ?: currentTime
            if (startTime >= dayEnd || streamEnd <= dayStart) {
                return null
            }

            // Calculate clipped start/end for the selected date
            val clippedStart = maxOf(startTime, dayStart)
            val clippedEnd = minOf(streamEnd, dayEnd)

            // Calculate fractions (0.0 - 1.0 representing position in the day)
            val dayDuration = (dayEnd - dayStart).inWholeMinutes.toFloat()
            val startFraction = ((clippedStart - dayStart).inWholeMinutes.toFloat() / dayDuration).coerceIn(0f, 1f)
            val endFraction = ((clippedEnd - dayStart).inWholeMinutes.toFloat() / dayDuration).coerceIn(0f, 1f)

            // Format display times
            val startLocal = clippedStart.toLocalDateTime(timeZone)
            val endLocal = clippedEnd.toLocalDateTime(timeZone)
            val displayStartTime = "${startLocal.hour.toString().padStart(
                2,
                '0',
            )}:${startLocal.minute.toString().padStart(2, '0')}"
            val displayEndTime = "${endLocal.hour.toString().padStart(2, '0')}:${endLocal.minute.toString().padStart(2, '0')}"

            // Check if upcoming
            val isUpcoming = startTime > currentTime
            val minutesToStart = if (isUpcoming) {
                (startTime - currentTime).inWholeMinutes
            } else {
                null
            }

            return TimelineBarInfo(
                channelId = channel.channelId,
                startFraction = startFraction,
                endFraction = endFraction,
                displayStartTime = displayStartTime,
                displayEndTime = displayEndTime,
                isLive = stream.endTime == null && !isUpcoming,
                isUpcoming = isUpcoming,
                minutesToStart = minutesToStart,
            )
        }
    }
}
