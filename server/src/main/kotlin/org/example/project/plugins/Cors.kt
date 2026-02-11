package org.example.project.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

/**
 * CORSプラグインの設定
 *
 * クロスオリジンリソース共有（CORS）の設定を行う。
 * 開発段階では全オリジンを許可し、後続USで制限可能とする。
 */
fun Application.configureCors() {
    install(CORS) {
        // 全オリジンを許可（開発段階）
        anyHost()

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
