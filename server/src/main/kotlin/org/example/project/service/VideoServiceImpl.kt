@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.config.ApiKeyConfig
import org.example.project.data.mapper.TwitchVideoMapper
import org.example.project.data.mapper.YouTubeVideoMapper
import org.example.project.data.model.TwitchApiResponse
import org.example.project.data.model.YouTubeApiResponse
import org.example.project.data.model.YouTubeSearchResponse
import org.example.project.domain.model.TwitchVideoDetails
import org.example.project.domain.model.YouTubeVideoDetails
import org.example.project.plugins.ExternalApiException
import org.example.project.plugins.NotFoundException
import org.example.project.plugins.ServiceUnavailableException
import kotlin.time.Duration.Companion.days

/**
 * VideoService の実装
 *
 * Ktor HttpClient を使用して外部APIを呼び出し、動画情報を取得する。
 */
class VideoServiceImpl(
    private val httpClient: HttpClient
) : VideoService {

    companion object {
        // YouTube API
        private const val YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3"
        private const val YOUTUBE_VIDEOS_ENDPOINT = "$YOUTUBE_API_BASE_URL/videos"
        private const val YOUTUBE_SEARCH_ENDPOINT = "$YOUTUBE_API_BASE_URL/search"
        private const val YOUTUBE_VIDEO_PART = "liveStreamingDetails,snippet"
        private const val YOUTUBE_SEARCH_MAX_RESULTS = "50"

        // Twitch API
        private const val TWITCH_API_BASE_URL = "https://api.twitch.tv/helix"
        private const val TWITCH_VIDEOS_ENDPOINT = "$TWITCH_API_BASE_URL/videos"
        private const val TWITCH_OAUTH_TOKEN_URL = "https://id.twitch.tv/oauth2/token"
        private const val TWITCH_CHANNEL_VIDEOS_FIRST = "100"
    }

    // Twitch OAuthアクセストークンのキャッシュ
    private var twitchAccessToken: String? = null

    // ========================================
    // YouTube API
    // ========================================

    override suspend fun getYouTubeVideoDetails(videoId: String): YouTubeVideoDetails {
        val apiKey = ApiKeyConfig.youtubeApiKey
            ?: throw ServiceUnavailableException("YouTube API key is not configured")

        return safeApiCall("Failed to fetch YouTube video details") {
            val response: HttpResponse = httpClient.get(YOUTUBE_VIDEOS_ENDPOINT) {
                parameter("part", YOUTUBE_VIDEO_PART)
                parameter("id", videoId)
                parameter("key", apiKey)
            }

            if (!response.status.isSuccess()) {
                throw ExternalApiException("YouTube API returned ${response.status}")
            }

            val apiResponse: YouTubeApiResponse = response.body()

            if (apiResponse.items.isEmpty()) {
                throw NotFoundException("YouTube video not found: $videoId")
            }

            YouTubeVideoMapper.toDomainModel(apiResponse.items.first())
        }
    }

    override suspend fun getYouTubeChannelVideos(
        channelId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<YouTubeVideoDetails> {
        val apiKey = ApiKeyConfig.youtubeApiKey
            ?: throw ServiceUnavailableException("YouTube API key is not configured")

        // 日付範囲のフォーマット（endDateはinclusiveにするため+1日）
        val publishedAfter = "${startDate}T00:00:00Z"
        val endDatePlusOne = endDate.plus(1, DateTimeUnit.DAY)
        val publishedBefore = "${endDatePlusOne}T00:00:00Z"

        return safeApiCall("Failed to fetch YouTube channel videos") {
            // Step 1: search.list で動画IDを取得
            val searchResponse: HttpResponse = httpClient.get(YOUTUBE_SEARCH_ENDPOINT) {
                parameter("part", "snippet")
                parameter("channelId", channelId)
                parameter("type", "video")
                parameter("eventType", "completed")
                parameter("order", "date")
                parameter("maxResults", YOUTUBE_SEARCH_MAX_RESULTS)
                parameter("publishedAfter", publishedAfter)
                parameter("publishedBefore", publishedBefore)
                parameter("key", apiKey)
            }

            if (!searchResponse.status.isSuccess()) {
                throw ExternalApiException("YouTube Search API returned ${searchResponse.status}")
            }

            val searchResult: YouTubeSearchResponse = searchResponse.body()

            if (searchResult.items.isEmpty()) {
                return@safeApiCall emptyList()
            }

            // Step 2: videos.list で詳細情報を取得
            val videoIds = searchResult.items.map { it.id.videoId }.joinToString(",")

            val videosResponse: HttpResponse = httpClient.get(YOUTUBE_VIDEOS_ENDPOINT) {
                parameter("part", YOUTUBE_VIDEO_PART)
                parameter("id", videoIds)
                parameter("key", apiKey)
            }

            if (!videosResponse.status.isSuccess()) {
                throw ExternalApiException("YouTube Videos API returned ${videosResponse.status}")
            }

            val videosResult: YouTubeApiResponse = videosResponse.body()

            videosResult.items.map { YouTubeVideoMapper.toDomainModel(it) }
        }
    }

    // ========================================
    // Twitch API
    // ========================================

    override suspend fun getTwitchVideoDetails(videoId: String): TwitchVideoDetails {
        val accessToken = getTwitchAccessToken()
        val clientId = ApiKeyConfig.twitchClientId
            ?: throw ServiceUnavailableException("Twitch Client ID is not configured")

        return safeApiCall("Failed to fetch Twitch video details") {
            val response: HttpResponse = httpClient.get(TWITCH_VIDEOS_ENDPOINT) {
                parameter("id", videoId)
                header("Client-ID", clientId)
                header("Authorization", "Bearer $accessToken")
            }

            if (!response.status.isSuccess()) {
                throw ExternalApiException("Twitch API returned ${response.status}")
            }

            val apiResponse: TwitchApiResponse = response.body()

            if (apiResponse.error != null) {
                throw ExternalApiException("Twitch API error: ${apiResponse.message}")
            }

            if (apiResponse.data.isEmpty()) {
                throw NotFoundException("Twitch video not found: $videoId")
            }

            TwitchVideoMapper.toDomainModel(apiResponse.data.first())
        }
    }

    override suspend fun getTwitchChannelVideos(
        channelId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TwitchVideoDetails> {
        val accessToken = getTwitchAccessToken()
        val clientId = ApiKeyConfig.twitchClientId
            ?: throw ServiceUnavailableException("Twitch Client ID is not configured")

        return safeApiCall("Failed to fetch Twitch channel videos") {
            val response: HttpResponse = httpClient.get(TWITCH_VIDEOS_ENDPOINT) {
                parameter("user_id", channelId)
                parameter("type", "archive")
                parameter("first", TWITCH_CHANNEL_VIDEOS_FIRST)
                header("Client-ID", clientId)
                header("Authorization", "Bearer $accessToken")
            }

            if (!response.status.isSuccess()) {
                throw ExternalApiException("Twitch API returned ${response.status}")
            }

            val apiResponse: TwitchApiResponse = response.body()

            if (apiResponse.error != null) {
                throw ExternalApiException("Twitch API error: ${apiResponse.message}")
            }

            // サーバー側で日付フィルタリング
            val startInstant = startDate.atStartOfDayIn(TimeZone.UTC)
            val endInstant = endDate.atStartOfDayIn(TimeZone.UTC) + 1.days

            val filteredVideos = apiResponse.data.filter { video ->
                val createdAt = TwitchVideoMapper.parseTimestamp(video.createdAt)
                createdAt != null && createdAt >= startInstant && createdAt < endInstant
            }

            filteredVideos.map { TwitchVideoMapper.toDomainModel(it) }
        }
    }

    // ========================================
    // Twitch OAuth
    // ========================================

    /**
     * Twitch OAuth Client Credentials フローでアクセストークンを取得する。
     * キャッシュ済みトークンがある場合はそれを返す。
     */
    private suspend fun getTwitchAccessToken(): String {
        twitchAccessToken?.let { return it }

        val clientId = ApiKeyConfig.twitchClientId
            ?: throw ServiceUnavailableException("Twitch Client ID is not configured")
        val clientSecret = ApiKeyConfig.twitchClientSecret
            ?: throw ServiceUnavailableException("Twitch Client Secret is not configured")

        try {
            val response: HttpResponse = httpClient.submitForm(
                url = TWITCH_OAUTH_TOKEN_URL,
                formParameters = parameters {
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    append("grant_type", "client_credentials")
                }
            )

            if (!response.status.isSuccess()) {
                throw ExternalApiException("Twitch OAuth token request failed: ${response.status}")
            }

            val tokenResponse: TwitchTokenResponse = response.body()
            twitchAccessToken = tokenResponse.accessToken
            return tokenResponse.accessToken
        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException, is ExternalApiException -> throw e
                else -> throw ExternalApiException("Failed to obtain Twitch access token", e)
            }
        }
    }

    // ========================================
    // ヘルパー
    // ========================================

    /**
     * 外部API呼び出しの共通例外ハンドリング
     */
    private suspend fun <T> safeApiCall(errorMessage: String, block: suspend () -> T): T {
        try {
            return block()
        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException, is NotFoundException, is ExternalApiException -> throw e
                else -> throw ExternalApiException(errorMessage, e)
            }
        }
    }
}

/**
 * Twitch OAuth トークンレスポンス
 */
@Serializable
private data class TwitchTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("token_type")
    val tokenType: String,
)
