@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.feature.timeline_sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.example.project.domain.model.FollowedChannel
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.ChannelFollowRepository

/**
 * テスト用 ChannelFollowRepository のフェイク実装。
 */
class FakeChannelFollowRepository : ChannelFollowRepository {
    private val followedChannels = mutableListOf<FollowedChannel>()
    private val _flow = MutableStateFlow<List<FollowedChannel>>(emptyList())

    override suspend fun follow(
        channelId: String,
        channelName: String,
        channelIconUrl: String,
        serviceType: VideoServiceType,
    ): Result<FollowedChannel> {
        val channel = FollowedChannel(
            channelId = channelId,
            channelName = channelName,
            channelIconUrl = channelIconUrl,
            serviceType = serviceType,
            followedAt = kotlin.time.Clock.System.now(),
        )
        followedChannels.removeAll { it.channelId == channelId && it.serviceType == serviceType }
        followedChannels.add(channel)
        _flow.value = followedChannels.toList()
        return Result.success(channel)
    }

    override suspend fun unfollow(channelId: String, serviceType: VideoServiceType): Result<Unit> {
        followedChannels.removeAll { it.channelId == channelId && it.serviceType == serviceType }
        _flow.value = followedChannels.toList()
        return Result.success(Unit)
    }

    override suspend fun isFollowing(channelId: String, serviceType: VideoServiceType): Boolean {
        return followedChannels.any { it.channelId == channelId && it.serviceType == serviceType }
    }

    override suspend fun getAllFollowedChannels(): Result<List<FollowedChannel>> {
        return Result.success(followedChannels.toList())
    }

    override fun observeFollowedChannels(): Flow<List<FollowedChannel>> {
        return _flow
    }
}
