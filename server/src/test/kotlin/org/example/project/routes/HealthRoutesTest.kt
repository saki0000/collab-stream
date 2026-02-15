package org.example.project.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.example.project.module
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * ヘルスチェックエンドポイントのテスト
 * Specification: implement-context/api-proxy-server/us-1-server-foundation/SPECIFICATION.md
 */
class HealthRoutesTest {

    // ========================================
    // GET /health
    // ========================================

    @Test
    fun `GET health_ステータス200とok応答を返すこと`() = testApplication {
        application {
            module()
        }

        val response = client.get("/health")

        // HTTPステータス確認
        assertEquals(HttpStatusCode.OK, response.status)

        // 型安全なデシリアライズで検証
        val healthResponse = Json.decodeFromString<HealthResponse>(response.bodyAsText())
        assertEquals("ok", healthResponse.status)
    }

    @Test
    fun `GET health_Content-TypeがJSONであること`() = testApplication {
        application {
            module()
        }

        val response = client.get("/health")

        // Content-Type確認
        assertEquals(ContentType.Application.Json, response.contentType()?.withoutParameters())
    }
}
