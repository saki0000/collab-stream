@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.data.mapper

import kotlin.time.Instant
import org.example.project.data.model.YouTubeVideoItem
import org.example.project.domain.model.LiveStreamingDetails
import org.example.project.domain.model.VideoSnippet
import org.example.project.domain.model.YouTubeVideoDetails

/**
 * Maps YouTube API DTOs to domain models.
 */
object YouTubeVideoMapper {

    /**
     * Maps YouTubeVideoItem (API DTO) to YouTubeVideoDetails (domain model).
     */
    fun toDomainModel(item: YouTubeVideoItem): YouTubeVideoDetails {
        return YouTubeVideoDetails(
            id = item.id,
            snippet = item.snippet?.let { snippetDto ->
                VideoSnippet(
                    title = snippetDto.title,
                    description = snippetDto.description,
                    channelId = snippetDto.channelId,
                    channelTitle = snippetDto.channelTitle,
                )
            } ?: VideoSnippet(
                title = "Unknown Title",
                description = "",
                channelId = "",
                channelTitle = "",
            ),
            liveStreamingDetails = item.liveStreamingDetails?.let { liveDetailsDto ->
                LiveStreamingDetails(
                    actualStartTime = liveDetailsDto.actualStartTime?.let { timeStr ->
                        try {
                            Instant.parse(timeStr)
                        } catch (e: Exception) {
                            null
                        }
                    },
                    scheduledStartTime = liveDetailsDto.scheduledStartTime?.let { timeStr ->
                        try {
                            Instant.parse(timeStr)
                        } catch (e: Exception) {
                            null
                        }
                    },
                    actualEndTime = liveDetailsDto.actualEndTime?.let { timeStr ->
                        try {
                            Instant.parse(timeStr)
                        } catch (e: Exception) {
                            null
                        }
                    },
                )
            },
        )
    }
}
