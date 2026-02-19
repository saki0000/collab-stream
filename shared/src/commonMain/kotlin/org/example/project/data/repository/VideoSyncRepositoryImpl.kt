package org.example.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.project.SERVER_BASE_URL
import org.example.project.data.util.ApiResponseHandler
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.VideoSyncRepository

/**
 * Implementation of VideoSyncRepository using server API proxy.
 * Delegates video detail retrieval to the backend server.
 *
 * ADR-005 Phase 2: サーバーAPI経由でYouTube/Twitch APIを呼び出し、
 * クライアントにAPIキーを含めないセキュアな実装。
 */
class VideoSyncRepositoryImpl(
    private val httpClient: HttpClient = createHttpClient(),
) : VideoSyncRepository {

    companion object {
        /**
         * Creates a configured HTTP client for server API communication.
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
        return try {
            val response = httpClient.get("$SERVER_BASE_URL/api/videos/$videoId") {
                parameter("service", serviceType.name.lowercase())
            }

            ApiResponseHandler.handleResponse(response)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to fetch video details for video ID '$videoId': ${e.message}", e),
            )
        }
    }
}
