package org.example.project.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.example.project.domain.model.ApiResponse

/**
 * StatusPagesプラグインの設定
 *
 * サーバー全体のエラーハンドリングを統一する。
 * すべてのエラーは ApiResponse.Error 形式で返される。
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        // 404 Not Found
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse.Error(
                    message = cause.message ?: "Resource not found",
                    code = HttpStatusCode.NotFound.value
                )
            )
        }

        // 400 Bad Request
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.Error(
                    message = cause.message ?: "Invalid request",
                    code = HttpStatusCode.BadRequest.value
                )
            )
        }

        // 502 Bad Gateway (外部APIエラー)
        exception<ExternalApiException> { call, cause ->
            call.application.log.error("External API error", cause)
            call.respond(
                HttpStatusCode.BadGateway,
                ApiResponse.Error(
                    message = cause.message ?: "External API error",
                    code = HttpStatusCode.BadGateway.value
                )
            )
        }

        // 503 Service Unavailable (サービス利用不可)
        exception<ServiceUnavailableException> { call, cause ->
            call.application.log.warn("Service unavailable", cause)
            call.respond(
                HttpStatusCode.ServiceUnavailable,
                ApiResponse.Error(
                    message = cause.message ?: "Service unavailable",
                    code = HttpStatusCode.ServiceUnavailable.value
                )
            )
        }

        // 500 Internal Server Error (未処理の例外)
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.Error(
                    message = "Internal server error",
                    code = HttpStatusCode.InternalServerError.value
                )
            )
        }
    }
}

/**
 * カスタム例外: リソースが見つからない場合
 */
class NotFoundException(message: String) : Exception(message)

/**
 * カスタム例外: 外部API呼び出しエラー (502 Bad Gateway)
 */
class ExternalApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * カスタム例外: サービス利用不可 (503 Service Unavailable)
 */
class ServiceUnavailableException(message: String) : Exception(message)
