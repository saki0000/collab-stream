package org.example.project.feature.timeline_sync.sync_history

import org.example.project.domain.repository.HistorySortOrder

/**
 * 同期履歴一覧画面のユーザーIntent定義。
 *
 * MVI アーキテクチャパターンに従い、ユーザーの操作をすべてIntentとして定義する。
 *
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 * Story: US-3 (履歴一覧表示)
 */
sealed interface SyncHistoryListIntent {

    /**
     * 画面データを読み込む。
     * 画面初回表示時に呼ばれる。
     */
    data object LoadScreen : SyncHistoryListIntent

    // ============================================
    // ソート機能
    // ============================================

    /**
     * ソートドロップダウンメニューを開く。
     */
    data object OpenSortMenu : SyncHistoryListIntent

    /**
     * ソートドロップダウンメニューを閉じる。
     */
    data object CloseSortMenu : SyncHistoryListIntent

    /**
     * ソート順を変更する。
     * 選択したソート順でリストが即座に更新される。
     */
    data class ChangeSortOrder(val sortOrder: HistorySortOrder) : SyncHistoryListIntent

    // ============================================
    // 削除機能
    // ============================================

    /**
     * 削除確認ダイアログを表示する。
     * @param historyId 削除対象の履歴ID
     */
    data class ShowDeleteDialog(val historyId: String) : SyncHistoryListIntent

    /**
     * 削除確認ダイアログを閉じる（キャンセル）。
     */
    data object DismissDeleteDialog : SyncHistoryListIntent

    /**
     * 削除を確定する。
     * 現在の [SyncHistoryListUiState.deletingHistoryId] の履歴を削除する。
     */
    data object ConfirmDelete : SyncHistoryListIntent

    // ============================================
    // 名前変更機能
    // ============================================

    /**
     * 名前変更ダイアログを表示する。
     * @param historyId 名前変更対象の履歴ID
     * @param currentName 現在の名前（テキストフィールドにプリセット）
     */
    data class ShowRenameDialog(
        val historyId: String,
        val currentName: String,
    ) : SyncHistoryListIntent

    /**
     * 名前変更ダイアログを閉じる（キャンセル）。
     */
    data object DismissRenameDialog : SyncHistoryListIntent

    /**
     * 名前変更のテキスト入力を更新する。
     */
    data class UpdateRenameInput(val input: String) : SyncHistoryListIntent

    /**
     * 名前変更を確定する。
     * 空文字の場合は自動生成名に戻す（name = null）。
     */
    data object ConfirmRename : SyncHistoryListIntent

    // ============================================
    // 復元機能
    // ============================================

    /**
     * 履歴から TimelineSync 画面にチャンネルを復元する。
     * カードタップ時に呼ばれる（3点メニュー以外の領域）。
     * @param historyId 復元対象の履歴ID
     */
    data class RestoreHistory(val historyId: String) : SyncHistoryListIntent
}
