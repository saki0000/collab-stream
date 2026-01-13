package org.example.project.testing.repository

import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.VideoSyncRepository

/**
 * テスト用VideoSyncRepository Fake実装。
 *
 * 設定可能な戻り値とエラー状態を提供し、
 * ユースケーステストで使用する。
 *
 * サブクラスで拡張可能なopen classとして定義。
 */
open class FakeVideoSyncRepository : VideoSyncRepository {
    /** エラーを返すかどうかのフラグ */
    var shouldReturnError: Boolean = false

    /** エラー時に返す例外 */
    var errorToReturn: Throwable = RuntimeException("Fake error")

    /** 成功時に返すVideoDetails */
    var videoDetailsToReturn: VideoDetails? = null

    /** 呼び出し履歴を記録 */
    private val _getVideoDetailsCalls = mutableListOf<GetVideoDetailsCall>()
    val getVideoDetailsCalls: List<GetVideoDetailsCall> get() = _getVideoDetailsCalls

    /**
     * getVideoDetailsの呼び出し記録。
     */
    data class GetVideoDetailsCall(
        val videoId: String,
        val serviceType: VideoServiceType,
    )

    override suspend fun getVideoDetails(
        videoId: String,
        serviceType: VideoServiceType,
    ): Result<VideoDetails> {
        _getVideoDetailsCalls.add(GetVideoDetailsCall(videoId, serviceType))

        return if (shouldReturnError) {
            Result.failure(errorToReturn)
        } else {
            videoDetailsToReturn?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException("Video not found"))
        }
    }

    /** テスト間で状態をリセット */
    open fun reset() {
        shouldReturnError = false
        errorToReturn = RuntimeException("Fake error")
        videoDetailsToReturn = null
        _getVideoDetailsCalls.clear()
    }
}
