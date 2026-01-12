package org.example.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.exampl.project.BuildKonfig
import org.example.project.data.mapper.TwitchVideoMapper
import org.example.project.data.mapper.YouTubeVideoMapper
import org.example.project.data.model.TwitchApiResponse
import org.example.project.data.model.YouTubeApiResponse
import org.example.project.domain.model.TwitchVideoDetailsImpl
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.model.YouTubeVideoDetailsImpl
import org.example.project.domain.repository.TimelineSyncRepository

/**
 * Implementation of TimelineSyncRepository supporting both YouTube Data API v3 and Twitch Helix API.
 *
 * Handles HTTP communication with video service APIs for timeline sync functionality.
 * Extends VideoSyncRepositoryImpl functionality to add channel video retrieval.
 *
 * Epic: Timeline Sync (EPIC-002)
 */
@OptIn(ExperimentalTime::class)
class TimelineSyncRepositoryImpl(
    private val httpClient: HttpClient,
) : TimelineSyncRepository {

    companion object {
        private const val YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3"
        private const val YOUTUBE_VIDEOS_ENDPOINT = "$YOUTUBE_API_BASE_URL/videos"
        private const val YOUTUBE_SEARCH_ENDPOINT = "$YOUTUBE_API_BASE_URL/search"

        private const val TWITCH_API_BASE_URL = "https://api.twitch.tv/helix"
        private const val TWITCH_VIDEOS_ENDPOINT = "$TWITCH_API_BASE_URL/videos"
    }

    override suspend fun getVideoDetails(videoId: String, serviceType: VideoServiceType): Result<VideoDetails> {
        return when (serviceType) {
            VideoServiceType.YOUTUBE -> getYouTubeVideoDetails(videoId)
            VideoServiceType.TWITCH -> getTwitchVideoDetails(videoId)
        }
    }

    override suspend fun getChannelVideos(
        channelId: String,
        serviceType: VideoServiceType,
        dateRange: ClosedRange<LocalDate>,
    ): Result<List<VideoDetails>> {
        return when (serviceType) {
            VideoServiceType.YOUTUBE -> getYouTubeChannelVideos(channelId, dateRange)
            VideoServiceType.TWITCH -> getTwitchChannelVideos(channelId, dateRange)
        }
    }

    private suspend fun getYouTubeVideoDetails(videoId: String): Result<VideoDetails> {
        return try {
            if (BuildKonfig.API_KEY.isBlank()) {
                return Result.failure(IllegalStateException("YouTube API key is not configured"))
            }

            val response = httpClient.get(YOUTUBE_VIDEOS_ENDPOINT) {
                parameter("part", "liveStreamingDetails,snippet")
                parameter("id", videoId)
                parameter("key", BuildKonfig.API_KEY)
            }

            val apiResponse: YouTubeApiResponse = response.body()

            if (apiResponse.items.isEmpty()) {
                return Result.failure(
                    NoSuchElementException("YouTube video with ID '$videoId' not found"),
                )
            }

            val videoItem = apiResponse.items.first()
            val domainModel = YouTubeVideoMapper.toDomainModel(videoItem)

            val unifiedModel = YouTubeVideoDetailsImpl(
                id = domainModel.id,
                snippet = domainModel.snippet,
                liveStreamingDetails = domainModel.liveStreamingDetails,
            )

            Result.success(unifiedModel)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to fetch YouTube video details for video ID '$videoId': ${e.message}", e),
            )
        }
    }

    private suspend fun getTwitchVideoDetails(videoId: String): Result<VideoDetails> {
        return try {
            if (BuildKonfig.TWITCH_CLIENT_ID.isBlank() || BuildKonfig.TWITCH_API_KEY.isBlank()) {
                return Result.failure(IllegalStateException("Twitch API credentials are not configured"))
            }

            val response = httpClient.get(TWITCH_VIDEOS_ENDPOINT) {
                parameter("id", videoId)
                header("Client-ID", BuildKonfig.TWITCH_CLIENT_ID)
                header("Authorization", "Bearer ${BuildKonfig.TWITCH_API_KEY}")
            }

            if (response.status.value !in 200..299) {
                return Result.failure(
                    RuntimeException("Twitch API returned HTTP ${response.status.value}: ${response.status.description}"),
                )
            }

            val apiResponse: TwitchApiResponse = response.body()

            if (!apiResponse.error.isNullOrBlank()) {
                return Result.failure(
                    RuntimeException("Twitch API error: ${apiResponse.error} - ${apiResponse.message ?: "Unknown error"}"),
                )
            }

            if (apiResponse.data.isEmpty()) {
                return Result.failure(
                    NoSuchElementException("Twitch video with ID '$videoId' not found"),
                )
            }

            val videoItem = apiResponse.data.first()
            val domainModel = TwitchVideoMapper.toDomainModel(videoItem)

            val unifiedModel = TwitchVideoDetailsImpl(
                id = domainModel.id,
                snippet = domainModel.snippet,
                streamInfo = domainModel.streamInfo,
            )

            Result.success(unifiedModel)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to fetch Twitch video details for video ID '$videoId': ${e.message}", e),
            )
        }
    }

    private suspend fun getYouTubeChannelVideos(
        channelId: String,
        dateRange: ClosedRange<LocalDate>,
    ): Result<List<VideoDetails>> {
        return try {
            if (BuildKonfig.API_KEY.isBlank()) {
                return Result.failure(IllegalStateException("YouTube API key is not configured"))
            }

            val timeZone = TimeZone.UTC
            val publishedAfter = dateRange.start.atStartOfDayIn(timeZone)
            val publishedBefore = dateRange.endInclusive.atStartOfDayIn(timeZone) + 1.days

            // Step 1: Search for videos in the date range
            val searchResponse = httpClient.get(YOUTUBE_SEARCH_ENDPOINT) {
                parameter("part", "snippet")
                parameter("channelId", channelId)
                parameter("type", "video")
                parameter("eventType", "completed")
                parameter("order", "date")
                parameter("maxResults", 50)
                parameter("publishedAfter", publishedAfter.toString())
                parameter("publishedBefore", publishedBefore.toString())
                parameter("key", BuildKonfig.API_KEY)
            }

            val searchApiResponse: org.example.project.data.model.YouTubeSearchResponse = searchResponse.body()

            if (searchApiResponse.items.isEmpty()) {
                return Result.success(emptyList())
            }

            // Step 2: Get video details for each video ID
            val videoIds = searchApiResponse.items.mapNotNull { it.id?.videoId }.joinToString(",")

            if (videoIds.isBlank()) {
                return Result.success(emptyList())
            }

            val videosResponse = httpClient.get(YOUTUBE_VIDEOS_ENDPOINT) {
                parameter("part", "liveStreamingDetails,snippet")
                parameter("id", videoIds)
                parameter("key", BuildKonfig.API_KEY)
            }

            val videosApiResponse: YouTubeApiResponse = videosResponse.body()

            val videos = videosApiResponse.items.map { videoItem ->
                val domainModel = YouTubeVideoMapper.toDomainModel(videoItem)
                YouTubeVideoDetailsImpl(
                    id = domainModel.id,
                    snippet = domainModel.snippet,
                    liveStreamingDetails = domainModel.liveStreamingDetails,
                )
            }

            Result.success(videos)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to fetch YouTube channel videos for channel '$channelId': ${e.message}", e),
            )
        }
    }

    private suspend fun getTwitchChannelVideos(
        channelId: String,
        dateRange: ClosedRange<LocalDate>,
    ): Result<List<VideoDetails>> {
        return try {
            if (BuildKonfig.TWITCH_CLIENT_ID.isBlank() || BuildKonfig.TWITCH_API_KEY.isBlank()) {
                return Result.failure(IllegalStateException("Twitch API credentials are not configured"))
            }

            val response = httpClient.get(TWITCH_VIDEOS_ENDPOINT) {
                parameter("user_id", channelId)
                parameter("type", "archive")
                parameter("first", 100)
                header("Client-ID", BuildKonfig.TWITCH_CLIENT_ID)
                header("Authorization", "Bearer ${BuildKonfig.TWITCH_API_KEY}")
            }

            if (response.status.value !in 200..299) {
                return Result.failure(
                    RuntimeException("Twitch API returned HTTP ${response.status.value}: ${response.status.description}"),
                )
            }

            val apiResponse: TwitchApiResponse = response.body()

            if (!apiResponse.error.isNullOrBlank()) {
                return Result.failure(
                    RuntimeException("Twitch API error: ${apiResponse.error} - ${apiResponse.message ?: "Unknown error"}"),
                )
            }

            val timeZone = TimeZone.UTC
            val startInstant = dateRange.start.atStartOfDayIn(timeZone)
            val endInstant = dateRange.endInclusive.atStartOfDayIn(timeZone) + 1.days

            val videos = apiResponse.data
                .map { videoItem ->
                    val domainModel = TwitchVideoMapper.toDomainModel(videoItem)
                    TwitchVideoDetailsImpl(
                        id = domainModel.id,
                        snippet = domainModel.snippet,
                        streamInfo = domainModel.streamInfo,
                    )
                }
                .filter { video ->
                    val startTime = video.getStartTimeForSync()
                    startTime != null && startTime >= startInstant && startTime < endInstant
                }

            Result.success(videos)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to fetch Twitch channel videos for channel '$channelId': ${e.message}", e),
            )
        }
    }
}
