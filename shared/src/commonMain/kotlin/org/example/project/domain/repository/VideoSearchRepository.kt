package org.example.project.domain.repository

import org.example.project.domain.model.SearchQuery
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.VideoServiceType

interface VideoSearchRepository {
    suspend fun searchVideos(searchQuery: SearchQuery): Result<SearchResponse>

    suspend fun searchVideosByService(
        searchQuery: SearchQuery,
        serviceType: VideoServiceType,
    ): Result<SearchResponse>
}
