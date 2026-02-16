package org.example.project.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.project.domain.model.ApiResponse
import org.example.project.service.CommentService

/**
 * コメント関連のルート設定
 *
 * エンドポイント:
 * - GET /api/videos/{id}/comments - 動画コメント取得
 *
 * Epic: コメントタイムスタンプ同期
 * US-1: Comment API Proxy
 */
fun Route.commentRoutes(commentService: CommentService) {
    route("/api/videos") {
        // GET /api/videos/{id}/comments?maxResults=100&pageToken=xxx&order=relevance
        get("/{id}/comments") {
            val videoId = call.parameters["id"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("Video ID is required", HttpStatusCode.BadRequest.value)
                )

            // クエリパラメータの取得とバリデーション
            val maxResults = call.request.queryParameters["maxResults"]?.let {
                it.toIntOrNull()?.coerceIn(1, 100) ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("maxResults must be between 1 and 100", HttpStatusCode.BadRequest.value)
                )
            } ?: 100

            val pageToken = call.request.queryParameters["pageToken"]

            val order = call.request.queryParameters["order"]?.lowercase() ?: "relevance"
            if (order !in listOf("relevance", "time")) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error("order must be 'relevance' or 'time'", HttpStatusCode.BadRequest.value)
                )
            }

            // コメント取得
            val commentsResponse = commentService.getYouTubeComments(
                videoId = videoId,
                maxResults = maxResults,
                pageToken = pageToken,
                order = order
            )

            call.respond(HttpStatusCode.OK, ApiResponse.Success(commentsResponse))
        }
    }
}
