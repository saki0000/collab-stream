package org.example.project.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.ChannelSearchResponse
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.plugins.configureSerialization
import org.example.project.plugins.configureStatusPages
import org.example.project.service.SearchService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * SearchRoutes のテスト
 */
class SearchRoutesTest {

    // ========================================
    // GET /api/search/videos - 動画検索
    // ========================================

    @Test
    fun `動画検索_YouTubeサービス指定で200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/videos?q=test&service=youtube")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Test YouTube Video"))
    }

    @Test
    fun `動画検索_Twitchサービス指定で200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/videos?q=test&service=twitch")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Test Twitch Video"))
    }

    @Test
    fun `動画検索_service未指定で統合検索が実行されること`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/videos?q=test")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Test YouTube Video") || body.contains("Test Twitch Video"))
    }

    @Test
    fun `動画検索_qパラメータ未指定で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/videos?service=youtube")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("q query parameter is required"))
    }

    @Test
    fun `動画検索_q空文字で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/videos?q=&service=youtube")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Search query must not be empty"))
    }

    @Test
    fun `動画検索_不正なserviceパラメータで400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/videos?q=test&service=invalid")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid service type"))
    }

    @Test
    fun `動画検索_オプションパラメータが正しく適用されること`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/videos?q=test&service=youtube&maxResults=10&eventType=live&order=date")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    // ========================================
    // GET /api/search/channels - チャンネル検索
    // ========================================

    @Test
    fun `チャンネル検索_YouTubeサービス指定で200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/channels?q=test&service=youtube")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Test YouTube Channel"))
    }

    @Test
    fun `チャンネル検索_Twitchサービス指定で200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/channels?q=test&service=twitch")

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Test Twitch Channel"))
    }

    @Test
    fun `チャンネル検索_service未指定で統合検索が実行されること`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/channels?q=test")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Test YouTube Channel") || body.contains("Test Twitch Channel"))
    }

    @Test
    fun `チャンネル検索_qパラメータ未指定で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/channels?service=youtube")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("q query parameter is required"))
    }

    @Test
    fun `チャンネル検索_q空文字で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/channels?q=&service=youtube")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Search query must not be empty"))
    }

    @Test
    fun `チャンネル検索_不正なserviceパラメータで400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                searchRoutes(createMockSearchService())
            }
        }

        val response = client.get("/api/search/channels?q=test&service=invalid")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid service type"))
    }

    // ========================================
    // モックSearchService
    // ========================================

    @OptIn(ExperimentalTime::class)
    private fun createMockSearchService(): SearchService {
        return object : SearchService {
            override suspend fun searchVideos(
                query: String,
                serviceType: VideoServiceType?,
                maxResults: Int,
                pageToken: String?,
                cursor: String?,
                eventType: String,
                order: String,
            ): SearchResponse {
                val results = when (serviceType) {
                    VideoServiceType.YOUTUBE -> listOf(
                        SearchResult(
                            videoId = "yt-video-1",
                            title = "Test YouTube Video",
                            description = "Test description",
                            thumbnailUrl = "https://example.com/thumb.jpg",
                            channelTitle = "Test YouTube Channel",
                            publishedAt = Instant.parse("2026-01-15T00:00:00Z"),
                            isLiveBroadcast = false,
                            serviceType = VideoServiceType.YOUTUBE,
                        )
                    )
                    VideoServiceType.TWITCH -> listOf(
                        SearchResult(
                            videoId = "tw-video-1",
                            title = "Test Twitch Video",
                            description = "Test description",
                            thumbnailUrl = "https://example.com/thumb.jpg",
                            channelTitle = "Test Twitch Channel",
                            publishedAt = Instant.parse("2026-01-15T00:00:00Z"),
                            isLiveBroadcast = false,
                            serviceType = VideoServiceType.TWITCH,
                        )
                    )
                    null -> listOf(
                        SearchResult(
                            videoId = "yt-video-1",
                            title = "Test YouTube Video",
                            description = "Test description",
                            thumbnailUrl = "https://example.com/thumb.jpg",
                            channelTitle = "Test YouTube Channel",
                            publishedAt = Instant.parse("2026-01-15T00:00:00Z"),
                            isLiveBroadcast = false,
                            serviceType = VideoServiceType.YOUTUBE,
                        ),
                        SearchResult(
                            videoId = "tw-video-1",
                            title = "Test Twitch Video",
                            description = "Test description",
                            thumbnailUrl = "https://example.com/thumb.jpg",
                            channelTitle = "Test Twitch Channel",
                            publishedAt = Instant.parse("2026-01-15T00:00:00Z"),
                            isLiveBroadcast = false,
                            serviceType = VideoServiceType.TWITCH,
                        )
                    )
                }

                return SearchResponse(
                    results = results,
                    nextPageToken = null,
                    totalResults = results.size,
                )
            }

            override suspend fun searchChannels(
                query: String,
                serviceType: VideoServiceType?,
                maxResults: Int,
                pageToken: String?,
                cursor: String?,
            ): ChannelSearchResponse {
                val channels = when (serviceType) {
                    VideoServiceType.YOUTUBE -> listOf(
                        ChannelInfo(
                            id = "yt-channel-1",
                            displayName = "Test YouTube Channel",
                            thumbnailUrl = "https://example.com/thumb.jpg",
                            serviceType = VideoServiceType.YOUTUBE,
                        )
                    )
                    VideoServiceType.TWITCH -> listOf(
                        ChannelInfo(
                            id = "tw-channel-1",
                            displayName = "Test Twitch Channel",
                            thumbnailUrl = "https://example.com/thumb.jpg",
                            serviceType = VideoServiceType.TWITCH,
                        )
                    )
                    null -> listOf(
                        ChannelInfo(
                            id = "yt-channel-1",
                            displayName = "Test YouTube Channel",
                            thumbnailUrl = "https://example.com/thumb.jpg",
                            serviceType = VideoServiceType.YOUTUBE,
                        ),
                        ChannelInfo(
                            id = "tw-channel-1",
                            displayName = "Test Twitch Channel",
                            thumbnailUrl = "https://example.com/thumb.jpg",
                            serviceType = VideoServiceType.TWITCH,
                        )
                    )
                }

                return ChannelSearchResponse(
                    results = channels,
                    nextPageToken = null,
                    totalResults = channels.size,
                )
            }
        }
    }
}
