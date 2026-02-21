package org.example.project.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.example.project.domain.model.SubscriptionStatus
import org.example.project.domain.model.SubscriptionTier
import org.example.project.plugins.ExternalApiException
import org.example.project.plugins.ServiceUnavailableException
import org.example.project.plugins.configureSerialization
import org.example.project.plugins.configureStatusPages
import org.example.project.service.SubscriptionService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * SubscriptionRoutes のテスト
 *
 * Specification: US-5（サーバーサイドサブスクリプション検証API）
 */
class SubscriptionRoutesTest {

    // ========================================
    // GET /api/subscription/status - 正常系
    // ========================================

    @Test
    fun `サブスクリプション状態取得_PROプランユーザーで200 OKとPROステータスを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                subscriptionRoutes(createMockSubscriptionService(
                    deviceId = "test-device-id",
                    status = SubscriptionStatus(
                        tier = SubscriptionTier.PRO,
                        isActive = true,
                        expiresAtMillis = 1767225599000L,
                        willRenew = true,
                    )
                ))
            }
        }

        val response = client.get("/api/subscription/status?deviceId=test-device-id")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("PRO"), "レスポンスにPROが含まれること")
        assertTrue(body.contains("true"), "isActiveがtrueであること")
    }

    @Test
    fun `サブスクリプション状態取得_FREEプランユーザーで200 OKとFREEステータスを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                subscriptionRoutes(createMockSubscriptionService(
                    deviceId = "free-user-device-id",
                    status = SubscriptionStatus(
                        tier = SubscriptionTier.FREE,
                        isActive = false,
                        expiresAtMillis = null,
                        willRenew = false,
                    )
                ))
            }
        }

        val response = client.get("/api/subscription/status?deviceId=free-user-device-id")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("FREE"), "レスポンスにFREEが含まれること")
        assertTrue(body.contains("false"), "isActiveがfalseであること")
    }

    @Test
    fun `サブスクリプション状態取得_未登録ユーザーでFREEプランを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                subscriptionRoutes(createMockSubscriptionService(
                    deviceId = "unregistered-device-id",
                    status = SubscriptionStatus(
                        tier = SubscriptionTier.FREE,
                        isActive = false,
                        expiresAtMillis = null,
                        willRenew = false,
                    )
                ))
            }
        }

        val response = client.get("/api/subscription/status?deviceId=unregistered-device-id")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("FREE"))
    }

    // ========================================
    // GET /api/subscription/status - バリデーションエラー
    // ========================================

    @Test
    fun `deviceIdパラメータ未指定で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                subscriptionRoutes(createMockSubscriptionService())
            }
        }

        val response = client.get("/api/subscription/status")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(
            response.bodyAsText().contains("deviceId query parameter is required"),
            "エラーメッセージにdeviceIdの説明が含まれること"
        )
    }

    @Test
    fun `deviceIdパラメータが空文字で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                subscriptionRoutes(createMockSubscriptionService())
            }
        }

        val response = client.get("/api/subscription/status?deviceId=")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(
            response.bodyAsText().contains("deviceId must not be blank"),
            "エラーメッセージにdeviceIdの説明が含まれること"
        )
    }

    // ========================================
    // GET /api/subscription/status - サーバーエラー
    // ========================================

    @Test
    fun `RevenueCat APIキー未設定で503 Service Unavailableを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                subscriptionRoutes(createMockServiceUnavailableService())
            }
        }

        val response = client.get("/api/subscription/status?deviceId=test-device-id")

        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
    }

    @Test
    fun `RevenueCat API呼び出し失敗で502 Bad Gatewayを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                subscriptionRoutes(createMockExternalApiErrorService())
            }
        }

        val response = client.get("/api/subscription/status?deviceId=test-device-id")

        assertEquals(HttpStatusCode.BadGateway, response.status)
    }

    // ========================================
    // モックSubscriptionService
    // ========================================

    /**
     * 正常系のモックサービスを生成する。
     *
     * @param deviceId 期待するdeviceId（省略時はどのdeviceIdでも同じレスポンスを返す）
     * @param status 返すサブスクリプション状態
     */
    private fun createMockSubscriptionService(
        deviceId: String? = null,
        status: SubscriptionStatus = SubscriptionStatus(
            tier = SubscriptionTier.FREE,
            isActive = false,
        )
    ): SubscriptionService {
        return object : SubscriptionService {
            override suspend fun getSubscriptionStatus(deviceId: String): SubscriptionStatus {
                return status
            }
        }
    }

    /**
     * APIキー未設定で503を返すモックサービスを生成する。
     */
    private fun createMockServiceUnavailableService(): SubscriptionService {
        return object : SubscriptionService {
            override suspend fun getSubscriptionStatus(deviceId: String): SubscriptionStatus {
                throw ServiceUnavailableException("RevenueCat API key is not configured")
            }
        }
    }

    /**
     * 外部API障害で502を返すモックサービスを生成する。
     */
    private fun createMockExternalApiErrorService(): SubscriptionService {
        return object : SubscriptionService {
            override suspend fun getSubscriptionStatus(deviceId: String): SubscriptionStatus {
                throw ExternalApiException("RevenueCat API returned 500")
            }
        }
    }
}
