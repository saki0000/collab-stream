package org.example.project.plugins

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.example.project.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * StatusPagesプラグインのテスト
 * Specification: implement-context/api-proxy-server/us-1-server-foundation/SPECIFICATION.md
 */
class StatusPagesTest {

    // ========================================
    // IllegalArgumentException (400 Bad Request)
    // ========================================

    @Test
    fun `IllegalArgumentException_400エラーとApiResponse Errorを返すこと`() = testApplication {
        application {
            module()
            routing {
                get("/test-bad-request") {
                    throw IllegalArgumentException("Invalid parameter")
                }
            }
        }

        val response = client.get("/test-bad-request")

        // HTTPステータス確認
        assertEquals(HttpStatusCode.BadRequest, response.status)

        // レスポンス内容確認
        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject

        assertTrue(json.containsKey("message"))
        assertTrue(json.containsKey("code"))
        assertEquals(400, json["code"]?.jsonPrimitive?.content?.toInt())
        assertTrue(json["message"]?.jsonPrimitive?.content?.contains("Invalid parameter") ?: false)
    }

    // ========================================
    // NotFoundException (404 Not Found)
    // ========================================

    @Test
    fun `NotFoundException_404エラーとApiResponse Errorを返すこと`() = testApplication {
        application {
            module()
            routing {
                get("/test-not-found") {
                    throw NotFoundException("Resource not found")
                }
            }
        }

        val response = client.get("/test-not-found")

        // HTTPステータス確認
        assertEquals(HttpStatusCode.NotFound, response.status)

        // レスポンス内容確認
        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject

        assertTrue(json.containsKey("message"))
        assertTrue(json.containsKey("code"))
        assertEquals(404, json["code"]?.jsonPrimitive?.content?.toInt())
        assertTrue(json["message"]?.jsonPrimitive?.content?.contains("Resource not found") ?: false)
    }

    // ========================================
    // 未処理例外 (500 Internal Server Error)
    // ========================================

    @Test
    fun `未処理例外_500エラーとApiResponse Errorを返すこと`() = testApplication {
        application {
            module()
            routing {
                get("/test-internal-error") {
                    throw RuntimeException("Unexpected error")
                }
            }
        }

        val response = client.get("/test-internal-error")

        // HTTPステータス確認
        assertEquals(HttpStatusCode.InternalServerError, response.status)

        // レスポンス内容確認
        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject

        assertTrue(json.containsKey("message"))
        assertTrue(json.containsKey("code"))
        assertEquals(500, json["code"]?.jsonPrimitive?.content?.toInt())
        assertEquals("Internal server error", json["message"]?.jsonPrimitive?.content)
    }
}
