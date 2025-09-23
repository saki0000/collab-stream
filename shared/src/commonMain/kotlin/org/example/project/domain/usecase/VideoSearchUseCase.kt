package org.example.project.domain.usecase

import org.example.project.domain.model.SearchEventType
import org.example.project.domain.model.SearchQuery
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.repository.VideoSearchRepository

class VideoSearchUseCase(
    private val videoSearchRepository: VideoSearchRepository,
) {

    suspend fun searchVideos(
        query: String,
        maxResults: Int = 25,
        pageToken: String? = null,
        preferArchived: Boolean = true,
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
        )

        return videoSearchRepository.searchVideos(searchQuery)
    }

    suspend fun loadMoreResults(
        query: String,
        nextPageToken: String,
        maxResults: Int = 25,
        preferArchived: Boolean = true,
    ): Result<SearchResponse> {
        return searchVideos(
            query = query,
            maxResults = maxResults,
            pageToken = nextPageToken,
            preferArchived = preferArchived,
        )
    }
}
