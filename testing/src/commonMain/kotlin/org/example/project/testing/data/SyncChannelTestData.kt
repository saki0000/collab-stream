@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.testing.data

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import org.example.project.domain.model.SelectedStreamInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.VideoServiceType

/**
 * SyncChannel テストデータ生成ユーティリティ。
 */
object SyncChannelTestData {
    val DEFAULT_START_TIME: Instant = Instant.parse("2023-12-25T10:00:00Z")

    /**
     * 標準的なSyncChannel を生成（ストリームあり）。
     */
    fun createSyncChannelWithStream(
        channelId: String = "test-channel-id",
        channelName: String = "Test Channel",
        channelIconUrl: String = "https://example.com/icon.jpg",
        serviceType: VideoServiceType = VideoServiceType.YOUTUBE,
        streamId: String = "test-stream-id",
        streamTitle: String = "Test Stream",
        startTime: Instant = DEFAULT_START_TIME,
        endTime: Instant? = DEFAULT_START_TIME + 3.hours,
        duration: Duration? = 3.hours,
        syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
        targetSeekPosition: Float? = null,
    ): SyncChannel =
        SyncChannel(
            channelId = channelId,
            channelName = channelName,
            channelIconUrl = channelIconUrl,
            serviceType = serviceType,
            selectedStream =
            SelectedStreamInfo(
                id = streamId,
                title = streamTitle,
                thumbnailUrl = "https://example.com/stream-thumbnail.jpg",
                startTime = startTime,
                endTime = endTime,
                duration = duration,
            ),
            syncStatus = syncStatus,
            targetSeekPosition = targetSeekPosition,
        )

    /**
     * ストリームなしのSyncChannel を生成。
     */
    fun createSyncChannelWithoutStream(
        channelId: String = "test-channel-id",
        channelName: String = "Test Channel",
        channelIconUrl: String = "https://example.com/icon.jpg",
        serviceType: VideoServiceType = VideoServiceType.YOUTUBE,
    ): SyncChannel =
        SyncChannel(
            channelId = channelId,
            channelName = channelName,
            channelIconUrl = channelIconUrl,
            serviceType = serviceType,
            selectedStream = null,
        )

    /**
     * 同期準備完了状態のSyncChannel を生成。
     */
    fun createReadySyncChannel(
        channelId: String = "test-channel-id",
        channelName: String = "Test Channel",
        serviceType: VideoServiceType = VideoServiceType.YOUTUBE,
        targetSeekPosition: Float = 300f,
    ): SyncChannel =
        createSyncChannelWithStream(
            channelId = channelId,
            channelName = channelName,
            serviceType = serviceType,
            syncStatus = SyncStatus.READY,
            targetSeekPosition = targetSeekPosition,
        )

    /**
     * 待機中状態のSyncChannel を生成。
     */
    fun createWaitingSyncChannel(
        channelId: String = "test-channel-id",
        channelName: String = "Test Channel",
        serviceType: VideoServiceType = VideoServiceType.YOUTUBE,
    ): SyncChannel =
        createSyncChannelWithStream(
            channelId = channelId,
            channelName = channelName,
            serviceType = serviceType,
            syncStatus = SyncStatus.WAITING,
            targetSeekPosition = null,
        )

    /**
     * 複数のSyncChannel リストを生成。
     */
    fun createSyncChannelList(
        count: Int = 3,
        withStream: Boolean = true,
    ): List<SyncChannel> =
        (0 until count).map { index ->
            val serviceType =
                if (index % 2 == 0) VideoServiceType.YOUTUBE else VideoServiceType.TWITCH
            if (withStream) {
                createSyncChannelWithStream(
                    channelId = "channel-$index",
                    channelName = "Channel $index",
                    streamId = "stream-$index",
                    serviceType = serviceType,
                )
            } else {
                createSyncChannelWithoutStream(
                    channelId = "channel-$index",
                    channelName = "Channel $index",
                    serviceType = serviceType,
                )
            }
        }
}
