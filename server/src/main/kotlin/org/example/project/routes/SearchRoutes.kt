package org.example.project.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.project.domain.model.ApiResponse
import org.example.project.domain.model.VideoServiceType
import org.example.project.service.SearchService

/**
 * 検索関連のルート設定
 *
 * エンドポイント:
 * - GET /api/search/videos - 動画検索（q クエリパラメータ必須）
 * - GET /api/search/channels - チャンネル検索（q クエリパラメータ必須）
 */
fun Route.searchRoutes(searchService: SearchService) {
    route("/api/search") {
        // GET /api/search/videos?q=keyword&service=youtube|twitch&maxResults=25&pageToken=...&cursor=...&eventType=completed&order=viewCount
        get("/videos") {
            val query = call.request.queryParameters["q"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("q query parameter is required", HttpStatusCode.BadRequest.value)
                )

            if (query.isBlank()) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("Search query must not be empty", HttpStatusCode.BadRequest.value)
                )
            }

            val serviceParam = call.request.queryParameters["service"]?.lowercase()
            val serviceType = when {
                serviceParam == null -> null // 統合検索
                serviceParam == "youtube" -> VideoServiceType.YOUTUBE
                serviceParam == "twitch" -> VideoServiceType.TWITCH
                else -> return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("Invalid service type. Use 'youtube' or 'twitch'", HttpStatusCode.BadRequest.value)
                )
            }

            val maxResults = call.request.queryParameters["maxResults"]?.toIntOrNull() ?: 25
            val pageToken = call.request.queryParameters["pageToken"]
            val cursor = call.request.queryParameters["cursor"]
            val eventType = call.request.queryParameters["eventType"] ?: "completed"
            val order = call.request.queryParameters["order"] ?: "viewCount"

            val searchResponse = searchService.searchVideos(
                query = query,
                serviceType = serviceType,
                maxResults = maxResults,
                pageToken = pageToken,
                cursor = cursor,
                eventType = eventType,
                order = order,
            )

            call.respond(HttpStatusCode.OK, ApiResponse.Success(searchResponse))
        }

        // GET /api/search/channels?q=keyword&service=youtube|twitch&maxResults=25&pageToken=...&cursor=...
        get("/channels") {
            val query = call.request.queryParameters["q"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("q query parameter is required", HttpStatusCode.BadRequest.value)
                )

            if (query.isBlank()) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("Search query must not be empty", HttpStatusCode.BadRequest.value)
                )
            }

            val serviceParam = call.request.queryParameters["service"]?.lowercase()
            val serviceType = when {
                serviceParam == null -> null // 統合検索
                serviceParam == "youtube" -> VideoServiceType.YOUTUBE
                serviceParam == "twitch" -> VideoServiceType.TWITCH
                else -> return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("Invalid service type. Use 'youtube' or 'twitch'", HttpStatusCode.BadRequest.value)
                )
            }

            val maxResults = call.request.queryParameters["maxResults"]?.toIntOrNull() ?: 25
            val pageToken = call.request.queryParameters["pageToken"]
            val cursor = call.request.queryParameters["cursor"]

            val channelSearchResponse = searchService.searchChannels(
                query = query,
                serviceType = serviceType,
                maxResults = maxResults,
                pageToken = pageToken,
                cursor = cursor,
            )

            call.respond(HttpStatusCode.OK, ApiResponse.Success(channelSearchResponse))
        }
    }
}
