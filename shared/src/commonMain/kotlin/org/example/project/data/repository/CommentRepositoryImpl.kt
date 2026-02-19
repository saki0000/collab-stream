package org.example.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.appendPathSegments
import org.example.project.domain.model.ApiResponse
import org.example.project.domain.model.CommentTimestampResult
import org.example.project.domain.model.TimestampExtractor
import org.example.project.domain.model.TimestampMarker
import org.example.project.domain.model.VideoCommentsResponse
import org.example.project.domain.repository.CommentRepository

/**
 * CommentRepositoryの実装。
 * サーバープロキシエンドポイントを呼び出し、コメント取得とタイムスタンプ抽出を行う。
 *
 * Epic: コメントタイムスタンプ同期
 * US-2: タイムスタンプ抽出とマーカー生成
 */
class CommentRepositoryImpl(
    private val httpClient: HttpClient,
    private val serverBaseUrl: String,
) : CommentRepository {

    override suspend fun getVideoComments(
        videoId: String,
        maxResults: Int,
        pageToken: String?,
        order: String,
    ): Result<CommentTimestampResult> {
        return try {
            val response = httpClient.get(serverBaseUrl) {
                url {
                    appendPathSegments("api", "videos", videoId, "comments")
                }
                parameter("maxResults", maxResults)
                if (pageToken != null) {
                    parameter("pageToken", pageToken)
                }
                parameter("order", order)
            }

            // レスポンスの解析
            when (val apiResponse: ApiResponse<VideoCommentsResponse> = response.body()) {
                is ApiResponse.Success -> {
                    val data = apiResponse.data
                    val markers = buildTimestampMarkers(data.comments)

                    Result.success(
                        CommentTimestampResult(
                            videoId = data.videoId,
                            comments = data.comments,
                            timestampMarkers = markers,
                            nextPageToken = data.nextPageToken,
                            commentsDisabled = false,
                        ),
                    )
                }

                is ApiResponse.Error -> {
                    // 403エラー（commentsDisabled）の処理
                    if (apiResponse.code == 403) {
                        Result.success(
                            CommentTimestampResult(
                                videoId = videoId,
                                comments = emptyList(),
                                timestampMarkers = emptyList(),
                                nextPageToken = null,
                                commentsDisabled = true,
                            ),
                        )
                    } else {
                        Result.failure(
                            CommentApiException(code = apiResponse.code, message = apiResponse.message),
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(
                CommentFetchException(videoId = videoId, cause = e),
            )
        }
    }

    /**
     * コメントリストからタイムスタンプマーカーを構築する。
     *
     * @param comments コメントリスト
     * @return タイムスタンプマーカーのリスト
     */
    private fun buildTimestampMarkers(comments: List<org.example.project.domain.model.VideoComment>): List<TimestampMarker> {
        return comments.flatMap { comment ->
            val timestamps = TimestampExtractor.extractTimestamps(
                text = comment.textContent,
                videoDurationSeconds = null, // 動画の長さチェックはここでは行わない
            )

            timestamps.map { extractedTimestamp ->
                TimestampMarker(
                    timestampSeconds = extractedTimestamp.timestampSeconds,
                    displayTimestamp = extractedTimestamp.displayTimestamp,
                    comment = comment,
                )
            }
        }
    }
}
