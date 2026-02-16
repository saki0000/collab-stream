package org.example.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.config.ApiKeyConfig
import org.example.project.data.mapper.YouTubeCommentMapper
import org.example.project.data.model.YouTubeCommentThreadsResponse
import org.example.project.domain.model.VideoCommentsResponse
import org.example.project.plugins.CommentsDisabledException
import org.example.project.plugins.ExternalApiException
import org.example.project.plugins.ServiceUnavailableException

/**
 * CommentService の実装
 *
 * Ktor HttpClient を使用して YouTube commentThreads.list API を呼び出す。
 *
 * Epic: コメントタイムスタンプ同期
 * US-1: Comment API Proxy
 */
class CommentServiceImpl(
    private val httpClient: HttpClient
) : CommentService {

    // ========================================
    // YouTube API
    // ========================================

    override suspend fun getYouTubeComments(
        videoId: String,
        maxResults: Int,
        pageToken: String?,
        order: String
    ): VideoCommentsResponse {
        val apiKey = ApiKeyConfig.youtubeApiKey
            ?: throw ServiceUnavailableException("YouTube API key is not configured")

        try {
            val response: HttpResponse = httpClient.get("https://www.googleapis.com/youtube/v3/commentThreads") {
                parameter("part", "snippet")
                parameter("videoId", videoId)
                parameter("maxResults", maxResults.coerceIn(1, 100))
                parameter("order", order)
                if (pageToken != null) {
                    parameter("pageToken", pageToken)
                }
                parameter("key", apiKey)
            }

            // ステータスコードチェック
            if (response.status == HttpStatusCode.Forbidden) {
                // 403 エラーの場合、レスポンスボディをパースしてcommentsDisabled検出
                val errorBody = try {
                    response.body<YouTubeErrorResponse>()
                } catch (e: Exception) {
                    throw ExternalApiException("YouTube API returned 403 Forbidden", e)
                }

                val isCommentsDisabled = errorBody.error.errors.any { it.reason == "commentsDisabled" }
                if (isCommentsDisabled) {
                    throw CommentsDisabledException("Comments are disabled for this video")
                } else {
                    throw ExternalApiException("YouTube API returned 403: ${errorBody.error.message}")
                }
            }

            if (!response.status.isSuccess()) {
                throw ExternalApiException("YouTube API returned ${response.status}")
            }

            val apiResponse: YouTubeCommentThreadsResponse = response.body()

            // コメントがない場合は空リストを返す
            if (apiResponse.items.isEmpty()) {
                return VideoCommentsResponse(
                    videoId = videoId,
                    comments = emptyList(),
                    nextPageToken = null
                )
            }

            // マッパーでドメインモデルに変換
            val comments = YouTubeCommentMapper.toDomainModelList(apiResponse.items)

            return VideoCommentsResponse(
                videoId = videoId,
                comments = comments,
                nextPageToken = apiResponse.nextPageToken
            )
        } catch (e: Exception) {
            when (e) {
                is ServiceUnavailableException,
                is CommentsDisabledException,
                is ExternalApiException -> throw e
                else -> throw ExternalApiException("Failed to fetch YouTube comments", e)
            }
        }
    }
}

// ========================================
// YouTube API エラーレスポンスDTO
// ========================================

/**
 * YouTube API のエラーレスポンス形状。
 *
 * 例:
 * ```json
 * {
 *   "error": {
 *     "code": 403,
 *     "message": "...",
 *     "errors": [
 *       {"reason": "commentsDisabled", "domain": "youtube.commentThread", ...}
 *     ]
 *   }
 * }
 * ```
 */
@Serializable
private data class YouTubeErrorResponse(
    @SerialName("error")
    val error: YouTubeError
)

@Serializable
private data class YouTubeError(
    @SerialName("code")
    val code: Int,

    @SerialName("message")
    val message: String,

    @SerialName("errors")
    val errors: List<YouTubeErrorDetail>
)

@Serializable
private data class YouTubeErrorDetail(
    @SerialName("reason")
    val reason: String,

    @SerialName("domain")
    val domain: String? = null,

    @SerialName("message")
    val message: String? = null
)
