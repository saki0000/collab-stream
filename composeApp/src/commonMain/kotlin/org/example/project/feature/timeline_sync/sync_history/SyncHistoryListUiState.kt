@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.feature.timeline_sync.sync_history

import kotlin.time.ExperimentalTime
import org.example.project.domain.model.SyncHistory
import org.example.project.domain.repository.HistorySortOrder

/**
 * 同期履歴一覧画面のUI状態。
 *
 * 保存済みの同期履歴リストを表示する。
 * MVI アーキテクチャパターンに従い、すべてのUI状態をこのクラスに集約する。
 *
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 * Story: US-3 (履歴一覧表示)
 */
@OptIn(ExperimentalTime::class)
data class SyncHistoryListUiState(
    /**
     * データ読み込み中フラグ。
     */
    val isLoading: Boolean = false,

    /**
     * 表示する同期履歴リスト。
     */
    val histories: List<SyncHistory> = emptyList(),

    /**
     * 現在選択中のソート順。
     * デフォルトは最終使用日時の降順。
     */
    val sortOrder: HistorySortOrder = HistorySortOrder.LAST_USED,

    /**
     * エラーメッセージ（ローディング失敗時など）。
     */
    val errorMessage: String? = null,

    // ============================================
    // ダイアログ状態
    // ============================================

    /**
     * 削除確認ダイアログの表示対象となる履歴ID。
     * nullの場合はダイアログ非表示。
     */
    val deletingHistoryId: String? = null,

    /**
     * 名前変更ダイアログの表示対象となる履歴ID。
     * nullの場合はダイアログ非表示。
     */
    val renamingHistoryId: String? = null,

    /**
     * 名前変更ダイアログのテキスト入力値。
     */
    val renameInput: String = "",

    /**
     * ソートドロップダウンメニューの表示フラグ。
     */
    val isSortMenuVisible: Boolean = false,
) {
    /**
     * 削除確認ダイアログを表示するかどうか。
     */
    val isDeleteDialogVisible: Boolean
        get() = deletingHistoryId != null

    /**
     * 名前変更ダイアログを表示するかどうか。
     */
    val isRenameDialogVisible: Boolean
        get() = renamingHistoryId != null

    /**
     * 履歴が0件かどうか（空状態表示の判定用）。
     * ローディング中は空状態を表示しない。
     */
    val isEmpty: Boolean
        get() = histories.isEmpty() && !isLoading
}
