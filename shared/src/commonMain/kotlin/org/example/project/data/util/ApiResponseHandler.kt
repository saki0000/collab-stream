package org.example.project.data.util

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import org.example.project.data.mapper.toVideoDetails
import org.example.project.domain.model.ApiResponse
import org.example.project.domain.model.TwitchVideoDetails
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.model.YouTubeVideoDetails

/**
 * ApiResponse のハンドリングユーティリティ
 *
 * サーバーAPIからの ApiResponse レスポンスをデシリアライズし、
 * 成功/失敗を Result 型に変換する。
 */
object ApiResponseHandler {

    /**
     * HTTP レスポンスを ApiResponse としてデシリアライズし、Result に変換する
     *
     * @param response HTTP レスポンス
     * @return 成功時は Result.success(data)、失敗時は Result.failure(exception)
     */
    suspend inline fun <reified T> handleResponse(response: HttpResponse): Result<T> {
        return try {
            if (response.status.isSuccess()) {
                // 2xx レスポンス: ApiResponse.Success としてデシリアライズ
                val apiResponse: ApiResponse.Success<T> = response.body()
                Result.success(apiResponse.data)
            } else {
                // 4xx / 5xx レスポンス: ApiResponse.Error としてデシリアライズ
                val errorResponse: ApiResponse.Error = response.body()
                Result.failure(
                    RuntimeException("API Error (${errorResponse.code}): ${errorResponse.message}"),
                )
            }
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to parse API response: ${e.message}", e),
            )
        }
    }

    /**
     * サービスタイプに応じた具象型でデシリアライズし、VideoDetails sealed classに変換する。
     *
     * サーバーは TwitchVideoDetails / YouTubeVideoDetails（単純data class）を返すため、
     * VideoDetails sealed classとして直接デシリアライズできない。
     * サービスタイプに応じて正しい具象型でデシリアライズ後、変換する。
     */
    suspend fun handleVideoDetailsResponse(
        response: HttpResponse,
        serviceType: VideoServiceType,
    ): Result<VideoDetails> {
        return try {
            if (response.status.isSuccess()) {
                when (serviceType) {
                    VideoServiceType.YOUTUBE -> {
                        val apiResponse: ApiResponse.Success<YouTubeVideoDetails> = response.body()
                        Result.success(apiResponse.data.toVideoDetails())
                    }
                    VideoServiceType.TWITCH -> {
                        val apiResponse: ApiResponse.Success<TwitchVideoDetails> = response.body()
                        Result.success(apiResponse.data.toVideoDetails())
                    }
                }
            } else {
                val errorResponse: ApiResponse.Error = response.body()
                Result.failure(
                    RuntimeException("API Error (${errorResponse.code}): ${errorResponse.message}"),
                )
            }
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to parse API response: ${e.message}", e),
            )
        }
    }

    /**
     * サービスタイプに応じた具象型リストでデシリアライズし、VideoDetailsリストに変換する。
     */
    suspend fun handleVideoDetailsListResponse(
        response: HttpResponse,
        serviceType: VideoServiceType,
    ): Result<List<VideoDetails>> {
        return try {
            if (response.status.isSuccess()) {
                when (serviceType) {
                    VideoServiceType.YOUTUBE -> {
                        val apiResponse: ApiResponse.Success<List<YouTubeVideoDetails>> = response.body()
                        Result.success(apiResponse.data.map { it.toVideoDetails() })
                    }
                    VideoServiceType.TWITCH -> {
                        val apiResponse: ApiResponse.Success<List<TwitchVideoDetails>> = response.body()
                        Result.success(apiResponse.data.map { it.toVideoDetails() })
                    }
                }
            } else {
                val errorResponse: ApiResponse.Error = response.body()
                Result.failure(
                    RuntimeException("API Error (${errorResponse.code}): ${errorResponse.message}"),
                )
            }
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to parse API response: ${e.message}", e),
            )
        }
    }
}
