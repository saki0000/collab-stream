@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.feature.timeline_sync.sync_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.core.navigation.PresetChannel
import org.example.project.domain.model.SavedChannelInfo
import org.example.project.domain.model.displayName
import org.example.project.domain.repository.HistorySortOrder
import org.example.project.domain.repository.SyncHistoryRepository

/**
 * 同期履歴一覧画面のViewModel。
 *
 * MVI アーキテクチャパターンに従い、[SyncHistoryListIntent] を受け取り、
 * [SyncHistoryListUiState] を更新する。
 * [SyncHistoryRepository.observeHistories] でリアルタイム監視を行い、
 * 追加・削除・更新時にUIが自動更新される。
 *
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 * Story: US-3 (履歴一覧表示)
 */
@OptIn(ExperimentalTime::class)
class SyncHistoryListViewModel(
    private val syncHistoryRepository: SyncHistoryRepository,
    private val clock: kotlin.time.Clock = kotlin.time.Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncHistoryListUiState())
    val uiState: StateFlow<SyncHistoryListUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<SyncHistoryListSideEffect>()
    val sideEffect: SharedFlow<SyncHistoryListSideEffect> = _sideEffect.asSharedFlow()

    /** observeHistoriesの現在のJobを保持する。ソート変更時に再購読するために使用。 */
    private var observeJob: Job? = null

    /**
     * ユーザーIntentを処理する。
     */
    fun handleIntent(intent: SyncHistoryListIntent) {
        when (intent) {
            SyncHistoryListIntent.LoadScreen -> loadScreen()
            SyncHistoryListIntent.OpenSortMenu -> openSortMenu()
            SyncHistoryListIntent.CloseSortMenu -> closeSortMenu()
            is SyncHistoryListIntent.ChangeSortOrder -> changeSortOrder(intent.sortOrder)
            is SyncHistoryListIntent.ShowDeleteDialog -> showDeleteDialog(intent.historyId)
            SyncHistoryListIntent.DismissDeleteDialog -> dismissDeleteDialog()
            SyncHistoryListIntent.ConfirmDelete -> confirmDelete()
            is SyncHistoryListIntent.ShowRenameDialog -> showRenameDialog(
                historyId = intent.historyId,
                currentName = intent.currentName,
            )
            SyncHistoryListIntent.DismissRenameDialog -> dismissRenameDialog()
            is SyncHistoryListIntent.UpdateRenameInput -> updateRenameInput(intent.input)
            SyncHistoryListIntent.ConfirmRename -> confirmRename()
            is SyncHistoryListIntent.RestoreHistory -> restoreHistory(intent.historyId)
        }
    }

    /**
     * 画面データを読み込む。
     * 現在のソート順で observeHistories を開始する。
     */
    private fun loadScreen() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        startObservingHistories(_uiState.value.sortOrder)
    }

    // ============================================
    // ソート機能
    // ============================================

    /**
     * ソートドロップダウンメニューを開く。
     */
    private fun openSortMenu() {
        _uiState.value = _uiState.value.copy(isSortMenuVisible = true)
    }

    /**
     * ソートドロップダウンメニューを閉じる。
     */
    private fun closeSortMenu() {
        _uiState.value = _uiState.value.copy(isSortMenuVisible = false)
    }

    /**
     * ソート順を変更し、リストを即座に再取得する。
     */
    private fun changeSortOrder(sortOrder: HistorySortOrder) {
        _uiState.value = _uiState.value.copy(
            sortOrder = sortOrder,
            isSortMenuVisible = false,
            isLoading = true,
        )
        startObservingHistories(sortOrder)
    }

    /**
     * 指定ソート順でobserveHistoriesを開始する。
     * 既存のJobをキャンセルして新しいFlowを購読する。
     */
    private fun startObservingHistories(sortOrder: HistorySortOrder) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            syncHistoryRepository.observeHistories(sortBy = sortOrder).collect { histories ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    histories = histories,
                    errorMessage = null,
                )
            }
        }
    }

    // ============================================
    // 削除機能
    // ============================================

    /**
     * 削除確認ダイアログを表示する。
     */
    private fun showDeleteDialog(historyId: String) {
        _uiState.value = _uiState.value.copy(deletingHistoryId = historyId)
    }

    /**
     * 削除確認ダイアログを閉じる（キャンセル）。
     */
    private fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(deletingHistoryId = null)
    }

    /**
     * 削除を確定する。
     * [SyncHistoryListUiState.deletingHistoryId] の履歴を削除し、結果に応じてSnackbarを表示する。
     */
    private fun confirmDelete() {
        val historyId = _uiState.value.deletingHistoryId ?: return
        // ダイアログを先に閉じる
        _uiState.value = _uiState.value.copy(deletingHistoryId = null)

        viewModelScope.launch {
            syncHistoryRepository.deleteHistory(historyId).fold(
                onSuccess = {
                    _sideEffect.emit(SyncHistoryListSideEffect.ShowDeleteSuccess)
                },
                onFailure = {
                    _sideEffect.emit(SyncHistoryListSideEffect.ShowDeleteError)
                },
            )
        }
    }

    // ============================================
    // 名前変更機能
    // ============================================

    /**
     * 名前変更ダイアログを表示する。
     * テキストフィールドに現在の名前をプリセットする。
     */
    private fun showRenameDialog(historyId: String, currentName: String) {
        _uiState.value = _uiState.value.copy(
            renamingHistoryId = historyId,
            renameInput = currentName,
        )
    }

    /**
     * 名前変更ダイアログを閉じる（キャンセル）。
     */
    private fun dismissRenameDialog() {
        _uiState.value = _uiState.value.copy(
            renamingHistoryId = null,
            renameInput = "",
        )
    }

    /**
     * 名前変更のテキスト入力を更新する。
     */
    private fun updateRenameInput(input: String) {
        _uiState.value = _uiState.value.copy(renameInput = input)
    }

    /**
     * 名前変更を確定する。
     * 空文字の場合はnull（自動生成名）として保存する。
     */
    private fun confirmRename() {
        val historyId = _uiState.value.renamingHistoryId ?: return
        val input = _uiState.value.renameInput.trim()
        // nullは自動生成名に戻すことを意味する（仕様: 空文字の場合は自動生成名に戻す）
        val newName = if (input.isEmpty()) null else input

        // ダイアログを先に閉じる
        _uiState.value = _uiState.value.copy(
            renamingHistoryId = null,
            renameInput = "",
        )

        viewModelScope.launch {
            syncHistoryRepository.updateHistoryName(
                historyId = historyId,
                newName = newName,
            ).fold(
                onSuccess = {
                    // 成功時はFlowで自動更新されるためSnackbar不要
                },
                onFailure = {
                    _sideEffect.emit(SyncHistoryListSideEffect.ShowRenameError)
                },
            )
        }
    }

    // ============================================
    // 復元機能
    // ============================================

    /**
     * 履歴から TimelineSync 画面にチャンネルを復元する。
     *
     * 処理フロー:
     * 1. 履歴をIDで取得
     * 2. recordUsage で使用状況を更新（失敗してもナビゲーションはブロックしない）
     * 3. SavedChannelInfo → PresetChannel に変換してJSONエンコード
     * 4. 今日の日付をpresetDateとして設定
     * 5. NavigateToTimeline SideEffect を発行
     *
     * @param historyId 復元対象の履歴ID
     */
    private fun restoreHistory(historyId: String) {
        viewModelScope.launch {
            // 履歴取得
            val history = syncHistoryRepository.getHistoryById(historyId).getOrNull()
            if (history == null) {
                _sideEffect.emit(SyncHistoryListSideEffect.ShowRestoreError)
                return@launch
            }

            // 使用状況を更新（失敗してもナビゲーションをブロックしない）
            syncHistoryRepository.recordUsage(historyId)

            // 今日の日付を取得
            val today = clock.now()
                .toLocalDateTime(timeZone)
                .date
            val presetDate = today.toString()

            // SavedChannelInfo → PresetChannel 変換
            val presetChannels = history.channels.map { it.toPresetChannel() }
            val presetChannelsJson = Json.encodeToString(presetChannels)

            // TimelineSync画面へのナビゲーションSideEffectを発行
            _sideEffect.emit(
                SyncHistoryListSideEffect.NavigateToTimeline(
                    presetChannelsJson = presetChannelsJson,
                    presetDate = presetDate,
                ),
            )
        }
    }
}

/**
 * SavedChannelInfo を PresetChannel に変換する拡張関数。
 *
 * ArchiveHome → TimelineSync で確立された変換パターンを再利用する。
 */
private fun SavedChannelInfo.toPresetChannel(): PresetChannel = PresetChannel(
    channelId = channelId,
    channelName = channelName,
    channelIconUrl = channelIconUrl,
    serviceType = serviceType.name,
)
