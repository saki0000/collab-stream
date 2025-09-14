package org.example.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.exampl.project.BuildKonfig
import org.example.project.data.mapper.YouTubeVideoMapper
import org.example.project.data.model.YouTubeApiResponse
import org.example.project.domain.model.YouTubeVideoDetails
import org.example.project.domain.repository.VideoSyncRepository

/**
 * Implementation of VideoSyncRepository using YouTube Data API v3.
 * Handles HTTP communication with YouTube API and data transformation.
 */
class VideoSyncRepositoryImpl(
    private val httpClient: HttpClient = createHttpClient(),
) : VideoSyncRepository {

    companion object {
        private const val YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3"
        private const val VIDEOS_ENDPOINT = "$YOUTUBE_API_BASE_URL/videos"

        /**
         * Creates a configured HTTP client for YouTube API communication.
         */
        fun createHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            coerceInputValues = true
                            isLenient = true
                        },
                    )
                }
            }
        }
    }

    override suspend fun getVideoDetails(videoId: String): Result<YouTubeVideoDetails> {
        return try {
            if (BuildKonfig.API_KEY.isBlank()) {
                return Result.failure(IllegalStateException("YouTube API key is not configured"))
            }

            val response = httpClient.get(VIDEOS_ENDPOINT) {
                parameter("part", "liveStreamingDetails,snippet")
                parameter("id", videoId)
                parameter("key", BuildKonfig.API_KEY)
            }

            val apiResponse: YouTubeApiResponse = response.body()

            if (apiResponse.items.isEmpty()) {
                return Result.failure(
                    NoSuchElementException("Video with ID '$videoId' not found"),
                )
            }

            val videoItem = apiResponse.items.first()
            val domainModel = YouTubeVideoMapper.toDomainModel(videoItem)

            Result.success(domainModel)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to fetch video details for video ID '$videoId': ${e.message}", e),
            )
        }
    }
}
