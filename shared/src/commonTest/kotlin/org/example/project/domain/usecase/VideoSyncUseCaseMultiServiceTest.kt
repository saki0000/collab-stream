package org.example.project.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.example.project.domain.model.LiveStreamingDetails
import org.example.project.domain.model.TwitchStreamInfo
import org.example.project.domain.model.TwitchVideoDetailsImpl
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.model.VideoSnippet
import org.example.project.domain.model.YouTubeVideoDetailsImpl
import org.example.project.domain.repository.VideoSyncRepository
import org.example.project.runTest

class MockMultiServiceVideoSyncRepository : VideoSyncRepository {
    var shouldReturnError: Boolean = false
    var errorToReturn: Throwable = RuntimeException("Mock error")
    var videoDetailsToReturn: VideoDetails? = null

    @OptIn(ExperimentalTime::class)
    override suspend fun getVideoDetails(videoId: String, serviceType: VideoServiceType): Result<VideoDetails> {
        return if (shouldReturnError) {
            Result.failure(errorToReturn)
        } else {
            videoDetailsToReturn?.let { Result.success(it) }
                ?: Result.failure(RuntimeException("No mock data set"))
        }
    }
}

@OptIn(ExperimentalTime::class)
class VideoSyncUseCaseMultiServiceTest {

    private val mockRepository = MockMultiServiceVideoSyncRepository()
    private val useCase = VideoSyncUseCaseImpl(mockRepository)

    private val testYouTubeStreamStartTime = Instant.parse("2023-12-25T10:00:00Z")
    private val testYouTubeVideoDetails = YouTubeVideoDetailsImpl(
        id = "test-youtube-video-id",
        snippet = VideoSnippet(
            title = "Test YouTube Live Stream",
            description = "A test live stream",
            channelId = "test-channel-id",
            channelTitle = "Test Channel",
        ),
        liveStreamingDetails = LiveStreamingDetails(
            actualStartTime = testYouTubeStreamStartTime,
            scheduledStartTime = null,
            actualEndTime = null,
        ),
    )

    private val testTwitchStreamStartTime = Instant.parse("2023-12-25T15:00:00Z")
    private val testTwitchVideoDetails = TwitchVideoDetailsImpl(
        id = "test-twitch-video-id",
        snippet = VideoSnippet(
            title = "Test Twitch Stream",
            description = "A test Twitch stream",
            channelId = "test-twitch-user-id",
            channelTitle = "Test Twitch Streamer",
        ),
        streamInfo = TwitchStreamInfo(
            streamId = "test-stream-id",
            createdAt = testTwitchStreamStartTime.toString(),
            publishedAt = testTwitchStreamStartTime.toString(),
            type = "archive",
            duration = "2h30m15s",
            viewable = "public",
        ),
    )

    @Test
    fun `should sync YouTube video to absolute time successfully`() = runTest {
        // Arrange
        mockRepository.videoDetailsToReturn = testYouTubeVideoDetails
        val playbackSeconds = 120f // 2 minutes

        // Act
        val result = useCase.syncVideoToAbsoluteTime(
            videoId = "test-youtube-video-id",
            currentPlaybackSeconds = playbackSeconds,
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Assert
        assertTrue(result.isSuccess)
        val syncInfo = result.getOrThrow()

        assertEquals("test-youtube-video-id", syncInfo.videoId)
        assertEquals(playbackSeconds, syncInfo.playbackSeconds)
        assertEquals(testYouTubeStreamStartTime, syncInfo.streamStartTime)

        val expectedAbsoluteTime = Instant.parse("2023-12-25T10:02:00Z") // Start time + 2 minutes
        assertEquals(expectedAbsoluteTime, syncInfo.absoluteTime)
    }

    @Test
    fun `should sync Twitch video to absolute time successfully`() = runTest {
        // Arrange
        mockRepository.videoDetailsToReturn = testTwitchVideoDetails
        val playbackSeconds = 300f // 5 minutes

        // Act
        val result = useCase.syncVideoToAbsoluteTime(
            videoId = "test-twitch-video-id",
            currentPlaybackSeconds = playbackSeconds,
            serviceType = VideoServiceType.TWITCH,
        )

        // Assert
        assertTrue(result.isSuccess)
        val syncInfo = result.getOrThrow()

        assertEquals("test-twitch-video-id", syncInfo.videoId)
        assertEquals(playbackSeconds, syncInfo.playbackSeconds)
        assertEquals(testTwitchStreamStartTime, syncInfo.streamStartTime)

        val expectedAbsoluteTime = Instant.parse("2023-12-25T15:05:00Z") // Start time + 5 minutes
        assertEquals(expectedAbsoluteTime, syncInfo.absoluteTime)
    }

    @Test
    fun `should handle repository error gracefully`() = runTest {
        // Arrange
        mockRepository.shouldReturnError = true
        mockRepository.errorToReturn = RuntimeException("API connection failed")

        // Act
        val result = useCase.syncVideoToAbsoluteTime(
            videoId = "any-video-id",
            currentPlaybackSeconds = 60f,
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Assert
        assertTrue(result.isFailure)
        assertEquals("API connection failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should validate input parameters for multi-service sync`() = runTest {
        // Test blank video ID
        val blankIdResult = useCase.syncVideoToAbsoluteTime(
            videoId = "",
            currentPlaybackSeconds = 60f,
            serviceType = VideoServiceType.TWITCH,
        )
        assertTrue(blankIdResult.isFailure)
        assertEquals("Video ID cannot be blank", blankIdResult.exceptionOrNull()?.message)

        // Test negative playback seconds
        val negativeSecondsResult = useCase.syncVideoToAbsoluteTime(
            videoId = "valid-id",
            currentPlaybackSeconds = -10f,
            serviceType = VideoServiceType.YOUTUBE,
        )
        assertTrue(negativeSecondsResult.isFailure)
        assertEquals("Playback seconds must be non-negative", negativeSecondsResult.exceptionOrNull()?.message)
    }

    @Test
    fun `should handle video without start time information`() = runTest {
        // Arrange - YouTube video without live streaming details
        val videoWithoutStartTime = YouTubeVideoDetailsImpl(
            id = "no-start-time-video",
            snippet = VideoSnippet(
                title = "Regular Upload",
                description = "Not a live stream",
                channelId = "test-channel",
                channelTitle = "Test Channel",
            ),
            liveStreamingDetails = null,
        )
        mockRepository.videoDetailsToReturn = videoWithoutStartTime

        // Act
        val result = useCase.syncVideoToAbsoluteTime(
            videoId = "no-start-time-video",
            currentPlaybackSeconds = 60f,
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("does not have start time information") == true)
    }
}
