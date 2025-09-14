@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.datetime.Instant
import org.example.project.data.model.YouTubeLiveStreamingDetailsDto
import org.example.project.data.model.YouTubeSnippetDto
import org.example.project.data.model.YouTubeVideoItem

class YouTubeVideoMapperTest {

    @Test
    fun `should map complete video item to domain model`() {
        // Arrange
        val videoItem = YouTubeVideoItem(
            kind = "youtube#video",
            etag = "test-etag",
            id = "test-video-id",
            snippet = YouTubeSnippetDto(
                title = "Test Video Title",
                description = "Test video description",
                channelId = "test-channel-id",
                channelTitle = "Test Channel",
            ),
            liveStreamingDetails = YouTubeLiveStreamingDetailsDto(
                actualStartTime = "2023-12-25T10:00:00Z",
                scheduledStartTime = "2023-12-25T09:55:00Z",
                actualEndTime = "2023-12-25T11:30:00Z",
                concurrentViewers = "1234",
            ),
        )

        // Act
        val result = YouTubeVideoMapper.toDomainModel(videoItem)

        // Assert
        assertEquals("test-video-id", result.id)
        assertEquals("Test Video Title", result.snippet.title)
        assertEquals("Test video description", result.snippet.description)
        assertEquals("test-channel-id", result.snippet.channelId)
        assertEquals("Test Channel", result.snippet.channelTitle)

        assertNotNull(result.liveStreamingDetails)
        val liveDetails = result.liveStreamingDetails!!
        assertEquals(Instant.parse("2023-12-25T10:00:00Z"), liveDetails.actualStartTime)
        assertEquals(Instant.parse("2023-12-25T09:55:00Z"), liveDetails.scheduledStartTime)
        assertEquals(Instant.parse("2023-12-25T11:30:00Z"), liveDetails.actualEndTime)
        assertEquals(1234L, liveDetails.concurrentViewers)
    }

    @Test
    fun `should handle missing snippet gracefully`() {
        // Arrange
        val videoItem = YouTubeVideoItem(
            kind = "youtube#video",
            etag = "test-etag",
            id = "test-video-id",
            snippet = null,
            liveStreamingDetails = null,
        )

        // Act
        val result = YouTubeVideoMapper.toDomainModel(videoItem)

        // Assert
        assertEquals("test-video-id", result.id)
        assertEquals("Unknown Title", result.snippet.title)
        assertEquals("", result.snippet.description)
        assertEquals("", result.snippet.channelId)
        assertEquals("", result.snippet.channelTitle)
        assertNull(result.liveStreamingDetails)
    }

    @Test
    fun `should handle missing live streaming details`() {
        // Arrange
        val videoItem = YouTubeVideoItem(
            kind = "youtube#video",
            etag = "test-etag",
            id = "test-video-id",
            snippet = YouTubeSnippetDto(
                title = "Regular Video",
                description = "Not a live stream",
                channelId = "test-channel-id",
                channelTitle = "Test Channel",
            ),
            liveStreamingDetails = null,
        )

        // Act
        val result = YouTubeVideoMapper.toDomainModel(videoItem)

        // Assert
        assertEquals("test-video-id", result.id)
        assertEquals("Regular Video", result.snippet.title)
        assertNull(result.liveStreamingDetails)
    }

    @Test
    fun `should handle partial live streaming details`() {
        // Arrange
        val videoItem = YouTubeVideoItem(
            kind = "youtube#video",
            etag = "test-etag",
            id = "test-video-id",
            snippet = YouTubeSnippetDto(
                title = "Live Stream",
                description = "Ongoing stream",
                channelId = "test-channel-id",
                channelTitle = "Test Channel",
            ),
            liveStreamingDetails = YouTubeLiveStreamingDetailsDto(
                actualStartTime = "2023-12-25T10:00:00Z",
                scheduledStartTime = null,
                actualEndTime = null, // Ongoing stream
                concurrentViewers = null, // Hidden viewer count
            ),
        )

        // Act
        val result = YouTubeVideoMapper.toDomainModel(videoItem)

        // Assert
        assertNotNull(result.liveStreamingDetails)
        val liveDetails = result.liveStreamingDetails
        assertEquals(Instant.parse("2023-12-25T10:00:00Z"), liveDetails.actualStartTime)
        assertNull(liveDetails.scheduledStartTime)
        assertNull(liveDetails.actualEndTime)
        assertNull(liveDetails.concurrentViewers)
    }

    @Test
    fun `should handle invalid timestamp strings`() {
        // Arrange
        val videoItem = YouTubeVideoItem(
            kind = "youtube#video",
            etag = "test-etag",
            id = "test-video-id",
            snippet = YouTubeSnippetDto(
                title = "Test Video",
                description = "Test description",
                channelId = "test-channel-id",
                channelTitle = "Test Channel",
            ),
            liveStreamingDetails = YouTubeLiveStreamingDetailsDto(
                actualStartTime = "invalid-timestamp",
                scheduledStartTime = "2023-12-25T10:00:00Z",
                actualEndTime = null,
                concurrentViewers = "not-a-number",
            ),
        )

        // Act
        val result = YouTubeVideoMapper.toDomainModel(videoItem)

        // Assert
        assertNotNull(result.liveStreamingDetails)
        val liveDetails = result.liveStreamingDetails
        assertNull(liveDetails.actualStartTime) // Invalid timestamp should be null
        assertEquals(Instant.parse("2023-12-25T10:00:00Z"), liveDetails.scheduledStartTime)
        assertNull(liveDetails.concurrentViewers) // Invalid number should be null
    }
}
