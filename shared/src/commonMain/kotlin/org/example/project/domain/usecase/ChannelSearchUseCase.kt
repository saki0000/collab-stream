package org.example.project.domain.usecase

import org.example.project.data.datasource.TwitchSearchDataSource
import org.example.project.data.datasource.YouTubeSearchDataSource
import org.example.project.data.mapper.TwitchChannelMapper.toChannelInfoList
import org.example.project.data.mapper.YouTubeChannelMapper.toChannelInfoList
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.VideoServiceType

/**
 * マルチプラットフォーム対応のチャンネル検索ユースケース。
 * Twitch と YouTube のチャンネル検索をサポートする。
 */
class ChannelSearchUseCase(
    private val twitchSearchDataSource: TwitchSearchDataSource,
    private val youTubeSearchDataSource: YouTubeSearchDataSource,
) {

    /**
     * 指定プラットフォームでチャンネルを検索する。
     *
     * @param query 検索クエリ（チャンネル名）
     * @param serviceType 検索対象のプラットフォーム
     * @param maxResults 最大結果数（デフォルト: 5）
     * @return 検索結果のチャンネルリストを含む Result
     */
    suspend fun searchChannels(
        query: String,
        serviceType: VideoServiceType,
        maxResults: Int = 5,
    ): Result<List<ChannelInfo>> {
        if (query.isBlank()) {
            return Result.failure(IllegalArgumentException("Search query cannot be empty"))
        }

        return when (serviceType) {
            VideoServiceType.TWITCH -> searchTwitchChannels(query, maxResults)
            VideoServiceType.YOUTUBE -> searchYouTubeChannels(query, maxResults)
        }
    }

    /**
     * Twitch チャンネルを検索する。
     * 既存の searchTwitchChannels メソッド（後方互換性のため残存）。
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

    /**
     * YouTube チャンネルを検索する。
     */
    private suspend fun searchYouTubeChannels(
        query: String,
        maxResults: Int = 5,
    ): Result<List<ChannelInfo>> {
        return youTubeSearchDataSource.searchChannels(
            query = query.trim(),
            maxResults = maxResults.coerceIn(1, 10),
        ).map { response ->
            response.items.toChannelInfoList()
        }
    }
}
