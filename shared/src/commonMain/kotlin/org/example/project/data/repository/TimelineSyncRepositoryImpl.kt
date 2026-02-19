package org.example.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate
import org.example.project.SERVER_BASE_URL
import org.example.project.data.util.ApiResponseHandler
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.TimelineSyncRepository

/**
 * Implementation of TimelineSyncRepository using server API proxy.
 *
 * サーバーAPI経由で動画詳細とチャンネル動画一覧を取得する実装。
 * ADR-005 Phase 2: APIキーをクライアントに含めない。
 *
 * Epic: Timeline Sync (EPIC-002)
 */
class TimelineSyncRepositoryImpl(
    private val httpClient: HttpClient,
) : TimelineSyncRepository {

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

    override suspend fun getChannelVideos(
        channelId: String,
        serviceType: VideoServiceType,
        dateRange: ClosedRange<LocalDate>,
    ): Result<List<VideoDetails>> {
        return try {
            val response = httpClient.get("$SERVER_BASE_URL/api/channels/$channelId/videos") {
                parameter("service", serviceType.name.lowercase())
                parameter("startDate", dateRange.start.toString())
                parameter("endDate", dateRange.endInclusive.toString())
            }

            ApiResponseHandler.handleResponse(response)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to fetch channel videos for channel '$channelId': ${e.message}", e),
            )
        }
    }
}
