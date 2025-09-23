package org.example.project.domain.repository

import org.example.project.domain.model.SearchQuery
import org.example.project.domain.model.SearchResponse

interface VideoSearchRepository {
    suspend fun searchVideos(searchQuery: SearchQuery): Result<SearchResponse>
}
