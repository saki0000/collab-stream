package org.example.project.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.example.project.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

        // レスポンス内容確認
        val body = response.bodyAsText()
        assertTrue(body.contains("\"status\""))
        assertTrue(body.contains("\"ok\""))

        // JSON形式の検証
        val json = Json.parseToJsonElement(body)
        assertTrue(json.toString().contains("ok"))
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
