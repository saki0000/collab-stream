package org.example.project.domain.usecase

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.example.project.domain.model.SearchEventType
import org.example.project.domain.model.SearchOrder
import org.example.project.domain.model.SearchQuery
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.VideoSearchRepository

class VideoSearchUseCase(
    private val videoSearchRepository: VideoSearchRepository,
) {

    @OptIn(ExperimentalTime::class)
    suspend fun searchVideos(
        query: String,
        maxResults: Int = 25,
        pageToken: String? = null,
        preferArchived: Boolean = true,
        publishedAfter: Instant? = null,
        publishedBefore: Instant? = null,
        order: SearchOrder = SearchOrder.VIEW_COUNT,
        channelId: String? = null,
        targetServices: Set<VideoServiceType> = setOf(VideoServiceType.YOUTUBE, VideoServiceType.TWITCH),
    ): Result<SearchResponse> {
        if (query.isBlank()) {
            return Result.failure(IllegalArgumentException("Search query cannot be empty"))
        }

        val eventType = if (preferArchived) {
            SearchEventType.COMPLETED
        } else {
            SearchEventType.ANY
        }

        val searchQuery = SearchQuery(
            query = query.trim(),
            maxResults = maxResults.coerceIn(1, 50),
            pageToken = pageToken,
            eventType = eventType,
            publishedAfter = publishedAfter,
            publishedBefore = publishedBefore,
            order = order,
            channelId = channelId,
            targetServices = targetServices,
        )

        return videoSearchRepository.searchVideos(searchQuery).map { response ->
            // Filter results by date range if specified
            // Twitch API doesn't support date filtering, so we filter client-side
            if (publishedAfter != null || publishedBefore != null) {
                val filteredResults = response.results.filter { result ->
                    val publishedAt = result.publishedAt
                    val afterCheck = publishedAfter?.let { publishedAt >= it } ?: true
                    val beforeCheck = publishedBefore?.let { publishedAt <= it } ?: true
                    afterCheck && beforeCheck
                }

                response.copy(
                    results = filteredResults,
                    totalResults = filteredResults.size,
                )
            } else {
                response
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun loadMoreResults(
        query: String,
        nextPageToken: String,
        maxResults: Int = 25,
        preferArchived: Boolean = true,
        publishedAfter: Instant? = null,
        publishedBefore: Instant? = null,
        order: SearchOrder = SearchOrder.VIEW_COUNT,
        channelId: String? = null,
        targetServices: Set<VideoServiceType> = setOf(VideoServiceType.YOUTUBE, VideoServiceType.TWITCH),
    ): Result<SearchResponse> {
        return searchVideos(
            query = query,
            maxResults = maxResults,
            pageToken = nextPageToken,
            preferArchived = preferArchived,
            publishedAfter = publishedAfter,
            publishedBefore = publishedBefore,
            order = order,
            channelId = channelId,
            targetServices = targetServices,
        )
    }
}
