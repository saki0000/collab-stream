package org.example.project

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.example.project.auth.TwitchAuthProvider
import org.example.project.config.ApiKeyConfig
import org.example.project.plugins.configureCors
import org.example.project.plugins.configureSerialization
import org.example.project.plugins.configureStatusPages
import org.example.project.routes.commentRoutes
import org.example.project.routes.healthRoutes
import org.example.project.routes.searchRoutes
import org.example.project.routes.videoRoutes
import org.example.project.service.CommentServiceImpl
import org.example.project.service.SearchServiceImpl
import org.example.project.service.VideoServiceImpl

fun main() {
    // Cloud Run 対応: PORT 環境変数が設定されている場合はその値を使用し、
    // 未設定の場合はデフォルトの SERVER_PORT（8080）にフォールバックする
    val port = System.getenv("PORT")?.toIntOrNull() ?: SERVER_PORT
    println("サーバーをポート $port で起動します")
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // プラグイン設定
    configureSerialization()
    configureStatusPages()
    configureCors()

    // APIキー読み込み
    ApiKeyConfig.loadFromEnvironment(this)

    // HttpClient 生成（外部API呼び出し用）
    val httpClient = HttpClient(Java) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    // サービス初期化
    val twitchAuth = TwitchAuthProvider(httpClient)
    val videoService = VideoServiceImpl(httpClient, twitchAuth)
    val commentService = CommentServiceImpl(httpClient)
    val searchService = SearchServiceImpl(httpClient, twitchAuth)

    // ルーティング設定
    routing {
        // 既存のルート（後方互換性のため維持）
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        // ヘルスチェック
        healthRoutes()

        // 動画API
        videoRoutes(videoService)

        // コメントAPI
        commentRoutes(commentService)

        // 検索API
        searchRoutes(searchService)
    }

    // アプリケーション終了時にHttpClientをクローズ
    monitor.subscribe(ApplicationStopPreparing) {
        httpClient.close()
    }
}