package org.example.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.example.project.domain.model.VideoServiceType

/**
 * VideoSearchRepositoryImplのテスト。
 *
 * searchChannelsメソッドのバリデーションロジックを検証する。
 * Story Issue: US-4（クライアント側Repository移行）
 */
class VideoSearchRepositoryImplTest {

    private fun createMockHttpClient(
        responseBody: String = """{"data": []}""",
        statusCode: HttpStatusCode = HttpStatusCode.OK,
    ): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = responseBody,
                        status = statusCode,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                        isLenient = true
                    },
                )
            }
        }
    }

    // ========================================
    // searchChannels - 空クエリバリデーション
    // ========================================

    @Test
    fun `searchChannels_空文字クエリでエラーを返すこと`() = runTest {
        // Arrange
        val repository = VideoSearchRepositoryImpl(createMockHttpClient())

        // Act
        val result = repository.searchChannels(
            query = "",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `searchChannels_空白のみのクエリでエラーを返すこと`() = runTest {
        // Arrange
        val repository = VideoSearchRepositoryImpl(createMockHttpClient())

        // Act
        val result = repository.searchChannels(
            query = "   ",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    // ========================================
    // searchChannels - クエリトリミング
    // ========================================

    @Test
    fun `searchChannels_前後の空白がトリミングされてリクエストされること`() = runTest {
        // Arrange
        var requestedQuery: String? = null
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    requestedQuery = request.url.parameters["q"]
                    respond(
                        content = """{"data": []}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val repository = VideoSearchRepositoryImpl(httpClient)

        // Act
        repository.searchChannels(
            query = "  test channel  ",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Assert
        assertEquals("test channel", requestedQuery)
    }

    // ========================================
    // searchChannels - maxResults制限
    // ========================================

    @Test
    fun `searchChannels_maxResultsが上限20に制限されること`() = runTest {
        // Arrange
        var requestedMaxResults: String? = null
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    requestedMaxResults = request.url.parameters["maxResults"]
                    respond(
                        content = """{"data": []}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val repository = VideoSearchRepositoryImpl(httpClient)

        // Act
        repository.searchChannels(
            query = "test",
            serviceType = VideoServiceType.YOUTUBE,
            maxResults = 100,
        )

        // Assert
        assertEquals("20", requestedMaxResults)
    }

    @Test
    fun `searchChannels_maxResultsが下限1に制限されること`() = runTest {
        // Arrange
        var requestedMaxResults: String? = null
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    requestedMaxResults = request.url.parameters["maxResults"]
                    respond(
                        content = """{"data": []}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val repository = VideoSearchRepositoryImpl(httpClient)

        // Act
        repository.searchChannels(
            query = "test",
            serviceType = VideoServiceType.YOUTUBE,
            maxResults = 0,
        )

        // Assert
        assertEquals("1", requestedMaxResults)
    }

    // ========================================
    // searchChannels - URLパス構築
    // ========================================

    @Test
    fun `searchChannels_正しいAPIパスにリクエストされること`() = runTest {
        // Arrange
        var requestedPath: String? = null
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    requestedPath = request.url.encodedPath
                    respond(
                        content = """{"data": []}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val repository = VideoSearchRepositoryImpl(httpClient)

        // Act
        repository.searchChannels(
            query = "test",
            serviceType = VideoServiceType.TWITCH,
        )

        // Assert
        assertEquals("/api/search/channels", requestedPath)
    }
}
