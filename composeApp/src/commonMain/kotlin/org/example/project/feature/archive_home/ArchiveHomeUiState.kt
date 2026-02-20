@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.archive_home

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.FollowedChannel
import org.example.project.domain.model.VideoServiceType

/**
 * アーカイブHome画面のUI状態。
 *
 * フォロー中チャンネルの選択日アーカイブを表示する。
 *
 * Epic: Channel Follow & Archive Home (US-3)
 * Story: US-3 (Archive Home Display)
 */
data class ArchiveHomeUiState(
    /**
     * データ読み込み中フラグ。
     */
    val isLoading: Boolean = false,

    /**
     * 表示するアーカイブアイテムのリスト。
     */
    val archives: List<ArchiveItem> = emptyList(),

    /**
     * フォロー中のチャンネルリスト。
     * 空状態判定に使用。
     */
    val followedChannels: List<FollowedChannel> = emptyList(),

    /**
     * 現在選択中の日付。
     * デフォルトは今日。
     */
    val selectedDate: LocalDate,

    /**
     * 表示中の週の開始日（月曜日）。
     * WeekCalendar のスクロール位置管理に使用。
     */
    val displayedWeekStart: LocalDate,

    /**
     * エラーメッセージ。
     */
    val errorMessage: String? = null,

    // ============================================
    // チャンネル検索モーダル（ChannelAddBottomSheet再利用）
    // ============================================

    /**
     * チャンネル追加モーダルの表示フラグ。
     */
    val isChannelAddModalVisible: Boolean = false,

    /**
     * モーダルで選択中のプラットフォーム。
     */
    val selectedPlatform: VideoServiceType = VideoServiceType.TWITCH,

    /**
     * チャンネル検索クエリ。
     */
    val channelSearchQuery: String = "",

    /**
     * チャンネル検索候補リスト。
     */
    val channelSuggestions: List<ChannelInfo> = emptyList(),

    /**
     * チャンネル検索中フラグ。
     */
    val isSearchingChannels: Boolean = false,

    /**
     * フォロー済みチャンネルIDのセット。
     * 選択中のプラットフォームでフィルタ済み。
     */
    val followedChannelIds: Set<String> = emptySet(),

    // ============================================
    // アーカイブ選択（US-4）
    // ============================================

    /**
     * 選択中のアーカイブVideoIDのセット。
     * タップでトグル。最大10件。
     */
    val selectedArchiveIds: Set<String> = emptySet(),
) {
    /**
     * フォロー中チャンネルが0件かどうか。
     */
    val hasNoFollowedChannels: Boolean
        get() = followedChannels.isEmpty() && !isLoading

    /**
     * アーカイブが0件かどうか（フォローはあるがアーカイブがない）。
     */
    val hasNoArchives: Boolean
        get() = followedChannels.isNotEmpty() && archives.isEmpty() && !isLoading

    /**
     * 選択中のアーカイブ件数。
     */
    val selectedCount: Int
        get() = selectedArchiveIds.size

    /**
     * 1件以上選択中かどうか。
     */
    val hasSelection: Boolean
        get() = selectedArchiveIds.isNotEmpty()
}

/**
 * アーカイブアイテムのUI表示モデル。
 *
 * VideoDetails + FollowedChannel から派生。
 */
data class ArchiveItem(
    /**
     * 動画ID。
     */
    val videoId: String,

    /**
     * 動画タイトル。
     */
    val title: String,

    /**
     * サムネイルURL。
     */
    val thumbnailUrl: String,

    /**
     * チャンネルID。
     */
    val channelId: String,

    /**
     * チャンネル名。
     */
    val channelName: String,

    /**
     * チャンネルアイコンURL。
     */
    val channelIconUrl: String,

    /**
     * 動画サービスタイプ。
     */
    val serviceType: VideoServiceType,

    /**
     * 配信開始日時。
     */
    val publishedAt: Instant?,

    /**
     * 動画の長さ（秒）。
     */
    val durationSeconds: Float?,
)
