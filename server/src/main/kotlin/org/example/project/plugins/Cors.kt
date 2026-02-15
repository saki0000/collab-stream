package org.example.project.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

/**
 * CORSプラグインの設定
 *
 * クロスオリジンリソース共有（CORS）の設定を行う。
 * 開発モードでは全オリジンを許可し、本番では制限する。
 */
fun Application.configureCors() {
    val isDevelopment = developmentMode
    install(CORS) {
        // 開発モードでは全オリジンを許可、本番では制限
        if (isDevelopment) {
            anyHost()
        } else {
            // TODO: 本番用の許可オリジンを設定
            // host("your-production-domain.com")
        }

        // 許可するHTTPメソッド
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)

        // 許可するヘッダー
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }
}
