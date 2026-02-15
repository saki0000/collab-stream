package org.example.project.data.local.entity

import androidx.room.Entity
import org.example.project.domain.model.VideoServiceType

/**
 * フォロー済みチャンネル情報のデータベースエンティティ。
 *
 * channelId + serviceTypeの複合キーで一意性を保証する。
 * （同じチャンネルIDでもYouTube/Twitchで別のフォローとして扱う）
 *
 * Story Issue: US-1（チャンネルフォロー データ層）
 * Specification: feature/channel_follow/SPECIFICATION.md
 */
@Entity(
    tableName = "followed_channel",
    primaryKeys = ["channelId", "serviceType"],
)
data class FollowedChannelEntity(
    /**
     * チャンネルID（プラットフォーム固有）。
     */
    val channelId: String,

    /**
     * チャンネル名（表示用）。
     */
    val channelName: String,

    /**
     * チャンネルアイコンURL（表示用）。
     */
    val channelIconUrl: String,

    /**
     * 動画サービスタイプ（YOUTUBE or TWITCH）。
     * Converters.TypeConverterによりString変換される。
     */
    val serviceType: VideoServiceType,

    /**
     * フォロー日時（epoch milliseconds）。
     */
    val followedAt: Long,
)
