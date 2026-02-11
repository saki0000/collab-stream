@file:OptIn(ExperimentalTime::class)

package org.example.project.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * DeepLinkInfo生成ロジックのテスト
 * Specification: feature/timeline_sync/SPECIFICATION.md
 * Story: US-4 (外部アプリ連携)
 */
class DeepLinkInfoTest {

    // ========================================
    // YouTube DeepLink生成
    // ========================================

    @Test
    fun `YouTube_正しいDeepLink URIが生成されること`() {
        // Arrange
        val channel = createYouTubeChannel(videoId = "abc123", seekPosition = 120f)

        // Act
        val result = channel.toDeepLinkInfo()

        // Assert
        assertEquals("youtube://watch?v=abc123&t=120", result?.deepLinkUri)
    }

    @Test
    fun `YouTube_正しいフォールバックURLが生成されること`() {
        // Arrange
        val channel = createYouTubeChannel(videoId = "abc123", seekPosition = 120f)

        // Act
        val result = channel.toDeepLinkInfo()

        // Assert
        assertEquals("https://www.youtube.com/watch?v=abc123&t=120s", result?.fallbackUrl)
    }

    @Test
    fun `YouTube_シーク位置0秒の場合_t=0が生成されること`() {
        // Arrange
        val channel = createYouTubeChannel(videoId = "vid001", seekPosition = 0f)

        // Act
        val result = channel.toDeepLinkInfo()

        // Assert
        assertEquals("youtube://watch?v=vid001&t=0", result?.deepLinkUri)
        assertEquals("https://www.youtube.com/watch?v=vid001&t=0s", result?.fallbackUrl)
    }

    // ========================================
    // Twitch DeepLink生成
    // ========================================

    @Test
    fun `Twitch_正しいDeepLink URIが生成されること`() {
        // Arrange
        val channel = createTwitchChannel(videoId = "tw456", seekPosition = 3600f)

        // Act
        val result = channel.toDeepLinkInfo()

        // Assert
        assertEquals("twitch://video/tw456?t=3600s", result?.deepLinkUri)
    }

    @Test
    fun `Twitch_正しいフォールバックURLが生成されること`() {
        // Arrange
        val channel = createTwitchChannel(videoId = "tw456", seekPosition = 3600f)

        // Act
        val result = channel.toDeepLinkInfo()

        // Assert
        assertEquals("https://www.twitch.tv/videos/tw456?t=3600s", result?.fallbackUrl)
    }

    // ========================================
    // シーク位置の変換
    // ========================================

    @Test
    fun `シーク位置_小数点以下が切り捨てられること`() {
        // Arrange
        val channel = createYouTubeChannel(videoId = "vid", seekPosition = 99.7f)

        // Act
        val result = channel.toDeepLinkInfo()

        // Assert
        assertEquals("youtube://watch?v=vid&t=99", result?.deepLinkUri)
    }

    @Test
    fun `シーク位置_負の値が0に丸められること`() {
        // Arrange
        val channel = createYouTubeChannel(videoId = "vid", seekPosition = -5f)

        // Act
        val result = channel.toDeepLinkInfo()

        // Assert
        assertEquals("youtube://watch?v=vid&t=0", result?.deepLinkUri)
    }

    // ========================================
    // null ケース
    // ========================================

    @Test
    fun `ストリーム未選択の場合_nullが返ること`() {
        // Arrange
        val channel = SyncChannel(
            channelId = "ch1",
            channelName = "Channel",
            channelIconUrl = "",
            serviceType = VideoServiceType.YOUTUBE,
            selectedStream = null,
            syncStatus = SyncStatus.NOT_SYNCED,
            targetSeekPosition = null,
        )

        // Act
        val result = channel.toDeepLinkInfo()

        // Assert
        assertNull(result)
    }

    @Test
    fun `再生位置未計算の場合_nullが返ること`() {
        // Arrange
        val channel = createYouTubeChannel(videoId = "vid", seekPosition = null)

        // Act
        val result = channel.toDeepLinkInfo()

        // Assert
        assertNull(result)
    }

    // ========================================
    // ヘルパー
    // ========================================

    private val baseTime = Instant.parse("2024-01-01T10:00:00Z")

    private fun createYouTubeChannel(videoId: String, seekPosition: Float?): SyncChannel =
        SyncChannel(
            channelId = "yt_ch",
            channelName = "YouTube Channel",
            channelIconUrl = "",
            serviceType = VideoServiceType.YOUTUBE,
            selectedStream = SelectedStreamInfo(
                id = videoId,
                title = "Stream",
                thumbnailUrl = "",
                startTime = baseTime,
                endTime = baseTime + 3.hours,
                duration = 3.hours,
            ),
            syncStatus = SyncStatus.READY,
            targetSeekPosition = seekPosition,
        )

    private fun createTwitchChannel(videoId: String, seekPosition: Float?): SyncChannel =
        SyncChannel(
            channelId = "tw_ch",
            channelName = "Twitch Channel",
            channelIconUrl = "",
            serviceType = VideoServiceType.TWITCH,
            selectedStream = SelectedStreamInfo(
                id = videoId,
                title = "Stream",
                thumbnailUrl = "",
                startTime = baseTime,
                endTime = baseTime + 3.hours,
                duration = 3.hours,
            ),
            syncStatus = SyncStatus.READY,
            targetSeekPosition = seekPosition,
        )
}
