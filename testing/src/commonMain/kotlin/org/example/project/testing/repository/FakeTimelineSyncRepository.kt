package org.example.project.testing.repository

import kotlinx.datetime.LocalDate
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.TimelineSyncRepository

/**
 * テスト用TimelineSyncRepository Fake実装。
 *
 * VideoSyncRepositoryを継承し、チャンネル動画取得機能を追加。
 * ViewModelテストやインテグレーションテストで使用。
 */
class FakeTimelineSyncRepository : TimelineSyncRepository {
    var shouldReturnError: Boolean = false
    var errorToReturn: Throwable = RuntimeException("Fake error")
    var videoDetailsToReturn: VideoDetails? = null
    var channelVideosToReturn: List<VideoDetails> = emptyList()

    private val _getVideoDetailsCalls = mutableListOf<GetVideoDetailsCall>()
    private val _getChannelVideosCalls = mutableListOf<GetChannelVideosCall>()

    val getVideoDetailsCalls: List<GetVideoDetailsCall> get() = _getVideoDetailsCalls
    val getChannelVideosCalls: List<GetChannelVideosCall> get() = _getChannelVideosCalls

    /**
     * getVideoDetailsの呼び出し記録。
     */
    data class GetVideoDetailsCall(
        val videoId: String,
        val serviceType: VideoServiceType,
    )

    /**
     * getChannelVideosの呼び出し記録。
     */
    data class GetChannelVideosCall(
        val channelId: String,
        val serviceType: VideoServiceType,
        val dateRange: ClosedRange<LocalDate>,
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

    override suspend fun getChannelVideos(
        channelId: String,
        serviceType: VideoServiceType,
        dateRange: ClosedRange<LocalDate>,
    ): Result<List<VideoDetails>> {
        _getChannelVideosCalls.add(GetChannelVideosCall(channelId, serviceType, dateRange))

        return if (shouldReturnError) {
            Result.failure(errorToReturn)
        } else {
            Result.success(channelVideosToReturn)
        }
    }

    /** テスト間で状態をリセット */
    fun reset() {
        shouldReturnError = false
        errorToReturn = RuntimeException("Fake error")
        videoDetailsToReturn = null
        channelVideosToReturn = emptyList()
        _getVideoDetailsCalls.clear()
        _getChannelVideosCalls.clear()
    }
}
