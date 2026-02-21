@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.TimestampMarker
import org.example.project.domain.model.VideoServiceType

/**
 * Data class representing the UI state for Timeline Sync screen.
 *
 * Contains all necessary state information for timeline display and
 * week/date navigation.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display), US-2 (Channel Add/Remove)
 */
data class TimelineSyncUiState(
    /**
     * Whether data is currently being loaded.
     */
    val isLoading: Boolean = false,

    /**
     * List of channels with their stream information.
     */
    val channels: List<SyncChannel> = emptyList(),

    /**
     * Currently selected date for timeline display.
     * Defaults to today.
     */
    val selectedDate: LocalDate = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault()),

    /**
     * Start date of the currently displayed week.
     * Used for calendar week navigation.
     */
    val displayedWeekStart: LocalDate = selectedDate.startOfWeek(),

    /**
     * Global sync time for timeline synchronization.
     * Null when no sync time is set (initial state).
     * Story 1: Display only, selection is Story 3.
     */
    val syncTime: Instant? = null,

    /**
     * Error message to display, if any.
     */
    val errorMessage: String? = null,

    /**
     * Whether the user is currently dragging the sync line.
     * Story 3: Sync Time Selection
     */
    val isDragging: Boolean = false,

    // ============================================
    // Story 2: Channel Add/Remove, Story 5: Multi-Platform Search
    // ============================================

    /**
     * チャンネル追加モーダルで選択中のプラットフォーム。
     * デフォルトは Twitch。
     * Story 5: Multi-Platform Search
     */
    val selectedPlatform: VideoServiceType = VideoServiceType.TWITCH,

    /**
     * Whether the channel add modal (bottom sheet) is visible.
     */
    val isChannelAddModalVisible: Boolean = false,

    /**
     * Current search query in the channel add modal.
     */
    val channelSearchQuery: String = "",

    /**
     * List of channel suggestions from search results.
     */
    val channelSuggestions: List<ChannelInfo> = emptyList(),

    /**
     * Whether channel search is in progress.
     */
    val isSearchingChannels: Boolean = false,

    /**
     * Error message for channel add operations.
     * Displayed as snackbar in the modal.
     */
    val channelAddError: String? = null,

    /**
     * Recently deleted channel for undo functionality.
     * Null when no channel was recently deleted.
     */
    val recentlyDeletedChannel: SyncChannel? = null,

    // ============================================
    // Channel Follow (US-2)
    // ============================================

    /**
     * フォロー済みチャンネルIDのセット。
     * 選択中のプラットフォームでフィルタ済み。
     */
    val followedChannelIds: Set<String> = emptySet(),

    // ============================================
    // Story 3: コメントタイムスタンプマーカー (US-3)
    // ============================================

    /**
     * チャンネルIDをキーとするコメント状態マップ。
     * YouTube チャンネルのみ対象（Twitch は空状態を保持）。
     */
    val channelComments: Map<String, ChannelCommentState> = emptyMap(),

    /**
     * 現在選択中のマーカープレビュー情報。
     * マーカータップ時に設定され、プレビュー閉じる時に null になる。
     */
    val selectedMarkerPreview: TimestampMarkerPreview? = null,

    // ============================================
    // 履歴保存 (US-2: 同期チャンネル履歴保存)
    // ============================================

    /**
     * 履歴保存処理中かどうか。
     * trueの場合、保存ボタンを非活性にする。
     */
    val isSavingHistory: Boolean = false,

    /**
     * 重複確認ダイアログの表示状態。
     * 同じチャンネル組み合わせが既に保存されている場合にtrueになる。
     */
    val showDuplicateDialog: Boolean = false,

    /**
     * 重複している履歴のID。
     * 上書き時に既存履歴を特定するために使用する。
     * showDuplicateDialogがtrueの場合にのみ有効。
     */
    val duplicateHistoryId: String? = null,
) {
    /**
     * Whether the channel list is empty (and not loading).
     */
    val isEmpty: Boolean
        get() = channels.isEmpty() && !isLoading

    /**
     * Number of active channels (channels with selected streams).
     */
    val activeChannelCount: Int
        get() = channels.count { it.selectedStream != null }

    /**
     * List of dates in the currently displayed week.
     */
    val weekDays: List<LocalDate>
        get() = (0..6).map { displayedWeekStart.plus(it, DateTimeUnit.DAY) }

    /**
     * Sync time slidable range calculated from all streams.
     * Range is from earliest start time to latest end time.
     * Returns null if no streams are selected.
     * Story 3: Sync Time Selection
     */
    val syncTimeRange: Pair<Instant, Instant>?
        get() {
            val streams = channels.mapNotNull { it.selectedStream }
            if (streams.isEmpty()) return null

            val earliestStart = streams.mapNotNull { it.startTime }.minOrNull() ?: return null
            val latestEnd = streams.mapNotNull { it.endTime }.maxOrNull()
                ?: kotlin.time.Clock.System.now()

            return earliestStart to latestEnd
        }

    /**
     * Whether a new channel can be added.
     * Limited to maximum 10 channels.
     * Story 2: Channel Add
     */
    val canAddChannel: Boolean
        get() = channels.size < MAX_CHANNELS

    /**
     * 履歴保存ボタンの有効/無効。
     * チャンネルが2つ以上かつ保存中でない場合に有効。
     * Epic: 同期チャンネル履歴保存 (US-2)
     */
    val canSaveHistory: Boolean
        get() = channels.size >= MIN_CHANNELS_FOR_SAVE && !isSavingHistory

    companion object {
        /**
         * Maximum number of channels allowed in timeline.
         */
        const val MAX_CHANNELS = 10

        /**
         * 履歴保存に必要な最小チャンネル数。
         * Epic: 同期チャンネル履歴保存 (US-2)
         */
        const val MIN_CHANNELS_FOR_SAVE = 2
    }
}

