package org.example.project.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import org.example.project.domain.model.ApiResponse
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
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("Video ID is required", HttpStatusCode.BadRequest.value)
                )

            val service = call.request.queryParameters["service"]?.lowercase()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("service query parameter is required", HttpStatusCode.BadRequest.value)
                )

            when (service) {
                "youtube" -> {
                    val videoDetails = videoService.getYouTubeVideoDetails(videoId)
                    call.respond(HttpStatusCode.OK, ApiResponse.Success(videoDetails))
                }
                "twitch" -> {
                    val videoDetails = videoService.getTwitchVideoDetails(videoId)
                    call.respond(HttpStatusCode.OK, ApiResponse.Success(videoDetails))
                }
                else -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.Error("Invalid service type: $service", HttpStatusCode.BadRequest.value)
                    )
                }
            }
        }

        // GET /api/channels/{id}/videos?service=youtube|twitch&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
        get("/channels/{id}/videos") {
            val channelId = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("Channel ID is required", HttpStatusCode.BadRequest.value)
                )

            val service = call.request.queryParameters["service"]?.lowercase()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("service query parameter is required", HttpStatusCode.BadRequest.value)
                )

            val startDateStr = call.request.queryParameters["startDate"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("startDate query parameter is required", HttpStatusCode.BadRequest.value)
                )

            val endDateStr = call.request.queryParameters["endDate"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("endDate query parameter is required", HttpStatusCode.BadRequest.value)
                )

            // 日付パース
            val startDate = try {
                LocalDate.parse(startDateStr)
            } catch (e: Exception) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("Invalid startDate format. Expected YYYY-MM-DD", HttpStatusCode.BadRequest.value)
                )
            }

            val endDate = try {
                LocalDate.parse(endDateStr)
            } catch (e: Exception) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("Invalid endDate format. Expected YYYY-MM-DD", HttpStatusCode.BadRequest.value)
                )
            }

            when (service) {
                "youtube" -> {
                    val videos = videoService.getYouTubeChannelVideos(channelId, startDate, endDate)
                    call.respond(HttpStatusCode.OK, ApiResponse.Success(videos))
                }
                "twitch" -> {
                    val videos = videoService.getTwitchChannelVideos(channelId, startDate, endDate)
                    call.respond(HttpStatusCode.OK, ApiResponse.Success(videos))
                }
                else -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.Error("Invalid service type: $service", HttpStatusCode.BadRequest.value)
                    )
                }
            }
        }
    }
}
