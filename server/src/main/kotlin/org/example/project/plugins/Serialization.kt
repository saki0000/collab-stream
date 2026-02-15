package org.example.project.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

/**
 * ContentNegotiationプラグインの設定
 *
 * JSON シリアライゼーション設定を行う。
 * 本番環境向けに prettyPrint=false、isLenient=true、ignoreUnknownKeys=true を設定。
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}
