package org.example.project.data.local.mapper

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.example.project.data.local.entity.FollowedChannelEntity
import org.example.project.domain.model.FollowedChannel

/**
 * FollowedChannelEntity ↔ FollowedChannel（Domain Model）の変換マッパー。
 *
 * Story Issue: US-1（チャンネルフォロー データ層）
 * Specification: feature/channel_follow/SPECIFICATION.md
 */
object FollowedChannelMapper {
    /**
     * EntityからDomainモデルに変換する。
     */
    @OptIn(ExperimentalTime::class)
    fun toDomain(entity: FollowedChannelEntity): FollowedChannel {
        return FollowedChannel(
            channelId = entity.channelId,
            channelName = entity.channelName,
            channelIconUrl = entity.channelIconUrl,
            serviceType = entity.serviceType,
            followedAt = Instant.fromEpochMilliseconds(entity.followedAt),
        )
    }

    /**
     * DomainモデルからEntityに変換する。
     */
    @OptIn(ExperimentalTime::class)
    fun toEntity(domain: FollowedChannel): FollowedChannelEntity {
        return FollowedChannelEntity(
            channelId = domain.channelId,
            channelName = domain.channelName,
            channelIconUrl = domain.channelIconUrl,
            serviceType = domain.serviceType,
            followedAt = domain.followedAt.toEpochMilliseconds(),
        )
    }
}
