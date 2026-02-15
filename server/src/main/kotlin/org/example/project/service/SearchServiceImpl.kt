@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.example.project.config.ApiKeyConfig
import org.example.project.data.mapper.TwitchChannelMapper.toChannelInfoList
import org.example.project.data.mapper.TwitchSearchMapper
import org.example.project.data.mapper.YouTubeChannelMapper.toChannelInfoList
import org.example.project.data.mapper.YouTubeSearchMapper
import org.example.project.data.model.TwitchSearchResponse
import org.example.project.data.model.TwitchUserResponse
import org.example.project.data.model.YouTubeChannelSearchResponse
import org.example.project.data.model.YouTubeSearchResponse
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.ChannelSearchResponse
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.plugins.ExternalApiException
import org.example.project.plugins.ServiceUnavailableException

/**
 * SearchService の実装
 *
 * Ktor HttpClient を使用して外部APIを呼び出し、検索結果を取得する。
 */
class SearchServiceImpl(
    private val httpClient: HttpClient
) : SearchService {

    // ========================================
    // 動画検索
    // ========================================

    override suspend fun searchVideos(
        query: String,
        serviceType: VideoServiceType?,
        maxResults: Int,
        pageToken: String?,
        cursor: String?,
        eventType: String,
        order: String,
    ): SearchResponse {
        return when (serviceType) {
            VideoServiceType.YOUTUBE -> searchYouTubeVideos(query, maxResults, pageToken, eventType, order)
            VideoServiceType.TWITCH -> searchTwitchVideos(query, maxResults, cursor)
            null -> searchBothVideos(query, maxResults, pageToken, cursor, eventType, order)
        }
    }

    /**
     * YouTube動画検索
     */
    private suspend fun searchYouTubeVideos(
        query: String,
        maxResults: Int,
        pageToken: String?,
        eventType: String,
        order: String,
    ): SearchResponse {
        val apiKey = ApiKeyConfig.youtubeApiKey
            ?: throw ServiceUnavailableException("YouTube API key is not configured")

        try {
            val response: HttpResponse = httpClient.get("https://www.googleapis.com/youtube/v3/search") {
                parameter("part", "snippet")
                parameter("q", query)
                parameter("type", "video")
                parameter("maxResults", maxResults.coerceAtMost(50))
                parameter("order", order)
                if (eventType != "any") {
                    parameter("eventType", eventType)
                }
                if (pageToken != null) {
                    parameter("pageToken", pageToken)
                }
                parameter("key", apiKey)
            }

            if (!response.status.isSuccess()) {
                throw ExternalApiException("YouTube Search API returned ${response.status}")
            }

            val apiResponse: YouTubeSearchResponse = response.body()
            return YouTubeSearchMapper.mapToSearchResponse(apiResponse)
        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException, is ExternalApiException -> throw e
                else -> throw ExternalApiException("Failed to search YouTube videos", e)
            }
        }
    }

    /**
     * Twitch動画検索（2段階）
     * Step 1: search/channels でチャンネル検索
     * Step 2: 最初のチャンネルの videos を取得
     */
    private suspend fun searchTwitchVideos(
        query: String,
        maxResults: Int,
        cursor: String?,
    ): SearchResponse {
        val clientId = ApiKeyConfig.twitchClientId
            ?: throw ServiceUnavailableException("Twitch Client ID is not configured")
        val clientSecret = ApiKeyConfig.twitchClientSecret
            ?: throw ServiceUnavailableException("Twitch Client Secret is not configured")

        try {
            // Step 1: チャンネル検索
            val channelResponse: HttpResponse = httpClient.get("https://api.twitch.tv/helix/search/channels") {
                parameter("query", query)
                parameter("first", "1")
                header("Client-ID", clientId)
                header("Authorization", "Bearer $clientSecret")
            }

            if (!channelResponse.status.isSuccess()) {
                throw ExternalApiException("Twitch Search Channels API returned ${channelResponse.status}")
            }

            val channelResult: TwitchUserResponse = channelResponse.body()

            if (channelResult.error != null) {
                throw ExternalApiException("Twitch API error: ${channelResult.message}")
            }

            if (channelResult.data.isEmpty()) {
                return SearchResponse(
                    results = emptyList(),
                    nextPageToken = null,
                    totalResults = 0,
                )
            }

            // Step 2: 最初のチャンネルの動画を取得
            val userId = channelResult.data.first().id

            val videoResponse: HttpResponse = httpClient.get("https://api.twitch.tv/helix/videos") {
                parameter("user_id", userId)
                parameter("type", "archive")
                parameter("first", maxResults.coerceAtMost(100))
                if (cursor != null) {
                    parameter("after", cursor)
                }
                header("Client-ID", clientId)
                header("Authorization", "Bearer $clientSecret")
            }

            if (!videoResponse.status.isSuccess()) {
                throw ExternalApiException("Twitch Videos API returned ${videoResponse.status}")
            }

            val videoResult: TwitchSearchResponse = videoResponse.body()

            if (videoResult.error != null) {
                throw ExternalApiException("Twitch API error: ${videoResult.message}")
            }

            return TwitchSearchMapper.mapToSearchResponse(videoResult)
        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException, is ExternalApiException -> throw e
                else -> throw ExternalApiException("Failed to search Twitch videos", e)
            }
        }
    }

    /**
     * YouTube + Twitch の統合検索
     * 両方を並行呼び出しし、片方のエラーは無視して成功した結果のみ返す
     */
    private suspend fun searchBothVideos(
        query: String,
        maxResults: Int,
        pageToken: String?,
        cursor: String?,
        eventType: String,
        order: String,
    ): SearchResponse = coroutineScope {
        val youtubeDeferred = async {
            try {
                searchYouTubeVideos(query, maxResults, pageToken, eventType, order)
            } catch (e: Exception) {
                null // エラーは無視
            }
        }

        val twitchDeferred = async {
            try {
                searchTwitchVideos(query, maxResults, cursor)
            } catch (e: Exception) {
                null // エラーは無視
            }
        }

        val youtubeResult = youtubeDeferred.await()
        val twitchResult = twitchDeferred.await()

        // 両方の結果を統合
        val combinedResults = mutableListOf<SearchResult>()
        val serviceTokens = mutableMapOf<VideoServiceType, String?>()

        youtubeResult?.let {
            combinedResults.addAll(it.results)
            serviceTokens[VideoServiceType.YOUTUBE] = it.nextPageToken
        }

        twitchResult?.let {
            combinedResults.addAll(it.results)
            serviceTokens[VideoServiceType.TWITCH] = it.nextPageToken
        }

        SearchResponse(
            results = combinedResults,
            nextPageToken = youtubeResult?.nextPageToken, // デフォルトはYouTube
            totalResults = combinedResults.size,
            servicePageTokens = serviceTokens,
        )
    }

    // ========================================
    // チャンネル検索
    // ========================================

    override suspend fun searchChannels(
        query: String,
        serviceType: VideoServiceType?,
        maxResults: Int,
        pageToken: String?,
        cursor: String?,
    ): ChannelSearchResponse {
        return when (serviceType) {
            VideoServiceType.YOUTUBE -> searchYouTubeChannels(query, maxResults, pageToken)
            VideoServiceType.TWITCH -> searchTwitchChannels(query, maxResults, cursor)
            null -> searchBothChannels(query, maxResults, pageToken, cursor)
        }
    }

    /**
     * YouTubeチャンネル検索
     */
    private suspend fun searchYouTubeChannels(
        query: String,
        maxResults: Int,
        pageToken: String?,
    ): ChannelSearchResponse {
        val apiKey = ApiKeyConfig.youtubeApiKey
            ?: throw ServiceUnavailableException("YouTube API key is not configured")

        try {
            val response: HttpResponse = httpClient.get("https://www.googleapis.com/youtube/v3/search") {
                parameter("part", "snippet")
                parameter("q", query)
                parameter("type", "channel")
                parameter("maxResults", maxResults.coerceAtMost(50))
                if (pageToken != null) {
                    parameter("pageToken", pageToken)
                }
                parameter("key", apiKey)
            }

            if (!response.status.isSuccess()) {
                throw ExternalApiException("YouTube Search API returned ${response.status}")
            }

            val apiResponse: YouTubeChannelSearchResponse = response.body()
            val channels = apiResponse.items.toChannelInfoList()

            return ChannelSearchResponse(
                results = channels,
                nextPageToken = null, // YouTubeChannelSearchResponse にはページトークンがない
                totalResults = channels.size,
            )
        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException, is ExternalApiException -> throw e
                else -> throw ExternalApiException("Failed to search YouTube channels", e)
            }
        }
    }

    /**
     * Twitchチャンネル検索
     */
    private suspend fun searchTwitchChannels(
        query: String,
        maxResults: Int,
        cursor: String?,
    ): ChannelSearchResponse {
        val clientId = ApiKeyConfig.twitchClientId
            ?: throw ServiceUnavailableException("Twitch Client ID is not configured")
        val clientSecret = ApiKeyConfig.twitchClientSecret
            ?: throw ServiceUnavailableException("Twitch Client Secret is not configured")

        try {
            val response: HttpResponse = httpClient.get("https://api.twitch.tv/helix/search/channels") {
                parameter("query", query)
                parameter("first", maxResults.coerceAtMost(100))
                if (cursor != null) {
                    parameter("after", cursor)
                }
                header("Client-ID", clientId)
                header("Authorization", "Bearer $clientSecret")
            }

            if (!response.status.isSuccess()) {
                throw ExternalApiException("Twitch Search Channels API returned ${response.status}")
            }

            val apiResponse: TwitchUserResponse = response.body()

            if (apiResponse.error != null) {
                throw ExternalApiException("Twitch API error: ${apiResponse.message}")
            }

            val channels = apiResponse.data.toChannelInfoList()

            return ChannelSearchResponse(
                results = channels,
                nextPageToken = null, // カーソルベースなので直接ページトークンには変換しない
                totalResults = channels.size,
            )
        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException, is ExternalApiException -> throw e
                else -> throw ExternalApiException("Failed to search Twitch channels", e)
            }
        }
    }

    /**
     * YouTube + Twitch の統合チャンネル検索
     * 両方を並行呼び出しし、片方のエラーは無視して成功した結果のみ返す
     */
    private suspend fun searchBothChannels(
        query: String,
        maxResults: Int,
        pageToken: String?,
        cursor: String?,
    ): ChannelSearchResponse = coroutineScope {
        val youtubeDeferred = async {
            try {
                searchYouTubeChannels(query, maxResults, pageToken)
            } catch (e: Exception) {
                null // エラーは無視
            }
        }

        val twitchDeferred = async {
            try {
                searchTwitchChannels(query, maxResults, cursor)
            } catch (e: Exception) {
                null // エラーは無視
            }
        }

        val youtubeResult = youtubeDeferred.await()
        val twitchResult = twitchDeferred.await()

        // 両方の結果を統合
        val combinedChannels = mutableListOf<ChannelInfo>()
        val serviceTokens = mutableMapOf<VideoServiceType, String?>()

        youtubeResult?.let {
            combinedChannels.addAll(it.results)
            serviceTokens[VideoServiceType.YOUTUBE] = it.nextPageToken
        }

        twitchResult?.let {
            combinedChannels.addAll(it.results)
            serviceTokens[VideoServiceType.TWITCH] = it.nextPageToken
        }

        ChannelSearchResponse(
            results = combinedChannels,
            nextPageToken = youtubeResult?.nextPageToken, // デフォルトはYouTube
            totalResults = combinedChannels.size,
            servicePageTokens = serviceTokens,
        )
    }
}
