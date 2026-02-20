@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.archive_home

import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.VideoServiceType

/**
 * アーカイブHome画面のユーザーIntent定義。
 *
 * MVI アーキテクチャパターンに従う。
 *
 * Epic: Channel Follow & Archive Home (US-3)
 * Story: US-3 (Archive Home Display)
 */
sealed interface ArchiveHomeIntent {
    /**
     * 画面データを読み込む。
     * 画面初回表示時に呼ばれる。
     */
    data object LoadScreen : ArchiveHomeIntent

    /**
     * 日付を選択する。
     * アーカイブ表示が選択日に更新される。
     */
    data class SelectDate(val date: LocalDate) : ArchiveHomeIntent

    /**
     * 前週に移動する。
     */
    data object NavigateToPreviousWeek : ArchiveHomeIntent

    /**
     * 次週に移動する。
     */
    data object NavigateToNextWeek : ArchiveHomeIntent

    /**
     * エラーメッセージをクリアする。
     */
    data object ClearError : ArchiveHomeIntent

    /**
     * データ再読み込みを実行する。
     */
    data object Retry : ArchiveHomeIntent

    // ============================================
    // チャンネル検索モーダル（ChannelAddBottomSheet再利用）
    // ============================================

    /**
     * チャンネル追加モーダルを開く。
     */
    data object OpenChannelAddModal : ArchiveHomeIntent

    /**
     * チャンネル追加モーダルを閉じる。
     */
    data object CloseChannelAddModal : ArchiveHomeIntent

    /**
     * プラットフォームを選択する。
     */
    data class SelectPlatform(val platform: VideoServiceType) : ArchiveHomeIntent

    /**
     * チャンネル検索クエリを更新する。
     */
    data class UpdateChannelSearchQuery(val query: String) : ArchiveHomeIntent

    /**
     * チャンネルをフォロー/アンフォローする。
     */
    data class ToggleFollow(val channel: ChannelInfo) : ArchiveHomeIntent

    // ============================================
    // アーカイブ選択（US-4）
    // ============================================

    /**
     * アーカイブの選択をトグルする。
     * 選択済みの場合は解除、未選択の場合は選択。最大10件制限あり。
     */
    data class ToggleArchiveSelection(val videoId: String) : ArchiveHomeIntent

    /**
     * タイムラインを開く。
     * 選択中のアーカイブをプリセットとしてTimelineSyncに遷移する。
     */
    data object OpenTimeline : ArchiveHomeIntent
}

/**
 * アーカイブHome画面のSideEffect定義。
 *
 * 一度だけ実行されるイベント（Snackbar、Navigation等）。
 */
sealed interface ArchiveHomeSideEffect {
    /**
     * エラーメッセージを表示する。
     */
    data class ShowError(val message: String) : ArchiveHomeSideEffect

    /**
     * フォロー/アンフォロー後のフィードバックSnackbarを表示する。
     */
    data class ShowFollowFeedback(val message: String) : ArchiveHomeSideEffect

    /**
     * タイムライン画面へ遷移する。
     * 選択チャンネル情報とプリセット日付を含む。
     */
    data class NavigateToTimeline(
        val presetChannelsJson: String,
        val presetDate: String,
    ) : ArchiveHomeSideEffect
}