// ============================================
// Story 3: コメントタイムスタンプマーカー 関連データクラス (US-3)
// ============================================

/**
 * コメント読み込み状態を表す列挙型。
 */
enum class CommentLoadStatus {
    /** コメント未取得（初期状態） */
    NOT_LOADED,

    /** コメント読み込み中 */
    LOADING,

    /** コメント読み込み完了（タイムスタンプなしも含む） */
    LOADED,

    /** ネットワークエラー等で読み込み失敗 */
    ERROR,

    /** コメントが無効化されている（YouTube 403 commentsDisabled） */
    DISABLED,
}

/**
 * チャンネルごとのコメント状態を保持するデータクラス。
 */
data class ChannelCommentState(
    /** 対象動画ID */
    val videoId: String,

    /** 読み込み状態 */
    val status: CommentLoadStatus,

    /** 抽出されたタイムスタンプマーカーリスト */
    val markers: List<TimestampMarker> = emptyList(),

    /** エラーメッセージ（status が ERROR または DISABLED の場合のみ） */
    val errorMessage: String? = null,
)

/**
 * マーカープレビュー表示用データクラス。
 * タップされたマーカーの情報とチャンネルIDを保持する。
 */
data class TimestampMarkerPreview(
    /** マーカーが属するチャンネルID */
    val channelId: String,

    /** タップされたマーカー情報 */
    val marker: TimestampMarker,
)

/**
 * Extension function to get the start of the week (Monday) for a given date.
 * Uses ISO week definition where Monday is the first day of the week.
 */
fun LocalDate.startOfWeek(): LocalDate {
    val daysFromMonday = this.dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber
    return this.plus(-daysFromMonday, DateTimeUnit.DAY)
}

/**
 * Data class representing timeline bar display information for a channel.
 *
 * Contains calculated positions for rendering timeline bars based on
 * the selected date's 0:00-24:00 time axis.
 */
data class TimelineBarInfo(
    /**
     * Channel ID this bar belongs to.
     */
    val channelId: String,

    /**
     * Start position as fraction (0.0 - 1.0) of the day.
     * 0.0 = 00:00, 1.0 = 24:00
     */
    val startFraction: Float,

    /**
     * End position as fraction (0.0 - 1.0) of the day.
     */
    val endFraction: Float,

    /**
     * Display start time in HH:MM format.
     */
    val displayStartTime: String,

    /**
     * Display end time in HH:MM format.
     */
    val displayEndTime: String,

    /**
     * Whether this is a live stream (endTime is null).
     */
    val isLive: Boolean = false,

    /**
     * Whether this stream is upcoming (hasn't started yet).
     */
    val isUpcoming: Boolean = false,

    /**
     * Minutes until stream starts (for upcoming streams).
     */
    val minutesToStart: Long? = null,
)
