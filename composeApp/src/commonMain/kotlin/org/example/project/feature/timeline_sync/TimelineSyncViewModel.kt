package org.example.project.feature.timeline_sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
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
import kotlinx.serialization.json.Json
import org.example.project.core.navigation.PresetChannel
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.FollowedChannel
import org.example.project.domain.model.SelectedStreamInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncHistory
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.TimestampMarker
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.model.toDeepLinkInfo
import org.example.project.domain.repository.ChannelFollowRepository
import org.example.project.domain.repository.CommentRepository
import org.example.project.domain.repository.SyncHistoryRepository
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
    private val channelFollowRepository: ChannelFollowRepository,
    private val commentRepository: CommentRepository,
    private val syncHistoryRepository: SyncHistoryRepository,
    private val clock: kotlin.time.Clock = kotlin.time.Clock.System,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineSyncUiState())
    val uiState: StateFlow<TimelineSyncUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<TimelineSyncSideEffect>()
    val sideEffect: SharedFlow<TimelineSyncSideEffect> = _sideEffect.asSharedFlow()

    // Story 2: Channel search debounce job
    private var channelSearchJob: Job? = null

    init {
        // Channel Follow (US-2): フォロー済みチャンネルをFlowで監視
        observeFollowedChannels()
    }

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
            // Channel Follow (US-2)
            is TimelineSyncIntent.ToggleFollow -> toggleFollow(intent.channel)
            // Story 3: コメントタイムスタンプマーカー (US-3)
            is TimelineSyncIntent.SelectMarker -> selectMarker(intent.channelId, intent.marker)
            TimelineSyncIntent.DismissMarkerPreview -> dismissMarkerPreview()
            is TimelineSyncIntent.RetryLoadComments -> retryLoadComments(intent.channelId)
            // US-4: コメントリスト
            is TimelineSyncIntent.OpenCommentList -> openCommentList(intent.channelId)
            TimelineSyncIntent.CloseCommentList -> closeCommentList()
            is TimelineSyncIntent.ChangeCommentSortOrder -> changeCommentSortOrder(intent.sortOrder)
            TimelineSyncIntent.LoadMoreComments -> loadMoreComments()
            is TimelineSyncIntent.TapCommentTimestamp -> tapCommentTimestamp(intent.channelId, intent.timestampSeconds)
            // 履歴保存 (US-2: 同期チャンネル履歴保存)
            TimelineSyncIntent.SaveHistory -> saveHistory()
            TimelineSyncIntent.ConfirmOverwriteHistory -> confirmOverwriteHistory()
            TimelineSyncIntent.CancelOverwriteHistory -> cancelOverwriteHistory()
            // アーカイブHome プリセット遷移（US-4）
            is TimelineSyncIntent.LoadWithPresets -> loadWithPresets(
                presetChannelsJson = intent.presetChannelsJson,
                presetDate = intent.presetDate,
            )
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

            // US-3: 初期読み込み後、YouTube チャンネルのコメントを自動取得
            loadCommentsForYouTubeChannels(channelsWithSyncInfo)
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
     * Channel Follow (US-2): プラットフォーム切替時にフォローIDリストを再計算
     */
    private fun selectPlatform(platform: VideoServiceType) {
        _uiState.value = _uiState.value.copy(
            selectedPlatform = platform,
            channelSuggestions = emptyList(),
        )

        // Channel Follow (US-2): プラットフォーム切替時にフォローIDリストを再計算
        recalculateFollowedChannelIds()

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

        // US-3: addChannel 時点では selectedStream は null
        // ストリーム選択フロー実装時に loadCommentsForChannel を呼び出すこと
        // TODO: ストリーム選択時にYouTubeチャンネルのコメントを自動取得
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

    // ============================================
    // アーカイブHome プリセット遷移（US-4）
    // ============================================

    /**
     * アーカイブHome画面からプリセット付きでタイムラインを読み込む。
     *
     * JSON文字列を List<PresetChannel> にデコードし、SyncChannel に変換してUiStateを更新する。
     * 重複チャンネルは除去する。チャンネルのプリセットのみで、ストリーム選択はしない。
     */
    private fun loadWithPresets(presetChannelsJson: String, presetDate: LocalDate) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
        )

        viewModelScope.launch {
            try {
                val presetChannels = Json.decodeFromString<List<PresetChannel>>(presetChannelsJson)

                // PresetChannel -> SyncChannel に変換（重複チャンネルを除去）
                val syncChannels = presetChannels
                    .distinctBy { it.channelId }
                    .map { presetChannel ->
                        SyncChannel(
                            channelId = presetChannel.channelId,
                            channelName = presetChannel.channelName,
                            channelIconUrl = presetChannel.channelIconUrl,
                            serviceType = VideoServiceType.valueOf(presetChannel.serviceType),
                            selectedStream = null,
                            syncStatus = SyncStatus.NOT_SYNCED,
                        )
                    }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    channels = syncChannels,
                    selectedDate = presetDate,
                    displayedWeekStart = presetDate.startOfWeek(),
                    syncTime = null,
                    errorMessage = null,
                )
            } catch (e: Exception) {
                // JSONデコード失敗時はデフォルトのLoadScreenにフォールバック
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null,
                )
                loadChannelsData()
            }
        }
    }

    // ============================================
    // Channel Follow (US-2)
    // ============================================

    /**
     * フォロー済みチャンネルの変更をFlowで監視し、UiStateを更新する。
     */
    private fun observeFollowedChannels() {
        viewModelScope.launch {
            channelFollowRepository.observeFollowedChannels().collect { followedChannels ->
                updateFollowedChannelIds(followedChannels)
            }
        }
    }

    /**
     * 選択中のプラットフォームでフィルタしたフォロー済みチャンネルIDのセットを再計算する。
     * プラットフォーム切替時など、Flowからの通知外で再フィルタが必要な場合に使用。
     */
    private fun recalculateFollowedChannelIds() {
        viewModelScope.launch {
            channelFollowRepository.getAllFollowedChannels().fold(
                onSuccess = { updateFollowedChannelIds(it) },
                onFailure = { /* エラーは無視（フォロー状態は表示に必須ではない） */ },
            )
        }
    }

    /**
     * フォロー済みチャンネルリストから選択中プラットフォームのIDセットを抽出し、UiStateを更新する。
     */
    private fun updateFollowedChannelIds(followedChannels: List<FollowedChannel>) {
        val selectedPlatform = _uiState.value.selectedPlatform
        val followedIds = followedChannels
            .filter { it.serviceType == selectedPlatform }
            .map { it.channelId }
            .toSet()
        _uiState.value = _uiState.value.copy(followedChannelIds = followedIds)
    }

    /**
     * チャンネルをフォロー/アンフォローする。
     * フォロー済みの場合はアンフォロー、未フォローの場合はフォローを実行する。
     */
    private fun toggleFollow(channel: ChannelInfo) {
        val isFollowing = _uiState.value.followedChannelIds.contains(channel.id)

        viewModelScope.launch {
            if (isFollowing) {
                // アンフォロー
                channelFollowRepository.unfollow(
                    channelId = channel.id,
                    serviceType = channel.serviceType,
                ).fold(
                    onSuccess = {
                        _sideEffect.emit(
                            TimelineSyncSideEffect.ShowFollowFeedback(
                                "${channel.displayName}のフォローを解除しました",
                            ),
                        )
                    },
                    onFailure = { error ->
                        _sideEffect.emit(
                            TimelineSyncSideEffect.ShowError("フォロー解除に失敗しました"),
                        )
                    },
                )
            } else {
                // フォロー
                channelFollowRepository.follow(
                    channelId = channel.id,
                    channelName = channel.displayName,
                    channelIconUrl = channel.thumbnailUrl ?: "",
                    serviceType = channel.serviceType,
                ).fold(
                    onSuccess = {
                        _sideEffect.emit(
                            TimelineSyncSideEffect.ShowFollowFeedback(
                                "${channel.displayName}をフォローしました",
                            ),
                        )
                    },
                    onFailure = { error ->
                        _sideEffect.emit(
                            TimelineSyncSideEffect.ShowError("フォローに失敗しました"),
                        )
                    },
                )
            }
        }
    }

    // ============================================
    // 履歴保存 (US-2: 同期チャンネル履歴保存)
    // ============================================

    /**
     * 保存ボタンタップ時の処理。
     * 重複チェックを実施し、重複がなければ保存、重複があれば確認ダイアログを表示する。
     */
    private fun saveHistory() {
        val channels = _uiState.value.channels

        // チャンネル数が2未満の場合は保存しない（ボタンが非活性のため通常到達しないが防御的チェック）
        if (channels.size < TimelineSyncUiState.MIN_CHANNELS_FOR_SAVE) return

        _uiState.value = _uiState.value.copy(isSavingHistory = true)

        viewModelScope.launch {
            // 重複チェック: 既存の全履歴を取得し channelId セットを比較する
            syncHistoryRepository.getAllHistories().fold(
                onSuccess = { existingHistories ->
                    val currentChannelIds = channels.map { it.channelId }.toSet()
                    val duplicate = existingHistories.find { history ->
                        val historyChannelIds = history.channels.map { it.channelId }.toSet()
                        historyChannelIds == currentChannelIds
                    }

                    if (duplicate != null) {
                        // 重複あり → 確認ダイアログを表示
                        _uiState.value = _uiState.value.copy(
                            isSavingHistory = false,
                            showDuplicateDialog = true,
                            duplicateHistoryId = duplicate.id,
                        )
                    } else {
                        // 重複なし → 保存実行
                        performSaveHistory(channels = channels, existingHistoryId = null)
                    }
                },
                onFailure = { error ->
                    // 履歴取得に失敗しても保存は継続する
                    performSaveHistory(channels = channels, existingHistoryId = null)
                },
            )
        }
    }

    /**
     * 重複確認ダイアログで「上書き」を選択した時の処理。
     * 重複している既存履歴を削除してから新規保存する。
     */
    private fun confirmOverwriteHistory() {
        val duplicateHistoryId = _uiState.value.duplicateHistoryId ?: return
        val channels = _uiState.value.channels

        _uiState.value = _uiState.value.copy(
            showDuplicateDialog = false,
            duplicateHistoryId = null,
            isSavingHistory = true,
        )

        viewModelScope.launch {
            // 既存履歴を削除してから新規保存
            syncHistoryRepository.deleteHistory(duplicateHistoryId).fold(
                onSuccess = {
                    performSaveHistory(channels = channels, existingHistoryId = null)
                },
                onFailure = { error ->
                    // 削除失敗時もそのまま上書き保存を試みる
                    performSaveHistory(channels = channels, existingHistoryId = null)
                },
            )
        }
    }

    /**
     * 重複確認ダイアログで「キャンセル」を選択した時の処理。
     * ダイアログを閉じて保存をキャンセルする。
     */
    private fun cancelOverwriteHistory() {
        _uiState.value = _uiState.value.copy(
            showDuplicateDialog = false,
            duplicateHistoryId = null,
            isSavingHistory = false,
        )
    }

    /**
     * 実際の保存処理を実行する。
     * 保存成功時は成功フィードバックを発行し、失敗時はエラーフィードバックを発行する。
     *
     * @param channels 保存するチャンネルリスト
     * @param existingHistoryId 上書き対象の履歴ID（nullの場合は新規保存）
     */
    private suspend fun performSaveHistory(
        channels: List<SyncChannel>,
        existingHistoryId: String?,
    ) {
        syncHistoryRepository.saveHistory(channels = channels, name = null).fold(
            onSuccess = { savedHistory ->
                _uiState.value = _uiState.value.copy(isSavingHistory = false)
                _sideEffect.emit(
                    TimelineSyncSideEffect.ShowSaveHistorySuccess("履歴を保存しました"),
                )
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(isSavingHistory = false)
                _sideEffect.emit(
                    TimelineSyncSideEffect.ShowSaveHistoryError("保存に失敗しました"),
                )
            },
        )
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

    // ============================================
    // Story 3: コメントタイムスタンプマーカー (US-3)
    // ============================================

    /**
     * YouTube チャンネルの selectedStream が設定されているチャンネルを対象に
     * コメントを一括取得する。
     *
     * @param channels コメントを取得するチャンネルリスト
     */
    private fun loadCommentsForYouTubeChannels(channels: List<SyncChannel>) {
        channels
            .filter { it.serviceType == VideoServiceType.YOUTUBE && it.selectedStream != null }
            .forEach { channel ->
                val videoId = channel.selectedStream?.id ?: return@forEach
                loadCommentsForChannel(channel.channelId, videoId)
            }
    }

    /**
     * 特定チャンネルのコメントを取得する。
     * LOADING 状態に遷移後、結果に応じて LOADED / ERROR / DISABLED に更新する。
     *
     * @param channelId 対象チャンネルID
     * @param videoId 対象動画ID
     */
    private fun loadCommentsForChannel(channelId: String, videoId: String) {
        // LOADING 状態に更新
        updateChannelCommentState(
            channelId,
            ChannelCommentState(
                videoId = videoId,
                status = CommentLoadStatus.LOADING,
            ),
        )

        viewModelScope.launch {
            commentRepository.getVideoComments(videoId = videoId).fold(
                onSuccess = { result ->
                    if (result.commentsDisabled) {
                        // コメント無効化（YouTube 403 commentsDisabled）
                        updateChannelCommentState(
                            channelId,
                            ChannelCommentState(
                                videoId = videoId,
                                status = CommentLoadStatus.DISABLED,
                                errorMessage = "この動画ではコメントが無効です",
                            ),
                        )
                    } else {
                        // 正常取得（タイムスタンプなしも LOADED 扱い）
                        updateChannelCommentState(
                            channelId,
                            ChannelCommentState(
                                videoId = videoId,
                                status = CommentLoadStatus.LOADED,
                                markers = result.timestampMarkers,
                                nextPageToken = result.nextPageToken,
                            ),
                        )
                    }
                },
                onFailure = { error ->
                    updateChannelCommentState(
                        channelId,
                        ChannelCommentState(
                            videoId = videoId,
                            status = CommentLoadStatus.ERROR,
                            errorMessage = "コメントの読み込みに失敗しました",
                        ),
                    )
                },
            )
        }
    }

    /**
     * チャンネルのコメント状態を更新するヘルパー関数。
     *
     * @param channelId 更新対象のチャンネルID
     * @param state 新しいコメント状態
     */
    private fun updateChannelCommentState(channelId: String, state: ChannelCommentState) {
        _uiState.value = _uiState.value.copy(
            channelComments = _uiState.value.channelComments + (channelId to state),
        )
    }

    /**
     * タイムラインバー上のマーカーをタップして、コメントプレビューを表示する。
     * 複数マーカーが集約されている場合は最もいいね数の多いコメントを選択する。
     *
     * @param channelId マーカーが属するチャンネルID
     * @param marker タップされたマーカー
     */
    private fun selectMarker(channelId: String, marker: TimestampMarker) {
        _uiState.value = _uiState.value.copy(
            selectedMarkerPreview = TimestampMarkerPreview(
                channelId = channelId,
                marker = marker,
            ),
        )
    }

    /**
     * マーカープレビューを閉じる。
     */
    private fun dismissMarkerPreview() {
        _uiState.value = _uiState.value.copy(selectedMarkerPreview = null)
    }

    /**
     * 指定チャンネルのコメント読み込みを再試行する。
     * ERROR 状態のチャンネルのみ有効。
     *
     * @param channelId 再試行対象のチャンネルID
     */
    private fun retryLoadComments(channelId: String) {
        val currentState = _uiState.value.channelComments[channelId] ?: return
        if (currentState.status != CommentLoadStatus.ERROR) return

        loadCommentsForChannel(channelId, currentState.videoId)
    }

    // ============================================
    // US-4: コメントリスト
    // ============================================

    /**
     * コメントリスト BottomSheet を開く。
     * LOADED 状態かつタイムスタンプ付きコメントが存在する場合のみ有効。
     *
     * @param channelId コメントリストを表示するチャンネルID
     */
    private fun openCommentList(channelId: String) {
        _uiState.value = _uiState.value.copy(
            isCommentListVisible = true,
            commentListChannelId = channelId,
        )
    }

    /**
     * コメントリスト BottomSheet を閉じる。
     */
    private fun closeCommentList() {
        _uiState.value = _uiState.value.copy(
            isCommentListVisible = false,
            commentListChannelId = null,
        )
    }

    /**
     * コメントリストのソート順を変更する。
     * クライアントサイドでソートを適用する（APIからの再取得はしない）。
     *
     * @param sortOrder 新しいソート順
     */
    private fun changeCommentSortOrder(sortOrder: CommentSortOrder) {
        _uiState.value = _uiState.value.copy(commentSortOrder = sortOrder)
    }

    /**
     * コメントリストの次ページを読み込む（ページネーション）。
     * 現在表示中のチャンネルの nextPageToken が存在する場合のみ実行。
     * 読み込んだ markers を既存リストに追記する。
     */
    private fun loadMoreComments() {
        val channelId = _uiState.value.commentListChannelId ?: return
        val currentState = _uiState.value.channelComments[channelId] ?: return

        // 追加読み込み中または nextPageToken がない場合は何もしない
        if (_uiState.value.isLoadingMoreComments) return
        val nextPageToken = currentState.nextPageToken ?: return

        _uiState.value = _uiState.value.copy(isLoadingMoreComments = true)

        viewModelScope.launch {
            val order = when (_uiState.value.commentSortOrder) {
                CommentSortOrder.LIKES -> "relevance"
                CommentSortOrder.TIME -> "time"
            }
            commentRepository.getVideoComments(
                videoId = currentState.videoId,
                pageToken = nextPageToken,
                order = order,
            ).fold(
                onSuccess = { result ->
                    val updatedMarkers = currentState.markers + result.timestampMarkers
                    updateChannelCommentState(
                        channelId,
                        currentState.copy(
                            markers = updatedMarkers,
                            nextPageToken = result.nextPageToken,
                        ),
                    )
                    _uiState.value = _uiState.value.copy(isLoadingMoreComments = false)
                },
                onFailure = { _ ->
                    _uiState.value = _uiState.value.copy(isLoadingMoreComments = false)
                    _sideEffect.emit(TimelineSyncSideEffect.ShowError("追加コメントの読み込みに失敗しました"))
                },
            )
        }
    }

    /**
     * コメントリスト内のタイムスタンプをタップして同期時刻を更新する。
     *
     * absoluteTime = channel.selectedStream.startTime + timestampSeconds
     * 既存の updateSyncTime / calculateChannelsSyncInfo を再利用する。
     *
     * @param channelId 対象チャンネルID
     * @param timestampSeconds タップしたタイムスタンプの秒数（動画先頭からの経過秒）
     */
    private fun tapCommentTimestamp(channelId: String, timestampSeconds: Long) {
        val channel = _uiState.value.channels.find { it.channelId == channelId } ?: return
        val startTime = channel.selectedStream?.startTime ?: return

        // 絶対時刻 = 動画開始時刻 + タイムスタンプ秒数
        val absoluteTime = startTime + timestampSeconds.seconds
        updateSyncTime(absoluteTime)
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
