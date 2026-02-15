package org.example.project.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import org.example.project.domain.model.ApiResponse
import org.example.project.domain.model.VideoServiceType
import org.example.project.service.VideoService

/**
 * 動画関連のルート設定
 *
 * エンドポイント:
 * - GET /api/videos/{id} - 動画詳細取得（service クエリパラメータ必須）
 * - GET /api/channels/{id}/videos - チャンネル動画一覧取得（service, startDate, endDate 必須）
 */
fun Route.videoRoutes(videoService: VideoService) {
    route("/api") {
        // GET /api/videos/{id}?service=youtube|twitch
        get("/videos/{id}") {
            val videoId = call.parameters["id"]
                ?: return@get call.respondBadRequest("Video ID is required")

            val serviceParam = call.request.queryParameters["service"]
                ?: return@get call.respondBadRequest("service query parameter is required")

            val serviceType = parseServiceType(serviceParam)
                ?: return@get call.respondBadRequest("Invalid service type: $serviceParam")

            when (serviceType) {
                VideoServiceType.YOUTUBE -> {
                    val videoDetails = videoService.getYouTubeVideoDetails(videoId)
                    call.respond(HttpStatusCode.OK, ApiResponse.Success(videoDetails))
                }
                VideoServiceType.TWITCH -> {
                    val videoDetails = videoService.getTwitchVideoDetails(videoId)
                    call.respond(HttpStatusCode.OK, ApiResponse.Success(videoDetails))
                }
            }
        }

        // GET /api/channels/{id}/videos?service=youtube|twitch&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
        get("/channels/{id}/videos") {
            val channelId = call.parameters["id"]
                ?: return@get call.respondBadRequest("Channel ID is required")

            val serviceParam = call.request.queryParameters["service"]
                ?: return@get call.respondBadRequest("service query parameter is required")

            val serviceType = parseServiceType(serviceParam)
                ?: return@get call.respondBadRequest("Invalid service type: $serviceParam")

            val startDateStr = call.request.queryParameters["startDate"]
                ?: return@get call.respondBadRequest("startDate query parameter is required")

            val endDateStr = call.request.queryParameters["endDate"]
                ?: return@get call.respondBadRequest("endDate query parameter is required")

            // 日付パース
            val startDate = try {
                LocalDate.parse(startDateStr)
            } catch (e: Exception) {
                return@get call.respondBadRequest("Invalid startDate format. Expected YYYY-MM-DD")
            }

            val endDate = try {
                LocalDate.parse(endDateStr)
            } catch (e: Exception) {
                return@get call.respondBadRequest("Invalid endDate format. Expected YYYY-MM-DD")
            }

            when (serviceType) {
                VideoServiceType.YOUTUBE -> {
                    val videos = videoService.getYouTubeChannelVideos(channelId, startDate, endDate)
                    call.respond(HttpStatusCode.OK, ApiResponse.Success(videos))
                }
                VideoServiceType.TWITCH -> {
                    val videos = videoService.getTwitchChannelVideos(channelId, startDate, endDate)
                    call.respond(HttpStatusCode.OK, ApiResponse.Success(videos))
                }
            }
        }
    }
}

/**
 * サービスタイプ文字列を VideoServiceType に変換する。
 * 不正な値の場合は null を返す。
 */
private fun parseServiceType(value: String): VideoServiceType? {
    return when (value.lowercase()) {
        "youtube" -> VideoServiceType.YOUTUBE
        "twitch" -> VideoServiceType.TWITCH
        else -> null
    }
}

/**
 * 400 Bad Request レスポンスを返すヘルパー
 */
private suspend fun ApplicationCall.respondBadRequest(message: String) {
    respond(
        HttpStatusCode.BadRequest,
        ApiResponse.Error(message, HttpStatusCode.BadRequest.value)
    )
}
