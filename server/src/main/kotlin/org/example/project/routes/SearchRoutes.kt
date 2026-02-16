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
            val (query, serviceType) = call.parseSearchParams()
                ?: return@get // レスポンス済み

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
            val (query, serviceType) = call.parseSearchParams()
                ?: return@get // レスポンス済み

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

/**
 * 検索パラメータのパース結果
 */
private data class SearchParams(
    val query: String,
    val serviceType: VideoServiceType?,
)

/**
 * 検索共通パラメータ（q, service）をパース・バリデーションする。
 * バリデーションエラー時はレスポンスを送信し null を返す。
 */
private suspend fun ApplicationCall.parseSearchParams(): SearchParams? {
    val query = request.queryParameters["q"]
    if (query == null) {
        respond(
            HttpStatusCode.BadRequest,
            ApiResponse.Error("q query parameter is required", HttpStatusCode.BadRequest.value)
        )
        return null
    }

    if (query.isBlank()) {
        respond(
            HttpStatusCode.BadRequest,
            ApiResponse.Error("Search query must not be empty", HttpStatusCode.BadRequest.value)
        )
        return null
    }

    val serviceParam = request.queryParameters["service"]?.lowercase()
    val serviceType = when (serviceParam) {
        null -> null
        "youtube" -> VideoServiceType.YOUTUBE
        "twitch" -> VideoServiceType.TWITCH
        else -> {
            respond(
                HttpStatusCode.BadRequest,
                ApiResponse.Error("Invalid service type. Use 'youtube' or 'twitch'", HttpStatusCode.BadRequest.value)
            )
            return null
        }
    }

    return SearchParams(query, serviceType)
}
