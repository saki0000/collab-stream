@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import org.example.project.data.model.TwitchVideoItem

class TwitchVideoMapperTest {

    @Test
    fun `should map complete Twitch video item to domain model`() {
        // Arrange
        val videoItem = TwitchVideoItem(
            id = "123456789",
            streamId = "987654321",
            userId = "user123",
            userLogin = "teststreamer",
            userName = "Test Streamer",
            title = "Amazing Game Stream",
            description = "Playing my favorite game live!",
            createdAt = "2023-12-25T10:00:00Z",
            publishedAt = "2023-12-25T10:05:00Z",
            url = "https://www.twitch.tv/videos/123456789",
            thumbnailUrl = "https://static-cdn.jtvnw.net/cf_vods/d2nvs31859zcd8/123456789/thumb/thumb0-%{width}x%{height}.jpg",
            viewable = "public",
            viewCount = 1500,
            language = "en",
            type = "archive",
            duration = "1h30m45s",
        )

        // Act
        val result = TwitchVideoMapper.toDomainModel(videoItem)

        // Assert
        assertEquals("123456789", result.id)
        assertEquals("Amazing Game Stream", result.snippet.title)
        assertEquals("Playing my favorite game live!", result.snippet.description)
        assertEquals("user123", result.snippet.channelId)
        assertEquals("Test Streamer", result.snippet.channelTitle)

        assertNotNull(result.streamInfo)
        assertEquals("987654321", result.streamInfo?.streamId)
        assertEquals("2023-12-25T10:00:00Z", result.streamInfo?.createdAt)
        assertEquals("2023-12-25T10:05:00Z", result.streamInfo?.publishedAt)
        assertEquals("archive", result.streamInfo?.type)
        assertEquals("1h30m45s", result.streamInfo?.duration)
        assertEquals("public", result.streamInfo?.viewable)
    }

    @Test
    fun `should handle missing stream ID gracefully`() {
        // Arrange
        val videoItem = TwitchVideoItem(
            id = "123456789",
            streamId = null,
            userId = "user123",
            userLogin = "teststreamer",
            userName = "Test Streamer",
            title = "Uploaded Video",
            description = "Previously recorded content",
            createdAt = "2023-12-25T10:00:00Z",
            publishedAt = "2023-12-25T10:00:00Z",
            url = "https://www.twitch.tv/videos/123456789",
            thumbnailUrl = "https://static-cdn.jtvnw.net/cf_vods/d2nvs31859zcd8/123456789/thumb/thumb0-%{width}x%{height}.jpg",
            viewable = "public",
            viewCount = 500,
            language = "en",
            type = "upload",
            duration = "45m30s",
        )

        // Act
        val result = TwitchVideoMapper.toDomainModel(videoItem)

        // Assert
        assertEquals("123456789", result.id)
        assertEquals("Uploaded Video", result.snippet.title)
        assertNotNull(result.streamInfo)
        assertNull(result.streamInfo?.streamId)
        assertEquals("upload", result.streamInfo?.type)
    }

    @Test
    fun `should parse valid timestamp successfully`() {
        // Arrange
        val validTimestamp = "2023-12-25T10:00:00Z"

        // Act
        val result = TwitchVideoMapper.parseTimestamp(validTimestamp)

        // Assert
        assertNotNull(result)
        assertEquals("2023-12-25T10:00:00Z", result.toString())
    }

    @Test
    fun `should handle invalid timestamp gracefully`() {
        // Arrange
        val invalidTimestamp = "invalid-timestamp"

        // Act
        val result = TwitchVideoMapper.parseTimestamp(invalidTimestamp)

        // Assert
        assertNull(result)
    }

    @Test
    fun `should extract video ID from various URL formats`() {
        // Test various Twitch URL formats
        val testCases = mapOf(
            "https://www.twitch.tv/videos/123456789" to "123456789",
            "https://twitch.tv/videos/123456789" to "123456789",
            "videos/123456789" to "123456789",
            "123456789" to "123456789",
            "www.twitch.tv/videos/987654321" to "987654321",
        )

        testCases.forEach { (input, expected) ->
            // Act
            val result = TwitchVideoMapper.extractVideoId(input)

            // Assert
            assertEquals(expected, result, "Failed for input: $input")
        }
    }

    @Test
    fun `should return null for invalid URL formats`() {
        // Arrange
        val invalidUrls = listOf(
            "https://youtube.com/watch?v=123",
            "invalid-url",
            "twitch.tv/channel/username",
            "",
        )

        invalidUrls.forEach { invalidUrl ->
            // Act
            val result = TwitchVideoMapper.extractVideoId(invalidUrl)

            // Assert
            assertNull(result, "Should return null for invalid URL: $invalidUrl")
        }
    }

    @Test
    fun `should format video ID correctly`() {
        // Test video ID formatting
        val testCases = mapOf(
            "123456789" to "123456789",
            "v123456789" to "123456789",
            "987654321" to "987654321",
            "v987654321" to "987654321",
        )

        testCases.forEach { (input, expected) ->
            // Act
            val result = TwitchVideoMapper.formatVideoId(input)

            // Assert
            assertEquals(expected, result, "Failed for input: $input")
        }
    }

    @Test
    fun `should handle non-numeric video IDs`() {
        // Arrange
        val nonNumericId = "abc123def"

        // Act
        val result = TwitchVideoMapper.formatVideoId(nonNumericId)

        // Assert
        assertEquals("abc123def", result) // Should return as-is for non-numeric IDs
    }
}
