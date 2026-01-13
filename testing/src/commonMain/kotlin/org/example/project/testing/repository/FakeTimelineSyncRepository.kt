package org.example.project.testing.repository

import kotlinx.datetime.LocalDate
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.TimelineSyncRepository

/**
 * テスト用TimelineSyncRepository Fake実装。
 *
 * FakeVideoSyncRepositoryを継承し、チャンネル動画取得機能を追加。
 * ViewModelテストやインテグレーションテストで使用。
 */
class FakeTimelineSyncRepository : FakeVideoSyncRepository(), TimelineSyncRepository {
    var channelVideosToReturn: List<VideoDetails> = emptyList()

    private val _getChannelVideosCalls = mutableListOf<GetChannelVideosCall>()

    val getChannelVideosCalls: List<GetChannelVideosCall> get() = _getChannelVideosCalls

    /**
     * getChannelVideosの呼び出し記録。
     */
    data class GetChannelVideosCall(
        val channelId: String,
        val serviceType: VideoServiceType,
        val dateRange: ClosedRange<LocalDate>,
    )

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
    override fun reset() {
        super.reset()
        channelVideosToReturn = emptyList()
        _getChannelVideosCalls.clear()
    }
}
