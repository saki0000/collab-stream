package org.example.project.data.repository

import org.example.project.data.datasource.YouTubeSearchDataSource
import org.example.project.data.mapper.YouTubeSearchMapper
import org.example.project.domain.model.SearchQuery
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.repository.VideoSearchRepository

class VideoSearchRepositoryImpl(
    private val youTubeSearchDataSource: YouTubeSearchDataSource,
) : VideoSearchRepository {

    override suspend fun searchVideos(searchQuery: SearchQuery): Result<SearchResponse> {
        return try {
            val apiResult = youTubeSearchDataSource.searchVideos(searchQuery)

            apiResult.fold(
                onSuccess = { apiResponse ->
                    val domainResponse = YouTubeSearchMapper.mapToSearchResponse(apiResponse)
                    Result.success(domainResponse)
                },
                onFailure = { error ->
                    Result.failure(error)
                },
            )
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to search videos: ${e.message}", e),
            )
        }
    }
}
