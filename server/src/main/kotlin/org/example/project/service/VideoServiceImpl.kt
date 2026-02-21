@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import org.example.project.auth.TwitchAuthProvider
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
    private val httpClient: HttpClient,
    private val twitchAuth: TwitchAuthProvider,
) : VideoService {

    // ========================================
    // YouTube API
    // ========================================

    override suspend fun getYouTubeVideoDetails(videoId: String): YouTubeVideoDetails {
        val apiKey = ApiKeyConfig.youtubeApiKey
            ?: throw ServiceUnavailableException("YouTube API key is not configured")

        try {
            val response: HttpResponse = httpClient.get("https://www.googleapis.com/youtube/v3/videos") {
                parameter("part", "liveStreamingDetails,snippet")
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

            return YouTubeVideoMapper.toDomainModel(apiResponse.items.first())
        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException, is NotFoundException, is ExternalApiException -> throw e
                else -> throw ExternalApiException("Failed to fetch YouTube video details", e)
            }
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

        try {
            // Step 1: search.list で動画IDを取得
            val searchResponse: HttpResponse = httpClient.get("https://www.googleapis.com/youtube/v3/search") {
                parameter("part", "snippet")
                parameter("channelId", channelId)
                parameter("type", "video")
                parameter("eventType", "completed")
                parameter("order", "date")
                parameter("maxResults", "50")
                parameter("publishedAfter", publishedAfter)
                parameter("publishedBefore", publishedBefore)
                parameter("key", apiKey)
            }

            if (!searchResponse.status.isSuccess()) {
                throw ExternalApiException("YouTube Search API returned ${searchResponse.status}")
            }

            val searchResult: YouTubeSearchResponse = searchResponse.body()

            if (searchResult.items.isEmpty()) {
                return emptyList()
            }

            // Step 2: videos.list で詳細情報を取得
            val videoIds = searchResult.items.map { it.id.videoId }.joinToString(",")

            val videosResponse: HttpResponse = httpClient.get("https://www.googleapis.com/youtube/v3/videos") {
                parameter("part", "liveStreamingDetails,snippet")
                parameter("id", videoIds)
                parameter("key", apiKey)
            }

            if (!videosResponse.status.isSuccess()) {
                throw ExternalApiException("YouTube Videos API returned ${videosResponse.status}")
            }

            val videosResult: YouTubeApiResponse = videosResponse.body()

            return videosResult.items.map { YouTubeVideoMapper.toDomainModel(it) }
        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException, is ExternalApiException -> throw e
                else -> throw ExternalApiException("Failed to fetch YouTube channel videos", e)
            }
        }
    }

    // ========================================
    // Twitch API
    // ========================================

    override suspend fun getTwitchVideoDetails(videoId: String): TwitchVideoDetails {
        val clientId = ApiKeyConfig.twitchClientId
            ?: throw ServiceUnavailableException("Twitch Client ID is not configured")

        try {
            val accessToken = twitchAuth.getAccessToken()

            val response: HttpResponse = httpClient.get("https://api.twitch.tv/helix/videos") {
                parameter("id", videoId)
                header("Client-ID", clientId)
                header("Authorization", "Bearer $accessToken")
            }

            if (!response.status.isSuccess()) {
                throw ExternalApiException("Twitch API returned ${response.status}")
            }

            val apiResponse: TwitchApiResponse = response.body()

            // エラーレスポンスのチェック
            if (apiResponse.error != null) {
                throw ExternalApiException("Twitch API error: ${apiResponse.message}")
            }

            if (apiResponse.data.isEmpty()) {
                throw NotFoundException("Twitch video not found: $videoId")
            }

            return TwitchVideoMapper.toDomainModel(apiResponse.data.first())
        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException, is NotFoundException, is ExternalApiException -> throw e
                else -> throw ExternalApiException("Failed to fetch Twitch video details", e)
            }
        }
    }

    override suspend fun getTwitchChannelVideos(
        channelId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TwitchVideoDetails> {
        val clientId = ApiKeyConfig.twitchClientId
            ?: throw ServiceUnavailableException("Twitch Client ID is not configured")

        try {
            val accessToken = twitchAuth.getAccessToken()

            val response: HttpResponse = httpClient.get("https://api.twitch.tv/helix/videos") {
                parameter("user_id", channelId)
                parameter("type", "archive")
                parameter("first", "100")
                header("Client-ID", clientId)
                header("Authorization", "Bearer $accessToken")
            }

            if (!response.status.isSuccess()) {
                throw ExternalApiException("Twitch API returned ${response.status}")
            }

            val apiResponse: TwitchApiResponse = response.body()

            // エラーレスポンスのチェック
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

            return filteredVideos.map { TwitchVideoMapper.toDomainModel(it) }
        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException, is ExternalApiException -> throw e
                else -> throw ExternalApiException("Failed to fetch Twitch channel videos", e)
            }
        }
    }
}
