package org.example.project.data.repository

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.data.local.FollowedChannelDao
import org.example.project.data.local.entity.FollowedChannelEntity
import org.example.project.data.local.mapper.FollowedChannelMapper
import org.example.project.domain.model.FollowedChannel
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.ChannelFollowRepository

/**
 * ChannelFollowRepositoryの実装。
 *
 * Room DAOを使用してローカルにフォロー情報を永続化する。
 *
 * Story Issue: US-1（チャンネルフォロー データ層）
 * Specification: feature/channel_follow/SPECIFICATION.md
 */
@OptIn(ExperimentalTime::class)
class ChannelFollowRepositoryImpl(
    private val dao: FollowedChannelDao,
) : ChannelFollowRepository {
    /**
     * チャンネルをフォローする。
     *
     * 既存のフォロー情報があればUPSERTされる（冪等性を保証）。
     */
    override suspend fun follow(
        channelId: String,
        channelName: String,
        channelIconUrl: String,
        serviceType: VideoServiceType,
    ): Result<FollowedChannel> = runCatching {
        val now = Clock.System.now()
        val entity = FollowedChannelEntity(
            channelId = channelId,
            channelName = channelName,
            channelIconUrl = channelIconUrl,
            serviceType = serviceType,
            followedAt = now.toEpochMilliseconds(),
        )
        dao.insert(entity)
        FollowedChannelMapper.toDomain(entity)
    }

    /**
     * チャンネルのフォローを解除する。
     *
     * 存在しない場合でもエラーにならず正常終了する（冪等性を保証）。
     */
    override suspend fun unfollow(
        channelId: String,
        serviceType: VideoServiceType,
    ): Result<Unit> = runCatching {
        dao.delete(channelId, serviceType)
    }

    /**
     * 指定したチャンネルがフォロー済みかどうかを確認する。
     */
    override suspend fun isFollowing(
        channelId: String,
        serviceType: VideoServiceType,
    ): Boolean {
        return dao.isFollowing(channelId, serviceType)
    }

    /**
     * フォロー済みチャンネル一覧を取得する。
     *
     * フォロー日時の降順（新しいフォローが先頭）で返される。
     */
    override suspend fun getAllFollowedChannels(): Result<List<FollowedChannel>> = runCatching {
        dao.getAll().map { FollowedChannelMapper.toDomain(it) }
    }

    /**
     * フォロー済みチャンネル一覧をFlowで監視する。
     *
     * フォロー日時の降順（新しいフォローが先頭）で返される。
     */
    override fun observeFollowedChannels(): Flow<List<FollowedChannel>> {
        return dao.observeAll().map { list ->
            list.map { FollowedChannelMapper.toDomain(it) }
        }
    }
}
