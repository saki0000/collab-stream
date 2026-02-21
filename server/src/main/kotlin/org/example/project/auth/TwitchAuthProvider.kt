@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.config.ApiKeyConfig
import org.example.project.plugins.ServiceUnavailableException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * Twitch OAuth App Access Token の取得・キャッシュを担当するプロバイダー
 *
 * client_credentials フローで取得したトークンをメモリキャッシュし、
 * 有効期限前に自動リフレッシュする。
 */
class TwitchAuthProvider(private val httpClient: HttpClient) {

    private var cachedToken: String? = null
    private var expiresAt: Instant? = null
    private val mutex = Mutex()

    /**
     * 有効な App Access Token を返す。
     * キャッシュが無効な場合は自動的にリフレッシュする。
     * Mutex によるダブルチェックロッキングで競合状態を防止。
     */
    suspend fun getAccessToken(): String {
        if (isTokenValid()) {
            return cachedToken!!
        }
        return mutex.withLock {
            if (isTokenValid()) {
                cachedToken!!
            } else {
                refreshToken()
            }
        }
    }

    private fun isTokenValid(): Boolean {
        val token = cachedToken
        val expires = expiresAt
        return token != null && expires != null && Clock.System.now() < expires
    }

    private suspend fun refreshToken(): String {
        val clientId = ApiKeyConfig.twitchClientId
            ?: throw ServiceUnavailableException("Twitch Client ID is not configured")
        val clientSecret = ApiKeyConfig.twitchClientSecret
            ?: throw ServiceUnavailableException("Twitch Client Secret is not configured")

        try {
            val response: HttpResponse = httpClient.submitForm(
                url = "https://id.twitch.tv/oauth2/token",
                formParameters = parameters {
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    append("grant_type", "client_credentials")
                }
            )

            if (!response.status.isSuccess()) {
                throw ServiceUnavailableException(
                    "Twitch OAuth token request failed: ${response.status}"
                )
            }

            val tokenResponse: TwitchTokenResponse = response.body()

            cachedToken = tokenResponse.accessToken
            expiresAt = Clock.System.now() + tokenResponse.expiresIn.seconds - REFRESH_MARGIN

            return tokenResponse.accessToken
        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException -> throw e
                else -> throw ServiceUnavailableException(
                    "Failed to obtain Twitch access token: ${e.message}"
                )
            }
        }
    }

    private companion object {
        private val REFRESH_MARGIN = 60.seconds
    }
}

@Serializable
private data class TwitchTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("token_type") val tokenType: String,
)
