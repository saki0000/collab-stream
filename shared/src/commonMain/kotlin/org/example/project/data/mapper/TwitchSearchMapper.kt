package org.example.project.data.mapper

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.example.project.data.model.TwitchSearchResponse
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType

object TwitchSearchMapper {

    @OptIn(ExperimentalTime::class)
    fun mapToSearchResponse(apiResponse: TwitchSearchResponse): SearchResponse {
        val results = apiResponse.data.map { item ->
            SearchResult(
                videoId = item.id,
                title = item.title,
                description = item.description,
                thumbnailUrl = formatThumbnailUrl(item.thumbnailUrl),
                channelTitle = item.userName,
                publishedAt = parseInstant(item.publishedAt),
                isLiveBroadcast = item.type == "live", // VODs are typically not live
                serviceType = VideoServiceType.TWITCH,
            )
        }

        return SearchResponse(
            results = results,
            nextPageToken = apiResponse.pagination?.cursor,
            totalResults = results.size, // Twitch doesn't provide total count
        )
    }

    /**
     * Formats Twitch thumbnail URL by replacing template parameters.
     * Twitch thumbnail URLs come as templates like: "https://example.com/%{width}x%{height}.jpg"
     */
    private fun formatThumbnailUrl(templateUrl: String): String {
        return templateUrl
            .replace("%{width}", "320")
            .replace("%{height}", "180")
    }

    @OptIn(ExperimentalTime::class)
    private fun parseInstant(dateString: String): Instant {
        return try {
            Instant.parse(dateString)
        } catch (e: Exception) {
            Instant.DISTANT_PAST
        }
    }
}
