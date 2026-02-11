package org.example.project.data.mapper

import org.example.project.data.model.YouTubeChannelSearchItem
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.VideoServiceType

/**
 * YouTube チャンネル検索結果をドメインモデルに変換するマッパー。
 */
object YouTubeChannelMapper {

    /**
     * YouTubeChannelSearchItem を ChannelInfo ドメインモデルに変換する。
     */
    fun YouTubeChannelSearchItem.toChannelInfo(): ChannelInfo {
        return ChannelInfo(
            id = id.channelId,
            displayName = snippet.title,
            thumbnailUrl = snippet.thumbnails?.default?.url,
            serviceType = VideoServiceType.YOUTUBE,
        )
    }

    /**
     * YouTubeChannelSearchItem のリストを ChannelInfo のリストに変換する。
     */
    fun List<YouTubeChannelSearchItem>.toChannelInfoList(): List<ChannelInfo> {
        return map { it.toChannelInfo() }
    }
}
