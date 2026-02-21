package org.example.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.config.ApiKeyConfig
import org.example.project.domain.model.SubscriptionStatus
import org.example.project.domain.model.SubscriptionTier
import org.example.project.plugins.ExternalApiException
import org.example.project.plugins.ServiceUnavailableException

/**
 * SubscriptionService の実装
 *
 * RevenueCat REST API v1 (`GET /v1/subscribers/{appUserId}`) を使用して
 * サブスクリプション状態を取得し、ドメインモデルに変換する。
 *
 * エンタイトルメント名: "pro"（RevenueCatダッシュボードの設定に依存）
 *
 * Epic: サブスクリプション検証
 * US-5: サーバーサイドサブスクリプション検証API
 */
class SubscriptionServiceImpl(
    private val httpClient: HttpClient
) : SubscriptionService {

    companion object {
        /** RevenueCat REST API v1 のベースURL */
        private const val REVENUECAT_API_BASE_URL = "https://api.revenuecat.com/v1"

        /** PRO プランに対応するエンタイトルメント名 */
        private const val PRO_ENTITLEMENT_ID = "pro"
    }

    // ========================================
    // サブスクリプション状態取得
    // ========================================

    override suspend fun getSubscriptionStatus(deviceId: String): SubscriptionStatus {
        // APIキーが設定されていない場合は503を返す
        val apiKey = ApiKeyConfig.revenueCatApiKey
            ?: throw ServiceUnavailableException("RevenueCat API key is not configured")

        try {
            val response: HttpResponse = httpClient.get(
                "$REVENUECAT_API_BASE_URL/subscribers/$deviceId"
            ) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
            }

            // 404 = RevenueCatに未登録ユーザー → FREEプランとして扱う
            if (response.status == HttpStatusCode.NotFound) {
                return createFreeSubscriptionStatus()
            }

            // その他のエラーは502 Bad Gatewayとして返す
            if (!response.status.isSuccess()) {
                throw ExternalApiException(
                    "RevenueCat API returned ${response.status}"
                )
            }

            val subscriberResponse: RevenueCatSubscriberResponse = response.body()

            return mapToSubscriptionStatus(subscriberResponse)

        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException,
                is ExternalApiException -> throw e
                else -> throw ExternalApiException("Failed to fetch subscription status from RevenueCat", e)
            }
        }
    }

    // ========================================
    // ドメインモデルへの変換
    // ========================================

    /**
     * RevenueCatのレスポンスをサブスクリプション状態に変換する。
     *
     * entitlement "pro" が is_active=true の場合はPROプラン、
     * それ以外の場合はFREEプランとして返す。
     */
    private fun mapToSubscriptionStatus(
        response: RevenueCatSubscriberResponse
    ): SubscriptionStatus {
        val proEntitlement = response.subscriber.entitlements[PRO_ENTITLEMENT_ID]

        // proエンタイトルメントが存在しない、またはis_activeがfalseの場合はFREE
        if (proEntitlement == null || !proEntitlement.isActive) {
            return createFreeSubscriptionStatus()
        }

        // expiresDateをエポックミリ秒に変換（nullの場合はnull）
        val expiresAtMillis = proEntitlement.expiresDate?.let { dateString ->
            try {
                // RevenueCatのdateはISO 8601形式（例: "2026-12-31T23:59:59Z"）
                @OptIn(ExperimentalTime::class)
                Instant.parse(dateString).toEpochMilliseconds()
            } catch (e: Exception) {
                null
            }
        }

        return SubscriptionStatus(
            tier = SubscriptionTier.PRO,
            isActive = proEntitlement.isActive,
            expiresAtMillis = expiresAtMillis,
            willRenew = proEntitlement.willRenew,
        )
    }

    /**
     * FREEプランのデフォルトサブスクリプション状態を返す。
     */
    private fun createFreeSubscriptionStatus(): SubscriptionStatus {
        return SubscriptionStatus(
            tier = SubscriptionTier.FREE,
            isActive = false,
            expiresAtMillis = null,
            willRenew = false,
        )
    }
}

// ========================================
// RevenueCat API レスポンスDTO
// ========================================

/**
 * RevenueCat REST API v1 の subscriber レスポンス。
 *
 * 例:
 * ```json
 * {
 *   "subscriber": {
 *     "entitlements": {
 *       "pro": {
 *         "expires_date": "2026-12-31T23:59:59Z",
 *         "is_active": true,
 *         "will_renew": true
 *       }
 *     }
 *   }
 * }
 * ```
 */
@Serializable
private data class RevenueCatSubscriberResponse(
    @SerialName("subscriber")
    val subscriber: RevenueCatSubscriber
)

@Serializable
private data class RevenueCatSubscriber(
    @SerialName("entitlements")
    val entitlements: Map<String, RevenueCatEntitlement> = emptyMap()
)

@Serializable
private data class RevenueCatEntitlement(
    @SerialName("is_active")
    val isActive: Boolean,

    @SerialName("will_renew")
    val willRenew: Boolean,

    @SerialName("expires_date")
    val expiresDate: String? = null,
)
