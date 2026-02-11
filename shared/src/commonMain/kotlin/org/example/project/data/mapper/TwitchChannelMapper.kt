package org.example.project.data.mapper

import org.example.project.data.model.TwitchUser
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.VideoServiceType

/**
 * Twitch API のチャンネルデータをドメインモデルに変換するマッパー。
 */
object TwitchChannelMapper {

    /**
     * TwitchUser を ChannelInfo ドメインモデルに変換する。
     */
    fun TwitchUser.toChannelInfo(): ChannelInfo {
        return ChannelInfo(
            id = id,
            displayName = displayName,
            thumbnailUrl = thumbnailUrl,
            broadcasterLanguage = broadcasterLanguage,
            gameId = gameId,
            gameName = gameName,
            serviceType = VideoServiceType.TWITCH,
        )
    }

    /**
     * TwitchUser のリストを ChannelInfo のリストに変換する。
     */
    fun List<TwitchUser>.toChannelInfoList(): List<ChannelInfo> {
        return map { it.toChannelInfo() }
    }
}
