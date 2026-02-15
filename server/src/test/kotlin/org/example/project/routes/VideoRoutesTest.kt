package org.example.project.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.example.project.domain.model.ApiResponse
import org.example.project.domain.model.LiveStreamingDetails
import org.example.project.domain.model.TwitchStreamInfo
import org.example.project.domain.model.TwitchVideoDetails
import org.example.project.domain.model.VideoSnippet
import org.example.project.domain.model.YouTubeVideoDetails
import org.example.project.plugins.configureSerialization
import org.example.project.plugins.configureStatusPages
import org.example.project.service.VideoService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * VideoRoutes のテスト
 */
class VideoRoutesTest {

    // ========================================
    // GET /api/videos/{id} - 動画詳細
    // ========================================

    @Test
    fun `動画詳細取得_YouTubeサービス指定で200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                videoRoutes(createMockVideoService())
            }
        }

        val response = client.get("/api/videos/test-youtube-id?service=youtube")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Test YouTube Video"))
    }

    @Test
    fun `動画詳細取得_Twitchサービス指定で200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                videoRoutes(createMockVideoService())
            }
        }

        val response = client.get("/api/videos/test-twitch-id?service=twitch")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Test Twitch Video"))
    }

    @Test
    fun `動画詳細取得_serviceパラメータ未指定で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                videoRoutes(createMockVideoService())
            }
        }

        val response = client.get("/api/videos/test-id")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("service query parameter is required"))
    }

    @Test
    fun `動画詳細取得_不正なserviceパラメータで400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                videoRoutes(createMockVideoService())
            }
        }

        val response = client.get("/api/videos/test-id?service=invalid")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid service type"))
    }

    // ========================================
    // GET /api/channels/{id}/videos - チャンネル動画一覧
    // ========================================

    @Test
    fun `チャンネル動画一覧_YouTubeサービス指定で200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                videoRoutes(createMockVideoService())
            }
        }

        val response = client.get("/api/channels/test-channel-id/videos?service=youtube&startDate=2026-01-01&endDate=2026-01-31")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Test YouTube Video"))
    }

    @Test
    fun `チャンネル動画一覧_Twitchサービス指定で200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                videoRoutes(createMockVideoService())
            }
        }

        val response = client.get("/api/channels/test-channel-id/videos?service=twitch&startDate=2026-01-01&endDate=2026-01-31")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Test Twitch Video"))
    }

    @Test
    fun `チャンネル動画一覧_serviceパラメータ未指定で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                videoRoutes(createMockVideoService())
            }
        }

        val response = client.get("/api/channels/test-id/videos?startDate=2026-01-01&endDate=2026-01-31")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("service query parameter is required"))
    }

    @Test
    fun `チャンネル動画一覧_startDateパラメータ未指定で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                videoRoutes(createMockVideoService())
            }
        }

        val response = client.get("/api/channels/test-id/videos?service=youtube&endDate=2026-01-31")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("startDate query parameter is required"))
    }

    @Test
    fun `チャンネル動画一覧_endDateパラメータ未指定で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                videoRoutes(createMockVideoService())
            }
        }

        val response = client.get("/api/channels/test-id/videos?service=youtube&startDate=2026-01-01")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("endDate query parameter is required"))
    }

    @Test
    fun `チャンネル動画一覧_不正な日付フォーマットで400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                videoRoutes(createMockVideoService())
            }
        }

        val response = client.get("/api/channels/test-id/videos?service=youtube&startDate=invalid&endDate=2026-01-31")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid startDate format"))
    }

    // ========================================
    // モックVideoService
    // ========================================

    private fun createMockVideoService(): VideoService {
        return object : VideoService {
            override suspend fun getYouTubeVideoDetails(videoId: String): YouTubeVideoDetails {
                return YouTubeVideoDetails(
                    id = videoId,
                    snippet = VideoSnippet(
                        title = "Test YouTube Video",
                        description = "Test description",
                        channelId = "test-channel",
                        channelTitle = "Test Channel"
                    ),
                    liveStreamingDetails = null
                )
            }

            override suspend fun getTwitchVideoDetails(videoId: String): TwitchVideoDetails {
                return TwitchVideoDetails(
                    id = videoId,
                    snippet = VideoSnippet(
                        title = "Test Twitch Video",
                        description = "Test description",
                        channelId = "test-channel",
                        channelTitle = "Test Channel"
                    ),
                    streamInfo = TwitchStreamInfo(
                        streamId = null,
                        createdAt = "2026-01-15T00:00:00Z",
                        publishedAt = "2026-01-15T00:00:00Z",
                        type = "archive",
                        duration = "1h30m",
                        viewable = "public"
                    )
                )
            }

            override suspend fun getYouTubeChannelVideos(
                channelId: String,
                startDate: LocalDate,
                endDate: LocalDate
            ): List<YouTubeVideoDetails> {
                return listOf(
                    YouTubeVideoDetails(
                        id = "video-1",
                        snippet = VideoSnippet(
                            title = "Test YouTube Video",
                            description = "Test description",
                            channelId = channelId,
                            channelTitle = "Test Channel"
                        ),
                        liveStreamingDetails = null
                    )
                )
            }

            override suspend fun getTwitchChannelVideos(
                channelId: String,
                startDate: LocalDate,
                endDate: LocalDate
            ): List<TwitchVideoDetails> {
                return listOf(
                    TwitchVideoDetails(
                        id = "video-1",
                        snippet = VideoSnippet(
                            title = "Test Twitch Video",
                            description = "Test description",
                            channelId = channelId,
                            channelTitle = "Test Channel"
                        ),
                        streamInfo = TwitchStreamInfo(
                            streamId = null,
                            createdAt = "2026-01-15T00:00:00Z",
                            publishedAt = "2026-01-15T00:00:00Z",
                            type = "archive",
                            duration = "1h30m",
                            viewable = "public"
                        )
                    )
                )
            }
        }
    }
}
