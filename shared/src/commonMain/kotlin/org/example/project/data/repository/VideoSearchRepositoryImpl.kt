package org.example.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.project.SERVER_BASE_URL
import org.example.project.data.util.ApiResponseHandler
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SearchOrder
import org.example.project.domain.model.SearchQuery
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.VideoSearchRepository

/**
 * Implementation of VideoSearchRepository using server API proxy.
 *
 * サーバーAPI経由で動画検索とチャンネル検索を実行する実装。
 * ADR-005 Phase 2: APIキーをクライアントに含めない。
 */
@OptIn(ExperimentalTime::class)
class VideoSearchRepositoryImpl(
    private val httpClient: HttpClient,
) : VideoSearchRepository {

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
                else -> results // Server API already sorts by relevance/viewCount
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
        return try {
            val response = httpClient.get("$SERVER_BASE_URL/api/search/videos") {
                parameter("q", searchQuery.query)
                parameter("service", serviceType.name.lowercase())
                parameter("maxResults", searchQuery.maxResults)
                searchQuery.pageToken?.let { parameter("pageToken", it) }
                searchQuery.channelId?.let { parameter("channelId", it) }
                parameter("eventType", searchQuery.eventType.value)
                parameter("order", searchQuery.order.value)
            }

            ApiResponseHandler.handleResponse(response)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to search videos for service $serviceType: ${e.message}", e),
            )
        }
    }

    override suspend fun searchChannels(
        query: String,
        serviceType: VideoServiceType,
        maxResults: Int,
    ): Result<List<ChannelInfo>> {
        if (query.isBlank()) {
            return Result.failure(IllegalArgumentException("Search query cannot be empty"))
        }

        return try {
            val response = httpClient.get("$SERVER_BASE_URL/api/search/channels") {
                parameter("q", query.trim())
                parameter("service", serviceType.name.lowercase())
                parameter("maxResults", maxResults.coerceIn(1, 20))
            }

            ApiResponseHandler.handleResponse(response)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to search channels for service $serviceType: ${e.message}", e),
            )
        }
    }
}
