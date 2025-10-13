package org.example.project.data.repository

import kotlin.time.ExperimentalTime
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.project.data.datasource.YouTubeSearchDataSource
import org.example.project.data.mapper.YouTubeSearchMapper
import org.example.project.domain.model.SearchOrder
import org.example.project.domain.model.SearchQuery
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.VideoSearchRepository

class VideoSearchRepositoryImpl(
    private val youTubeSearchDataSource: YouTubeSearchDataSource,
) : VideoSearchRepository {

    @OptIn(ExperimentalTime::class)
    override suspend fun searchVideos(searchQuery: SearchQuery): Result<SearchResponse> {
        return try {
            // Search across all target services in parallel
            val results = mutableListOf<SearchResult>()
            val serviceTokens = mutableMapOf<VideoServiceType, String?>()

            coroutineScope {
                val searchJobs = searchQuery.targetServices.map { service ->
                    async {
                        searchVideosByService(searchQuery, service)
                    }
                }

                searchJobs.awaitAll().forEach { result ->
                    result.onSuccess { response ->
                        results.addAll(response.results)
                        response.servicePageTokens.forEach { (service, token) ->
                            serviceTokens[service] = token
                        }
                    }
                }
            }

            // Sort results based on order preference
            val sortedResults = when (searchQuery.order) {
                SearchOrder.DATE -> results.sortedByDescending { it.publishedAt }
                else -> results // YouTube API already sorts by relevance/viewCount
            }

            Result.success(
                SearchResponse(
                    results = sortedResults,
                    totalResults = results.size,
                    servicePageTokens = serviceTokens,
                ),
            )
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to search videos: ${e.message}", e),
            )
        }
    }

    override suspend fun searchVideosByService(
        searchQuery: SearchQuery,
        serviceType: VideoServiceType,
    ): Result<SearchResponse> {
        return when (serviceType) {
            VideoServiceType.YOUTUBE -> {
                youTubeSearchDataSource.searchVideos(searchQuery)
                    .map { apiResponse ->
                        YouTubeSearchMapper.mapToSearchResponse(apiResponse).copy(
                            servicePageTokens = mapOf(VideoServiceType.YOUTUBE to apiResponse.nextPageToken),
                        )
                    }
            }
            VideoServiceType.TWITCH -> {
                // TODO: Implement Twitch search when TwitchSearchDataSource is ready
                Result.success(
                    SearchResponse(
                        results = emptyList(),
                        totalResults = 0,
                        servicePageTokens = mapOf(VideoServiceType.TWITCH to null),
                    ),
                )
            }
        }
    }
}
