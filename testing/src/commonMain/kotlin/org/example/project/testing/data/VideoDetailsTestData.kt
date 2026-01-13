@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.testing.data

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import org.example.project.domain.model.LiveStreamingDetails
import org.example.project.domain.model.TwitchStreamInfo
import org.example.project.domain.model.TwitchVideoDetailsImpl
import org.example.project.domain.model.VideoSnippet
import org.example.project.domain.model.YouTubeVideoDetailsImpl

/**
 * VideoDetailsテストデータ生成ユーティリティ。
 *
 * テストで使用する標準的なVideoDetails インスタンスを提供。
 */
object VideoDetailsTestData {
    /** デフォルトのストリーム開始時刻 */
    val DEFAULT_START_TIME: Instant = Instant.parse("2023-12-25T10:00:00Z")

    /** デフォルトのストリーム終了時刻（3時間後） */
    val DEFAULT_END_TIME: Instant = Instant.parse("2023-12-25T13:00:00Z")

    /**
     * 標準的なYouTube VideoDetails を生成。
     */
    fun createYouTubeVideoDetails(
        id: String = "test-youtube-video-id",
        title: String = "Test YouTube Video",
        description: String = "Test Description",
        channelId: String = "test-youtube-channel-id",
        channelTitle: String = "Test YouTube Channel",
        actualStartTime: Instant? = DEFAULT_START_TIME,
        scheduledStartTime: Instant? = DEFAULT_START_TIME,
        actualEndTime: Instant? = DEFAULT_END_TIME,
    ): YouTubeVideoDetailsImpl =
        YouTubeVideoDetailsImpl(
            id = id,
            snippet =
            VideoSnippet(
                title = title,
                description = description,
                channelId = channelId,
                channelTitle = channelTitle,
            ),
            liveStreamingDetails =
            if (actualStartTime != null) {
                LiveStreamingDetails(
                    actualStartTime = actualStartTime,
                    scheduledStartTime = scheduledStartTime,
                    actualEndTime = actualEndTime,
                )
            } else {
                null
            },
        )

    /**
     * ライブストリーミング情報なしのYouTube VideoDetails を生成。
     */
    fun createYouTubeVideoDetailsWithoutLiveInfo(
        id: String = "test-youtube-video-id",
        title: String = "Test YouTube Video",
        description: String = "Test Description",
        channelId: String = "test-youtube-channel-id",
        channelTitle: String = "Test YouTube Channel",
    ): YouTubeVideoDetailsImpl =
        YouTubeVideoDetailsImpl(
            id = id,
            snippet =
            VideoSnippet(
                title = title,
                description = description,
                channelId = channelId,
                channelTitle = channelTitle,
            ),
            liveStreamingDetails = null,
        )

    /**
     * 標準的なTwitch VideoDetails を生成。
     */
    fun createTwitchVideoDetails(
        id: String = "test-twitch-video-id",
        title: String = "Test Twitch Video",
        description: String = "Test Description",
        channelId: String = "test-twitch-channel-id",
        channelTitle: String = "Test Twitch Channel",
        createdAt: String = "2023-12-25T10:00:00Z",
        publishedAt: String = "2023-12-25T10:00:00Z",
        duration: String = "3h0m0s",
        type: String = "archive",
    ): TwitchVideoDetailsImpl =
        TwitchVideoDetailsImpl(
            id = id,
            snippet =
            VideoSnippet(
                title = title,
                description = description,
                channelId = channelId,
                channelTitle = channelTitle,
            ),
            streamInfo =
            TwitchStreamInfo(
                streamId = id,
                createdAt = createdAt,
                publishedAt = publishedAt,
                type = type,
                duration = duration,
                viewable = "public",
            ),
        )

    /**
     * ストリーム情報なしのTwitch VideoDetails を生成。
     */
    fun createTwitchVideoDetailsWithoutStreamInfo(
        id: String = "test-twitch-video-id",
        title: String = "Test Twitch Video",
        description: String = "Test Description",
        channelId: String = "test-twitch-channel-id",
        channelTitle: String = "Test Twitch Channel",
    ): TwitchVideoDetailsImpl =
        TwitchVideoDetailsImpl(
            id = id,
            snippet =
            VideoSnippet(
                title = title,
                description = description,
                channelId = channelId,
                channelTitle = channelTitle,
            ),
            streamInfo = null,
        )

    /**
     * 複数のYouTube VideoDetails リストを生成。
     */
    fun createYouTubeVideoDetailsList(
        count: Int = 3,
        baseId: String = "test-video",
        startTimeOffset: Duration = Duration.ZERO,
    ): List<YouTubeVideoDetailsImpl> =
        (0 until count).map { index ->
            createYouTubeVideoDetails(
                id = "$baseId-$index",
                title = "Test Video $index",
                actualStartTime = DEFAULT_START_TIME + startTimeOffset + (index.hours),
            )
        }

    /**
     * 複数のTwitch VideoDetails リストを生成。
     */
    fun createTwitchVideoDetailsList(
        count: Int = 3,
        baseId: String = "test-twitch-video",
        startTimeOffset: Duration = Duration.ZERO,
    ): List<TwitchVideoDetailsImpl> =
        (0 until count).map { index ->
            val startTime = DEFAULT_START_TIME + startTimeOffset + (index.hours)
            createTwitchVideoDetails(
                id = "$baseId-$index",
                title = "Test Twitch Video $index",
                createdAt = startTime.toString(),
                publishedAt = startTime.toString(),
            )
        }
}
