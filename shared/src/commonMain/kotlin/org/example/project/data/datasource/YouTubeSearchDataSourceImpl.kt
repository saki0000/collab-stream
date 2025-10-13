package org.example.project.data.datasource

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.exampl.project.BuildKonfig
import org.example.project.data.model.YouTubeSearchResponse
import org.example.project.domain.model.SearchQuery
import kotlin.time.ExperimentalTime

class YouTubeSearchDataSourceImpl(
    private val httpClient: HttpClient,
) : YouTubeSearchDataSource {

    companion object {
        private const val YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3"
        private const val YOUTUBE_SEARCH_ENDPOINT = "$YOUTUBE_API_BASE_URL/search"
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun searchVideos(searchQuery: SearchQuery): Result<YouTubeSearchResponse> {
        return try {
            if (BuildKonfig.API_KEY.isBlank()) {
                return Result.failure(IllegalStateException("YouTube API key is not configured"))
            }

            val response = httpClient.get(YOUTUBE_SEARCH_ENDPOINT) {
                parameter("part", "snippet")
                parameter("q", searchQuery.query)
                parameter("type", "video")
                parameter("maxResults", searchQuery.maxResults)
                parameter("eventType", searchQuery.eventType.value)
                parameter("order", searchQuery.order.value)
                parameter("key", BuildKonfig.API_KEY)

                // Add optional parameters
                searchQuery.pageToken?.let { pageToken ->
                    parameter("pageToken", pageToken)
                }
                searchQuery.channelId?.let { channelId ->
                    parameter("channelId", channelId)
                }
                searchQuery.publishedAfter?.let { publishedAfter ->
                    parameter("publishedAfter", publishedAfter.toString())
                }
                searchQuery.publishedBefore?.let { publishedBefore ->
                    parameter("publishedBefore", publishedBefore.toString())
                }
            }

            val apiResponse: YouTubeSearchResponse = response.body()
            Result.success(apiResponse)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to search YouTube videos for query '${searchQuery.query}': ${e.message}", e),
            )
        }
    }
}
