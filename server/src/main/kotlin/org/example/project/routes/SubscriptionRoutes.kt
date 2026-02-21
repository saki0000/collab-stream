package org.example.project.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.project.domain.model.ApiResponse
import org.example.project.service.SubscriptionService

/**
 * サブスクリプション検証関連のルート設定
 *
 * エンドポイント:
 * - GET /api/subscription/status?deviceId={deviceId} - サブスクリプション状態取得
 *
 * Epic: サブスクリプション検証
 * US-5: サーバーサイドサブスクリプション検証API
 */
fun Route.subscriptionRoutes(subscriptionService: SubscriptionService) {
    route("/api/subscription") {
        // GET /api/subscription/status?deviceId={deviceId}
        get("/status") {
            val deviceId = call.request.queryParameters["deviceId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error(
                        message = "deviceId query parameter is required",
                        code = HttpStatusCode.BadRequest.value
                    )
                )

            if (deviceId.isBlank()) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse.Error(
                        message = "deviceId must not be blank",
                        code = HttpStatusCode.BadRequest.value
                    )
                )
            }

            // サブスクリプション状態取得
            // 例外は StatusPages プラグインでハンドリングされる
            val subscriptionStatus = subscriptionService.getSubscriptionStatus(deviceId)

            call.respond(HttpStatusCode.OK, ApiResponse.Success(subscriptionStatus))
        }
    }
}
