@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import org.example.project.domain.model.LiveStreamingDetails
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.model.VideoSnippet
import org.example.project.domain.model.YouTubeVideoDetailsImpl
import org.example.project.domain.repository.VideoSyncRepository
import org.example.project.runTest

/**
 * Test mock implementation of VideoSyncRepository.
 */
class MockVideoSyncRepository : VideoSyncRepository {
    var shouldReturnError: Boolean = false
    var errorToReturn: Throwable = RuntimeException("Mock error")
    var videoDetailsToReturn: VideoDetails? = null

    override suspend fun getVideoDetails(videoId: String, serviceType: VideoServiceType): Result<VideoDetails> {
        return if (shouldReturnError) {
            Result.failure(errorToReturn)
        } else {
            videoDetailsToReturn?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException("Video not found"))
        }
    }
}

class VideoSyncUseCaseTest {

    private val mockRepository = MockVideoSyncRepository()
    private val useCase = VideoSyncUseCaseImpl(mockRepository)

    private val testStreamStartTime = Instant.parse("2023-12-25T10:00:00Z")
    private val testVideoDetails = YouTubeVideoDetailsImpl(
        id = "test-video-id",
        snippet = VideoSnippet(
            title = "Test Video",
            description = "Test Description",
            channelId = "test-channel-id",
            channelTitle = "Test Channel",
        ),
        liveStreamingDetails = LiveStreamingDetails(
            actualStartTime = testStreamStartTime,
            scheduledStartTime = testStreamStartTime,
            actualEndTime = null,
        ),
    )

    @Test
    fun `syncVideoToAbsoluteTime should calculate correct absolute time`() = runTest {
        // Arrange
        mockRepository.videoDetailsToReturn = testVideoDetails
        val playbackSeconds = 300.0f // 5 minutes

        // Act
        val result = useCase.syncVideoToAbsoluteTime("test-video-id", playbackSeconds, VideoServiceType.YOUTUBE)

        // Assert
        assertTrue(result.isSuccess)
        val syncInfo = result.getOrNull()!!

        assertEquals("test-video-id", syncInfo.videoId)
        assertEquals(playbackSeconds, syncInfo.playbackSeconds)
        assertEquals(testStreamStartTime, syncInfo.streamStartTime)

        // Check that absolute time = start time + playback seconds
        val expectedAbsoluteTime = Instant.parse("2023-12-25T10:05:00Z") // +5 minutes
        assertEquals(expectedAbsoluteTime, syncInfo.absoluteTime)
    }

    @Test
    fun `syncVideoToAbsoluteTime should handle zero playback time`() = runTest {
        // Arrange
        mockRepository.videoDetailsToReturn = testVideoDetails
        val playbackSeconds = 0.0f

        // Act
        val result = useCase.syncVideoToAbsoluteTime("test-video-id", playbackSeconds, VideoServiceType.YOUTUBE)

        // Assert
        assertTrue(result.isSuccess)
        val syncInfo = result.getOrNull()!!
        assertEquals(testStreamStartTime, syncInfo.absoluteTime) // Should equal start time
    }

    @Test
    fun `syncVideoToAbsoluteTime should fail for blank video ID`() = runTest {
        // Act
        val result = useCase.syncVideoToAbsoluteTime("", 100.0f, VideoServiceType.YOUTUBE)

        // Assert
        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun `syncVideoToAbsoluteTime should fail for negative playback seconds`() = runTest {
        // Act
        val result = useCase.syncVideoToAbsoluteTime("test-video-id", -10.0f, VideoServiceType.YOUTUBE)

        // Assert
        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun `syncVideoToAbsoluteTime should fail when video has no live streaming details`() = runTest {
        // Arrange
        val videoWithoutLiveDetails = testVideoDetails.copy(liveStreamingDetails = null)
        mockRepository.videoDetailsToReturn = videoWithoutLiveDetails

        // Act
        val result = useCase.syncVideoToAbsoluteTime("test-video-id", 100.0f, VideoServiceType.YOUTUBE)

        // Assert
        assertTrue(result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())
    }

    @Test
    fun `syncVideoToAbsoluteTime should fail when video has no actual start time`() = runTest {
        // Arrange
        val videoWithoutStartTime = testVideoDetails.copy(
            liveStreamingDetails = testVideoDetails.liveStreamingDetails!!.copy(
                actualStartTime = null,
            ),
        )
        mockRepository.videoDetailsToReturn = videoWithoutStartTime

        // Act
        val result = useCase.syncVideoToAbsoluteTime("test-video-id", 100.0f, VideoServiceType.YOUTUBE)

        // Assert
        assertTrue(result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())
    }

    @Test
    fun `syncVideoToAbsoluteTime should propagate repository errors`() = runTest {
        // Arrange
        mockRepository.shouldReturnError = true
        mockRepository.errorToReturn = RuntimeException("API Error")

        // Act
        val result = useCase.syncVideoToAbsoluteTime("test-video-id", 100.0f, VideoServiceType.YOUTUBE)

        // Assert
        assertTrue(result.isFailure)
        assertIs<RuntimeException>(result.exceptionOrNull())
    }
}

// Helper function to run suspending tests (defined in TestUtils.kt)
