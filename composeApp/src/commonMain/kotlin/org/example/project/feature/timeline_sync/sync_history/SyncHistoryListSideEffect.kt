package org.example.project.feature.timeline_sync.sync_history

/**
 * 同期履歴一覧画面のSideEffect定義。
 *
 * 一度だけ実行されるイベント（Snackbar表示等）を定義する。
 * MVI アーキテクチャパターンに従い、ViewModelからUIへの一方向通知に使用する。
 *
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 * Story: US-3 (履歴一覧表示)
 */
sealed interface SyncHistoryListSideEffect {

    /**
     * 削除成功時のSnackbar表示。
     * 「履歴を削除しました」メッセージを自動消去で表示する。
     */
    data object ShowDeleteSuccess : SyncHistoryListSideEffect

    /**
     * 削除失敗時のSnackbar表示。
     * 「削除に失敗しました」メッセージを自動消去で表示する。
     */
    data object ShowDeleteError : SyncHistoryListSideEffect

    /**
     * 名前変更失敗時のSnackbar表示。
     * 「名前の変更に失敗しました」メッセージを自動消去で表示する。
     */
    data object ShowRenameError : SyncHistoryListSideEffect
}
