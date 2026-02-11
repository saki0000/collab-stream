package org.example.project.data.datasource

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.time.ExperimentalTime
import org.exampl.project.BuildKonfig
import org.example.project.data.model.YouTubeChannelSearchResponse
import org.example.project.data.model.YouTubeSearchResponse
import org.example.project.domain.model.SearchQuery

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

    /**
     * YouTube チャンネルを検索する。
     * YouTube Data API v3 の /search エンドポイントを type=channel で使用。
     * クォータ消費が大きい（100 units/call）ため、maxResults を制限する。
     */
    override suspend fun searchChannels(query: String, maxResults: Int): Result<YouTubeChannelSearchResponse> {
        return try {
            if (BuildKonfig.API_KEY.isBlank()) {
                return Result.failure(IllegalStateException("YouTube API key is not configured"))
            }

            val response = httpClient.get(YOUTUBE_SEARCH_ENDPOINT) {
                parameter("part", "snippet")
                parameter("q", query)
                parameter("type", "channel")
                parameter("maxResults", maxResults.coerceIn(1, 10))
                parameter("key", BuildKonfig.API_KEY)
            }

            val apiResponse: YouTubeChannelSearchResponse = response.body()
            Result.success(apiResponse)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to search YouTube channels for query '$query': ${e.message}", e),
            )
        }
    }
}
