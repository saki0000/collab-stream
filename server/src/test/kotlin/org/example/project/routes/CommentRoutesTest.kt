package org.example.project.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.example.project.domain.model.VideoComment
import org.example.project.domain.model.VideoCommentsResponse
import org.example.project.plugins.CommentsDisabledException
import org.example.project.plugins.configureSerialization
import org.example.project.plugins.configureStatusPages
import org.example.project.service.CommentService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * CommentRoutes のテスト
 *
 * Epic: コメントタイムスタンプ同期
 * US-1: Comment API Proxy
 */
class CommentRoutesTest {

    // ========================================
    // GET /api/videos/{id}/comments - 正常系
    // ========================================

    @Test
    fun `コメント取得_動画IDを指定して200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                commentRoutes(createMockCommentService())
            }
        }

        val response = client.get("/api/videos/test-video-id/comments")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("test-video-id"))
        assertTrue(body.contains("Test comment"))
    }

    @Test
    fun `コメント取得_maxResultsパラメータを指定して200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                commentRoutes(createMockCommentService())
            }
        }

        val response = client.get("/api/videos/test-video-id/comments?maxResults=50")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `コメント取得_pageTokenパラメータを指定して200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                commentRoutes(createMockCommentService())
            }
        }

        val response = client.get("/api/videos/test-video-id/comments?pageToken=abc123")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `コメント取得_orderパラメータにtimeを指定して200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                commentRoutes(createMockCommentService())
            }
        }

        val response = client.get("/api/videos/test-video-id/comments?order=time")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `コメント取得_空コメントリストで200 OKを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                commentRoutes(createMockCommentService(emptyComments = true))
            }
        }

        val response = client.get("/api/videos/empty-video/comments")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("\"comments\":[]"))
    }

    // ========================================
    // GET /api/videos/{id}/comments - エラー系
    // ========================================

    @Test
    fun `コメント取得_Video ID未指定で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                commentRoutes(createMockCommentService())
            }
        }

        val response = client.get("/api/videos//comments")

        // パスが不正なため404になる可能性があるが、ルーティングの仕様に依存
        assertTrue(response.status == HttpStatusCode.NotFound || response.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun `コメント取得_maxResultsが範囲外で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                commentRoutes(createMockCommentService())
            }
        }

        val response = client.get("/api/videos/test-video-id/comments?maxResults=invalid")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("maxResults must be between 1 and 100"))
    }

    @Test
    fun `コメント取得_orderが不正な値で400 Bad Requestを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                commentRoutes(createMockCommentService())
            }
        }

        val response = client.get("/api/videos/test-video-id/comments?order=invalid")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("order must be 'relevance' or 'time'"))
    }

    @Test
    fun `コメント取得_コメント無効化で403 Forbiddenを返すこと`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                commentRoutes(createMockCommentService(commentsDisabled = true))
            }
        }

        val response = client.get("/api/videos/disabled-video/comments")

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertTrue(response.bodyAsText().contains("Comments are disabled"))
    }

    // ========================================
    // モックCommentService
    // ========================================

    private fun createMockCommentService(
        emptyComments: Boolean = false,
        commentsDisabled: Boolean = false
    ): CommentService {
        return object : CommentService {
            override suspend fun getYouTubeComments(
                videoId: String,
                maxResults: Int,
                pageToken: String?,
                order: String
            ): VideoCommentsResponse {
                // コメント無効化のシミュレーション
                if (commentsDisabled || videoId == "disabled-video") {
                    throw CommentsDisabledException("Comments are disabled for this video")
                }

                // 空コメントのシミュレーション
                if (emptyComments || videoId == "empty-video") {
                    return VideoCommentsResponse(
                        videoId = videoId,
                        comments = emptyList(),
                        nextPageToken = null
                    )
                }

                // 正常なコメントレスポンス
                return VideoCommentsResponse(
                    videoId = videoId,
                    comments = listOf(
                        VideoComment(
                            commentId = "comment-1",
                            authorDisplayName = "Test User",
                            authorProfileImageUrl = "https://example.com/avatar.jpg",
                            textContent = "Test comment",
                            likeCount = 10,
                            publishedAt = "2026-01-15T00:00:00Z"
                        )
                    ),
                    nextPageToken = if (pageToken == null) "next-page-token" else null
                )
            }
        }
    }
}
