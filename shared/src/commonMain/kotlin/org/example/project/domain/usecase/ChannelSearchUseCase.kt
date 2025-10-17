package org.example.project.domain.usecase

import org.example.project.data.datasource.TwitchSearchDataSource
import org.example.project.data.mapper.TwitchChannelMapper.toChannelInfoList
import org.example.project.domain.model.ChannelInfo

/**
 * Use case for searching Twitch channels.
 * Returns a list of matching channels for display in search suggestions.
 */
class ChannelSearchUseCase(
    private val twitchSearchDataSource: TwitchSearchDataSource,
) {

    /**
     * Search for Twitch channels by query
     *
     * @param query The search query (channel name)
     * @param maxResults Maximum number of results to return (default: 5)
     * @return Result containing a list of matching channels
     */
    suspend fun searchTwitchChannels(
        query: String,
        maxResults: Int = 5,
    ): Result<List<ChannelInfo>> {
        if (query.isBlank()) {
            return Result.failure(IllegalArgumentException("Search query cannot be empty"))
        }

        return twitchSearchDataSource.searchChannels(
            query = query.trim(),
            maxResults = maxResults.coerceIn(1, 20),
        ).map { response ->
            response.data.toChannelInfoList()
        }
    }
}
