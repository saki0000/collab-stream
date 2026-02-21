package org.example.project.feature.timeline_sync

import org.example.project.domain.model.CommentTimestampResult
import org.example.project.domain.repository.CommentRepository

/**
 * テスト用 CommentRepository のフェイク実装。
 *
 * デフォルトでは空のタイムスタンプマーカーリストを返す。
 * テストごとに `returnResult` を設定することで任意の結果を返せる。
 */
class FakeCommentRepository : CommentRepository {

    /** 次の getVideoComments 呼び出しで返す結果 */
    var returnResult: Result<CommentTimestampResult> = Result.success(
        CommentTimestampResult(
            videoId = "",
            comments = emptyList(),
            timestampMarkers = emptyList(),
            nextPageToken = null,
            commentsDisabled = false,
        ),
    )

    /** getVideoComments が呼ばれた回数 */
    var callCount = 0

    /** 最後に渡された videoId */
    var lastVideoId: String? = null

    override suspend fun getVideoComments(
        videoId: String,
        maxResults: Int,
        pageToken: String?,
        order: String,
    ): Result<CommentTimestampResult> {
        callCount++
        lastVideoId = videoId
        return returnResult.map { it.copy(videoId = videoId) }
    }
}
