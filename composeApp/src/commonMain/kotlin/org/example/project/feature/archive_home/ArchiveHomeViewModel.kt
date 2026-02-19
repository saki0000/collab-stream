@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.archive_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.FollowedChannel
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.ChannelFollowRepository
import org.example.project.domain.repository.TimelineSyncRepository
import org.example.project.domain.usecase.ChannelSearchUseCase
import org.example.project.feature.timeline_sync.startOfWeek

/**
 * アーカイブHome画面のViewModel。
 *
 * MVI アーキテクチャパターンに従う。
 *
 * Epic: Channel Follow & Archive Home (US-3)
 * Story: US-3 (Archive Home Display)
 */
class ArchiveHomeViewModel(
    private val timelineSyncRepository: TimelineSyncRepository,
    private val channelFollowRepository: ChannelFollowRepository,
    private val channelSearchUseCase: ChannelSearchUseCase,
    private val clock: kotlin.time.Clock = kotlin.time.Clock.System,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ArchiveHomeUiState(
            selectedDate = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            displayedWeekStart = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.startOfWeek(),
        ),
    )
    val uiState: StateFlow<ArchiveHomeUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<ArchiveHomeSideEffect>()
    val sideEffect: SharedFlow<ArchiveHomeSideEffect> = _sideEffect.asSharedFlow()

    // チャンネル検索のデバウンスJob
    private var channelSearchJob: Job? = null

    init {
        // フォロー済みチャンネルをFlowで監視
        observeFollowedChannels()
    }

    /**
     * ユーザーIntentを処理する。
     */
    fun handleIntent(intent: ArchiveHomeIntent) {
        when (intent) {
            ArchiveHomeIntent.LoadScreen -> loadScreen()
            is ArchiveHomeIntent.SelectDate -> selectDate(intent.date)
            ArchiveHomeIntent.NavigateToPreviousWeek -> navigateToPreviousWeek()
            ArchiveHomeIntent.NavigateToNextWeek -> navigateToNextWeek()
            ArchiveHomeIntent.ClearError -> clearError()
            ArchiveHomeIntent.Retry -> retry()
            ArchiveHomeIntent.OpenChannelAddModal -> openChannelAddModal()
            ArchiveHomeIntent.CloseChannelAddModal -> closeChannelAddModal()
            is ArchiveHomeIntent.SelectPlatform -> selectPlatform(intent.platform)
            is ArchiveHomeIntent.UpdateChannelSearchQuery -> updateChannelSearchQuery(intent.query)
            is ArchiveHomeIntent.ToggleFollow -> toggleFollow(intent.channel)
        }
    }

    /**
     * 画面データを読み込む。
     */
    private fun loadScreen() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
        )

        viewModelScope.launch {
            loadArchivesForSelectedDate()
        }
    }

    /**
     * 日付を選択する。
     */
    private fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
        )

        viewModelScope.launch {
            loadArchivesForSelectedDate()
        }
    }

    /**
     * 前週に移動する。
     */
    private fun navigateToPreviousWeek() {
        val currentWeekStart = _uiState.value.displayedWeekStart
        val newWeekStart = currentWeekStart.plus(-7, DateTimeUnit.DAY)

        _uiState.value = _uiState.value.copy(
            displayedWeekStart = newWeekStart,
        )
    }

    /**
     * 次週に移動する。
     */
    private fun navigateToNextWeek() {
        val currentWeekStart = _uiState.value.displayedWeekStart
        val newWeekStart = currentWeekStart.plus(7, DateTimeUnit.DAY)

        _uiState.value = _uiState.value.copy(
            displayedWeekStart = newWeekStart,
        )
    }

    /**
     * エラーをクリアする。
     */
    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * データ再読み込み。
     */
    private fun retry() {
        loadScreen()
    }

    // ============================================
    // アーカイブ取得
    // ============================================

    /**
     * フォロー済みチャンネルの変更をFlowで監視し、アーカイブを自動再取得する。
     */
    private fun observeFollowedChannels() {
        viewModelScope.launch {
            channelFollowRepository.observeFollowedChannels().collect { followedChannels ->
                _uiState.value = _uiState.value.copy(followedChannels = followedChannels)
                // フォローリスト変更時にアーカイブを再取得
                loadArchivesForSelectedDate()
            }
        }
    }

    /**
     * 選択日のアーカイブを取得する。
     */
    private suspend fun loadArchivesForSelectedDate() {
        val selectedDate = _uiState.value.selectedDate
        val followedChannels = _uiState.value.followedChannels

        if (followedChannels.isEmpty()) {
            // フォロー0件の場合は空リストを設定
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                archives = emptyList(),
                errorMessage = null,
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        try {
            // フォロー中チャンネルごとに並列でアーカイブを取得
            val archiveResults = followedChannels.map { channel ->
                viewModelScope.async {
                    timelineSyncRepository.getChannelVideos(
                        channelId = channel.channelId,
                        serviceType = channel.serviceType,
                        dateRange = selectedDate..selectedDate,
                    )
                }
            }.awaitAll()

            // 全結果が失敗の場合はエラー表示
            val allFailed = archiveResults.all { it.isFailure }
            if (allFailed) {
                val firstError = archiveResults.firstNotNullOfOrNull { it.exceptionOrNull() }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "データの読み込みに失敗しました: ${firstError?.message}",
                )
                _sideEffect.emit(
                    ArchiveHomeSideEffect.ShowError("データの読み込みに失敗しました"),
                )
                return
            }

            // 成功したアーカイブのみを抽出してフラット化
            val archives = archiveResults
                .mapNotNull { result ->
                    result.getOrNull()
                }
                .flatten()
                .map { videoDetails ->
                    val followedChannel = followedChannels.find { ch ->
                        ch.channelId == videoDetails.snippet.channelId &&
                            ch.serviceType == videoDetails.serviceType
                    }
                    videoDetails.toArchiveItem(followedChannel)
                }
                .sortedByDescending { it.publishedAt }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                archives = archives,
                errorMessage = null,
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "データの読み込みに失敗しました: ${e.message}",
            )
            _sideEffect.emit(
                ArchiveHomeSideEffect.ShowError("データの読み込みに失敗しました"),
            )
        }
    }

    /**
     * VideoDetails を ArchiveItem に変換する。
     */
    private fun VideoDetails.toArchiveItem(followedChannel: FollowedChannel?): ArchiveItem =
        ArchiveItem(
            videoId = id,
            title = snippet.title,
            thumbnailUrl = "", // VideoSnippetにはthumbnailUrlがないため空文字列
            channelId = snippet.channelId,
            channelName = followedChannel?.channelName ?: snippet.channelTitle,
            channelIconUrl = followedChannel?.channelIconUrl ?: "",
            serviceType = serviceType,
            publishedAt = getStartTimeForSync(),
            durationSeconds = getDurationInSeconds(),
        )

    // ============================================
    // チャンネル検索モーダル
    // ============================================

    /**
     * チャンネル追加モーダルを開く。
     */
    private fun openChannelAddModal() {
        _uiState.value = _uiState.value.copy(
            isChannelAddModalVisible = true,
        )
        // プラットフォーム切替時にフォローIDリストを再計算
        recalculateFollowedChannelIds()
    }

    /**
     * チャンネル追加モーダルを閉じる。
     */
    private fun closeChannelAddModal() {
        channelSearchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isChannelAddModalVisible = false,
            channelSearchQuery = "",
            channelSuggestions = emptyList(),
            isSearchingChannels = false,
        )
    }

    /**
     * プラットフォームを選択する。
     */
    private fun selectPlatform(platform: VideoServiceType) {
        _uiState.value = _uiState.value.copy(
            selectedPlatform = platform,
            channelSuggestions = emptyList(),
        )

        // プラットフォーム切替時にフォローIDリストを再計算
        recalculateFollowedChannelIds()

        // クエリが空でない場合は再検索
        val currentQuery = _uiState.value.channelSearchQuery
        if (currentQuery.isNotBlank()) {
            channelSearchJob?.cancel()
            channelSearchJob = viewModelScope.launch {
                searchChannels(currentQuery)
            }
        }
    }

    /**
     * チャンネル検索クエリを更新する（500msデバウンス）。
     */
    private fun updateChannelSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(channelSearchQuery = query)

        // 前回の検索Jobをキャンセル
        channelSearchJob?.cancel()

        // クエリが空の場合は候補をクリア
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                channelSuggestions = emptyList(),
                isSearchingChannels = false,
            )
            return
        }

        // デバウンス検索を開始
        channelSearchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            searchChannels(query)
        }
    }

    /**
     * チャンネル検索を実行する。
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
                    _uiState.value = _uiState.value.copy(
                        channelSuggestions = channels,
                        isSearchingChannels = false,
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        channelSuggestions = emptyList(),
                        isSearchingChannels = false,
                    )
                },
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                channelSuggestions = emptyList(),
                isSearchingChannels = false,
            )
        }
    }

    /**
     * 選択中のプラットフォームでフィルタしたフォロー済みチャンネルIDのセットを再計算する。
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
                            ArchiveHomeSideEffect.ShowFollowFeedback(
                                "${channel.displayName}のフォローを解除しました",
                            ),
                        )
                    },
                    onFailure = {
                        _sideEffect.emit(
                            ArchiveHomeSideEffect.ShowError("フォロー解除に失敗しました"),
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
                            ArchiveHomeSideEffect.ShowFollowFeedback(
                                "${channel.displayName}をフォローしました",
                            ),
                        )
                    },
                    onFailure = {
                        _sideEffect.emit(
                            ArchiveHomeSideEffect.ShowError("フォローに失敗しました"),
                        )
                    },
                )
            }
        }
    }

    companion object {
        // チャンネル検索デバウンス時間
        private const val SEARCH_DEBOUNCE_MS = 500L
    }
}
