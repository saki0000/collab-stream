package org.example.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.exampl.project.BuildKonfig
import org.example.project.data.mapper.TwitchVideoMapper
import org.example.project.data.mapper.YouTubeVideoMapper
import org.example.project.data.model.TwitchApiResponse
import org.example.project.data.model.YouTubeApiResponse
import org.example.project.domain.model.TwitchVideoDetailsImpl
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.model.YouTubeVideoDetailsImpl
import org.example.project.domain.repository.VideoSyncRepository

/**
 * Implementation of VideoSyncRepository supporting both YouTube Data API v3 and Twitch Helix API.
 * Handles HTTP communication with video service APIs and data transformation.
 */
class VideoSyncRepositoryImpl(
    private val httpClient: HttpClient = createHttpClient(),
) : VideoSyncRepository {

    companion object {
        private const val YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3"
        private const val YOUTUBE_VIDEOS_ENDPOINT = "$YOUTUBE_API_BASE_URL/videos"

        private const val TWITCH_API_BASE_URL = "https://api.twitch.tv/helix"
        private const val TWITCH_VIDEOS_ENDPOINT = "$TWITCH_API_BASE_URL/videos"

        /**
         * Creates a configured HTTP client for video service API communication.
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


    override suspend fun getVideoDetails(videoId: String, serviceType: VideoServiceType): Result<VideoDetails> {
        return when (serviceType) {
            VideoServiceType.YOUTUBE -> getYouTubeVideoDetails(videoId)
            VideoServiceType.TWITCH -> getTwitchVideoDetails(videoId)
        }
    }

    private suspend fun getYouTubeVideoDetails(videoId: String): Result<VideoDetails> {
        return try {
            if (BuildKonfig.API_KEY.isBlank()) {
                return Result.failure(IllegalStateException("YouTube API key is not configured"))
            }

            val response = httpClient.get(YOUTUBE_VIDEOS_ENDPOINT) {
                parameter("part", "liveStreamingDetails,snippet")
                parameter("id", videoId)
                parameter("key", BuildKonfig.API_KEY)
            }

            val apiResponse: YouTubeApiResponse = response.body()

            if (apiResponse.items.isEmpty()) {
                return Result.failure(
                    NoSuchElementException("YouTube video with ID '$videoId' not found"),
                )
            }

            val videoItem = apiResponse.items.first()
            val domainModel = YouTubeVideoMapper.toDomainModel(videoItem)
            
            // Convert to unified VideoDetails
            val unifiedModel = YouTubeVideoDetailsImpl(
                id = domainModel.id,
                snippet = domainModel.snippet,
                liveStreamingDetails = domainModel.liveStreamingDetails
            )

            Result.success(unifiedModel)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to fetch YouTube video details for video ID '$videoId': ${e.message}", e),
            )
        }
    }

    private suspend fun getTwitchVideoDetails(videoId: String): Result<VideoDetails> {
        return try {

            if (BuildKonfig.TWITCH_CLIENT_ID.isBlank() || BuildKonfig.TWITCH_API_KEY.isBlank()) {
                return Result.failure(IllegalStateException("Twitch API credentials are not configured"))
            }

            val response = httpClient.get(TWITCH_VIDEOS_ENDPOINT) {
                parameter("id", videoId)
                header("Client-ID", BuildKonfig.TWITCH_CLIENT_ID)
                header("Authorization", "Bearer ${BuildKonfig.TWITCH_API_KEY}")
            }

            // Check HTTP status first
            if (response.status.value !in 200..299) {
                return Result.failure(
                    RuntimeException("Twitch API returned HTTP ${response.status.value}: ${response.status.description}")
                )
            }

            val apiResponse: TwitchApiResponse = response.body()

            // Check for API error response
            if (!apiResponse.error.isNullOrBlank()) {
                return Result.failure(
                    RuntimeException("Twitch API error: ${apiResponse.error} - ${apiResponse.message ?: "Unknown error"}")
                )
            }

            if (apiResponse.data.isEmpty()) {
                return Result.failure(
                    NoSuchElementException("Twitch video with ID '$videoId' not found"),
                )
            }

            val videoItem = apiResponse.data.first()
            val domainModel = TwitchVideoMapper.toDomainModel(videoItem)
            
            // Convert to unified VideoDetails
            val unifiedModel = TwitchVideoDetailsImpl(
                id = domainModel.id,
                snippet = domainModel.snippet,
                streamInfo = domainModel.streamInfo
            )

            Result.success(unifiedModel)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to fetch Twitch video details for video ID '$videoId': ${e.message}", e),
            )
        }
    }
}
