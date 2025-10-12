package org.example.project.data.mapper

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.example.project.data.model.YouTubeSearchResponse
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.SearchResult

object YouTubeSearchMapper {

    @OptIn(ExperimentalTime::class)
    fun mapToSearchResponse(apiResponse: YouTubeSearchResponse): SearchResponse {
        val results = apiResponse.items.map { item ->
            SearchResult(
                videoId = item.id.videoId,
                title = item.snippet.title,
                description = item.snippet.description,
                thumbnailUrl = item.snippet.thumbnails.medium?.url
                    ?: item.snippet.thumbnails.high?.url
                    ?: item.snippet.thumbnails.default?.url
                    ?: "",
                channelTitle = item.snippet.channelTitle,
                publishedAt = parseInstant(item.snippet.publishedAt),
                isLiveBroadcast = item.snippet.liveBroadcastContent != "none",
            )
        }

        return SearchResponse(
            results = results,
            nextPageToken = apiResponse.nextPageToken,
            totalResults = apiResponse.pageInfo.totalResults,
        )
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
