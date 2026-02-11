package org.example.project.feature.timeline_sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SelectedStreamInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.model.toDeepLinkInfo
import org.example.project.domain.repository.TimelineSyncRepository
import org.example.project.domain.usecase.ChannelSearchUseCase

/**
 * ViewModel for Timeline Sync screen following MVI architecture pattern.
 * Manages timeline display state and handles user intents for date/week navigation.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display), US-2 (Channel Add/Remove)
 */
@OptIn(ExperimentalTime::class)
class TimelineSyncViewModel(
    private val timelineSyncRepository: TimelineSyncRepository,
    private val channelSearchUseCase: ChannelSearchUseCase,
    private val clock: kotlin.time.Clock = kotlin.time.Clock.System,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineSyncUiState())
    val uiState: StateFlow<TimelineSyncUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<TimelineSyncSideEffect>()
    val sideEffect: SharedFlow<TimelineSyncSideEffect> = _sideEffect.asSharedFlow()

    // Story 2: Channel search debounce job
    private var channelSearchJob: Job? = null

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
            // Story 2: Channel Add/Remove, Story 5: Multi-Platform Search
            is TimelineSyncIntent.SelectPlatform -> selectPlatform(intent.platform)
            TimelineSyncIntent.OpenChannelAddModal -> openChannelAddModal()
            TimelineSyncIntent.CloseChannelAddModal -> closeChannelAddModal()
            is TimelineSyncIntent.UpdateChannelSearchQuery -> updateChannelSearchQuery(intent.query)
            is TimelineSyncIntent.AddChannel -> addChannel(intent.channel)
            is TimelineSyncIntent.RemoveChannel -> removeChannel(intent.channelId)
            TimelineSyncIntent.UndoRemoveChannel -> undoRemoveChannel()
            TimelineSyncIntent.ClearChannelAddError -> clearChannelAddError()
            // Story 4: External App Navigation
            is TimelineSyncIntent.OpenExternalApp -> openExternalApp(intent.channelId)
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
     * Story 3: Sets initial syncTime to the first channel's stream start time.
     */
    private suspend fun loadChannelsData() {
        try {
            // Story 1: Use mock data for demonstration
            // Channel management will be implemented in Story 2
            val mockChannels = getMockChannels()

            // Story 3: 初期syncTime - 最初のチャンネルのストリーム開始時刻
            val initialSyncTime = mockChannels
                .firstOrNull { it.selectedStream?.startTime != null }
                ?.selectedStream?.startTime

            // Story 3: 初期syncTimeに基づいてチャンネルのsyncStatus/targetSeekPositionを計算
            val channelsWithSyncInfo = calculateChannelsSyncInfo(mockChannels, initialSyncTime)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                channels = channelsWithSyncInfo,
                syncTime = initialSyncTime,
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
     * Recalculates all channels' syncStatus and targetSeekPosition.
     */
    private fun updateSyncTime(syncTime: Instant) {
        val currentChannels = _uiState.value.channels
        val updatedChannels = calculateChannelsSyncInfo(currentChannels, syncTime)

        _uiState.value = _uiState.value.copy(
            syncTime = syncTime,
            channels = updatedChannels,
        )
    }

    /**
     * Calculates syncStatus and targetSeekPosition for all channels.
     * Story 3: Sync Time Selection
     *
     * @param channels List of channels to calculate sync info for
     * @param syncTime The global sync time to calculate positions from
     * @return Updated list of channels with calculated sync info
     */
    private fun calculateChannelsSyncInfo(
        channels: List<SyncChannel>,
        syncTime: Instant?,
    ): List<SyncChannel> {
        if (syncTime == null) return channels

        return channels.map { channel ->
            calculateChannelSyncInfo(channel, syncTime)
        }
    }

    /**
     * Calculates syncStatus and targetSeekPosition for a single channel.
     * Story 3: Sync Time Selection
     *
     * @param channel The channel to calculate sync info for
     * @param syncTime The global sync time to calculate position from
     * @return Updated channel with calculated sync info
     */
    private fun calculateChannelSyncInfo(
        channel: SyncChannel,
        syncTime: Instant,
    ): SyncChannel {
        val stream = channel.selectedStream ?: return channel.copy(
            syncStatus = SyncStatus.NOT_SYNCED,
            targetSeekPosition = null,
        )

        val startTime = stream.startTime ?: return channel.copy(
            syncStatus = SyncStatus.NOT_SYNCED,
            targetSeekPosition = null,
        )

        val endTime = stream.endTime ?: clock.now()

        return when {
            syncTime < startTime -> channel.copy(
                syncStatus = SyncStatus.WAITING,
                targetSeekPosition = 0f,
            )
            syncTime in startTime..endTime -> {
                val newSeekPosition = (syncTime - startTime).inWholeSeconds.toFloat()
                // Story 4: OPENED状態のチャンネルはREADY範囲内なら維持
                val newStatus = if (channel.syncStatus == SyncStatus.OPENED) {
                    SyncStatus.OPENED
                } else {
                    SyncStatus.READY
                }
                channel.copy(
                    syncStatus = newStatus,
                    targetSeekPosition = newSeekPosition,
                )
            }
            else -> channel.copy(
                syncStatus = SyncStatus.NOT_SYNCED,
                targetSeekPosition = null,
            )
        }
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

    // ============================================
    // Story 2: Channel Add/Remove
    // ============================================

    /**
     * プラットフォームを選択する。
     * 検索結果をクリアし、検索クエリが空でない場合は再検索を実行する。
     * Story 5: Multi-Platform Search
     */
    private fun selectPlatform(platform: VideoServiceType) {
        _uiState.value = _uiState.value.copy(
            selectedPlatform = platform,
            channelSuggestions = emptyList(),
        )

        // クエリが空でない場合は選択プラットフォームで再検索
        val currentQuery = _uiState.value.channelSearchQuery
        if (currentQuery.isNotBlank()) {
            channelSearchJob?.cancel()
            channelSearchJob = viewModelScope.launch {
                searchChannels(currentQuery)
            }
        }
    }

    /**
     * Opens the channel add modal (bottom sheet).
     */
    private fun openChannelAddModal() {
        _uiState.value = _uiState.value.copy(
            isChannelAddModalVisible = true,
        )
    }

    /**
     * Closes the channel add modal.
     * Resets search query and suggestions.
     */
    private fun closeChannelAddModal() {
        channelSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isChannelAddModalVisible = false,
            channelSearchQuery = "",
            channelSuggestions = emptyList(),
            isSearchingChannels = false,
            channelAddError = null,
        )
    }

    /**
     * Updates the channel search query with 500ms debounce.
     */
    private fun updateChannelSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(channelSearchQuery = query)

        // Cancel previous search job
        channelSearchJob?.cancel()

        // Clear suggestions if query is empty
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                channelSuggestions = emptyList(),
                isSearchingChannels = false,
            )
            return
        }

        // Launch debounced search
        channelSearchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            searchChannels(query)
        }
    }

    /**
     * チャンネル検索を実行する。
     * 選択中のプラットフォームに応じて適切な検索を行う。
     */
    private suspend fun searchChannels(query: String) {
        _uiState.value = _uiState.value.copy(isSearchingChannels = true)

        try {
            channelSearchUseCase.searchChannels(
                query = query,
                serviceType = _uiState.value.selectedPlatform,
                maxResults = 5,
            ).fold(
                onSuccess = { channels ->
                    // Filter out already added channels
                    val existingChannelIds = _uiState.value.channels.map { it.channelId }.toSet()
                    val filteredChannels = channels.filterNot { it.id in existingChannelIds }

                    _uiState.value = _uiState.value.copy(
                        channelSuggestions = filteredChannels,
                        isSearchingChannels = false,
                    )
                },
                onFailure = { error ->
                    handleSearchFailure()
                },
            )
        } catch (e: Exception) {
            handleSearchFailure()
        }
    }

    /**
     * Handles search failure by updating UI state and emitting error side effect.
     */
    private suspend fun handleSearchFailure() {
        _uiState.value = _uiState.value.copy(
            channelSuggestions = emptyList(),
            isSearchingChannels = false,
            channelAddError = "検索に失敗しました",
        )
        _sideEffect.emit(
            TimelineSyncSideEffect.ShowChannelAddError("検索に失敗しました"),
        )
    }

    /**
     * Adds a channel to the timeline.
     */
    private fun addChannel(channelInfo: ChannelInfo) {
        val currentChannels = _uiState.value.channels

        // Check for duplicate
        if (currentChannels.any { it.channelId == channelInfo.id }) {
            showChannelAddError("既に追加済みです")
            return
        }

        // Check max limit
        if (!_uiState.value.canAddChannel) {
            showChannelAddError("最大${TimelineSyncUiState.MAX_CHANNELS}チャンネルまで追加可能です")
            return
        }

        // Convert ChannelInfo to SyncChannel
        val newChannel = channelInfo.toSyncChannel()

        // Remove from suggestions and add to channels
        val updatedSuggestions = _uiState.value.channelSuggestions
            .filterNot { it.id == channelInfo.id }

        _uiState.value = _uiState.value.copy(
            channels = currentChannels + newChannel,
            channelSuggestions = updatedSuggestions,
            channelAddError = null,
        )
    }

    /**
     * Shows channel add error and auto-dismisses after a delay.
     */
    private fun showChannelAddError(message: String) {
        _uiState.value = _uiState.value.copy(channelAddError = message)
        viewModelScope.launch {
            _sideEffect.emit(TimelineSyncSideEffect.ShowChannelAddError(message))
            delay(ERROR_AUTO_DISMISS_MS)
            if (_uiState.value.channelAddError == message) {
                _uiState.value = _uiState.value.copy(channelAddError = null)
            }
        }
    }

    /**
     * Removes a channel from the timeline.
     */
    private fun removeChannel(channelId: String) {
        val currentChannels = _uiState.value.channels
        val channelToRemove = currentChannels.find { it.channelId == channelId } ?: return

        val updatedChannels = currentChannels.filterNot { it.channelId == channelId }

        _uiState.value = _uiState.value.copy(
            channels = updatedChannels,
            recentlyDeletedChannel = channelToRemove,
        )

        viewModelScope.launch {
            _sideEffect.emit(
                TimelineSyncSideEffect.ShowUndoSnackbar(channelToRemove.channelName),
            )

            // Auto-clear recently deleted after undo timeout
            delay(UNDO_TIMEOUT_MS)
            // Only clear if it's still the same channel
            if (_uiState.value.recentlyDeletedChannel?.channelId == channelId) {
                _uiState.value = _uiState.value.copy(recentlyDeletedChannel = null)
            }
        }
    }

    /**
     * Undoes the most recent channel removal.
     */
    private fun undoRemoveChannel() {
        val channelToRestore = _uiState.value.recentlyDeletedChannel ?: return

        _uiState.value = _uiState.value.copy(
            channels = _uiState.value.channels + channelToRestore,
            recentlyDeletedChannel = null,
        )
    }

    /**
     * Clears the channel add error message.
     */
    private fun clearChannelAddError() {
        _uiState.value = _uiState.value.copy(channelAddError = null)
    }

    // ============================================
    // Story 4: External App Navigation
    // ============================================

    /**
     * 外部アプリでチャンネルの動画を開く。
     * DeepLinkInfoを生成してSideEffectを発行し、SyncStatusをOPENEDに更新する。
     */
    private fun openExternalApp(channelId: String) {
        val channel = _uiState.value.channels.find { it.channelId == channelId } ?: return

        val deepLinkInfo = channel.toDeepLinkInfo() ?: return

        // SyncStatusをOPENEDに更新
        val updatedChannels = _uiState.value.channels.map {
            if (it.channelId == channelId) {
                it.copy(syncStatus = SyncStatus.OPENED)
            } else {
                it
            }
        }
        _uiState.value = _uiState.value.copy(channels = updatedChannels)

        viewModelScope.launch {
            _sideEffect.emit(
                TimelineSyncSideEffect.NavigateToExternalApp(
                    deepLinkUri = deepLinkInfo.deepLinkUri,
                    fallbackUrl = deepLinkInfo.fallbackUrl,
                ),
            )
        }
    }

    /**
     * ChannelInfo を SyncChannel に変換する拡張関数。
     * ChannelInfo の serviceType をそのまま使用する。
     */
    private fun ChannelInfo.toSyncChannel(): SyncChannel = SyncChannel(
        channelId = id,
        channelName = displayName,
        channelIconUrl = thumbnailUrl ?: "",
        serviceType = serviceType,
        selectedStream = null,
        syncStatus = SyncStatus.NOT_SYNCED,
    )

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
        // Story 2: Channel Add constants
        private const val SEARCH_DEBOUNCE_MS = 500L
        private const val ERROR_AUTO_DISMISS_MS = 2000L
        private const val UNDO_TIMEOUT_MS = 3000L

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
