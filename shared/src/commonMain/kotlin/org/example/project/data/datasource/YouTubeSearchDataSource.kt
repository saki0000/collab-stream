package org.example.project.data.datasource

import org.example.project.data.model.YouTubeSearchResponse
import org.example.project.domain.model.SearchQuery

interface YouTubeSearchDataSource {
    suspend fun searchVideos(searchQuery: SearchQuery): Result<YouTubeSearchResponse>
}
