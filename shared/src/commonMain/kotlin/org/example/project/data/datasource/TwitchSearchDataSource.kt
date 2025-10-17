package org.example.project.data.datasource

import org.example.project.data.model.TwitchSearchResponse
import org.example.project.data.model.TwitchUserResponse
import org.example.project.domain.model.SearchQuery

interface TwitchSearchDataSource {
    suspend fun searchVideos(searchQuery: SearchQuery): Result<TwitchSearchResponse>
    suspend fun searchChannels(query: String, maxResults: Int = 5): Result<TwitchUserResponse>
}
