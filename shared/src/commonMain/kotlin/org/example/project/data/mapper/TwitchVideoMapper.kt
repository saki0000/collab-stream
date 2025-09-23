@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.data.mapper

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.example.project.data.model.TwitchVideoItem
import org.example.project.domain.model.TwitchStreamInfo
import org.example.project.domain.model.TwitchVideoDetails
import org.example.project.domain.model.VideoSnippet

/**
 * Maps Twitch API DTOs to domain models.
 */
object TwitchVideoMapper {

    /**
     * Maps TwitchVideoItem (API DTO) to TwitchVideoDetails (domain model).
     */
    fun toDomainModel(item: TwitchVideoItem): TwitchVideoDetails {
        return TwitchVideoDetails(
            id = item.id,
            snippet = VideoSnippet(
                title = item.title,
                description = item.description,
                channelId = item.userId,
                channelTitle = item.userName,
            ),
            streamInfo = TwitchStreamInfo(
                streamId = item.streamId,
                createdAt = item.createdAt,
                publishedAt = item.publishedAt,
                type = item.type,
                duration = item.duration,
                viewable = item.viewable,
            ),
        )
    }

    /**
     * Parses Twitch's ISO 8601 timestamp to Instant.
     * @param timeStr ISO 8601 timestamp string from Twitch API
     * @return Parsed Instant or null if parsing fails
     */
    fun parseTimestamp(timeStr: String): Instant? {
        return try {
            Instant.parse(timeStr)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts video ID from Twitch video URL.
     * Supports various Twitch URL formats:
     * - https://www.twitch.tv/videos/123456789
     * - https://twitch.tv/videos/123456789
     * - videos/123456789
     * - 123456789
     */
    fun extractVideoId(url: String): String? {
        val videoIdRegex = """(?:twitch\.tv/videos/|videos/)?(\d+)""".toRegex()
        return videoIdRegex.find(url)?.groupValues?.get(1)
    }

    /**
     * Formats video ID for Twitch API requests.
     * Ensures the video ID is in the correct format for API calls.
     */
    fun formatVideoId(videoId: String): String {
        // Remove 'v' prefix if present and ensure it's numeric
        val cleanId = videoId.removePrefix("v")
        return if (cleanId.matches(Regex("\\d+"))) cleanId else videoId
    }
}