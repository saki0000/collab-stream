package org.example.project

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.project.config.ApiKeyConfig
import org.example.project.plugins.configureCors
import org.example.project.plugins.configureSerialization
import org.example.project.plugins.configureStatusPages
import org.example.project.routes.healthRoutes

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // プラグイン設定
    configureSerialization()
    configureStatusPages()
    configureCors()

    // APIキー読み込み
    ApiKeyConfig.loadFromEnvironment(this)

    // ルーティング設定
    routing {
        // 既存のルート（後方互換性のため維持）
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        // ヘルスチェック
        healthRoutes()
    }
}