package org.example.project.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * ヘルスチェックエンドポイント
 *
 * サーバーの稼働状態を確認するための公開エンドポイント。
 * 認証不要で、シンプルなステータス情報を返す。
 */
fun Route.healthRoutes() {
    get("/health") {
        call.respond(
            HttpStatusCode.OK,
            HealthResponse(status = "ok")
        )
    }
}

/**
 * ヘルスチェックレスポンス
 *
 * @property status サーバーの稼働状態（"ok" = 正常稼働）
 */
@Serializable
data class HealthResponse(
    val status: String
)
