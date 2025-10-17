package org.example.project.data.mapper

import org.example.project.data.model.TwitchUser
import org.example.project.domain.model.ChannelInfo

/**
 * Mapper for converting Twitch API channel data to domain models
 */
object TwitchChannelMapper {

    /**
     * Converts a TwitchUser to ChannelInfo domain model
     */
    fun TwitchUser.toChannelInfo(): ChannelInfo {
        return ChannelInfo(
            id = id,
            displayName = displayName,
            thumbnailUrl = thumbnailUrl,
            broadcasterLanguage = broadcasterLanguage,
            gameId = gameId,
            gameName = gameName,
        )
    }

    /**
     * Converts a list of TwitchUsers to a list of ChannelInfo domain models
     */
    fun List<TwitchUser>.toChannelInfoList(): List<ChannelInfo> {
        return map { it.toChannelInfo() }
    }
}
