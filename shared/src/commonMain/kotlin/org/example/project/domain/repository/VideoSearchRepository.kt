package org.example.project.domain.repository

import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SearchQuery
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.VideoServiceType

interface VideoSearchRepository {
    suspend fun searchVideos(searchQuery: SearchQuery): Result<SearchResponse>

    suspend fun searchVideosByService(
        searchQuery: SearchQuery,
        serviceType: VideoServiceType,
    ): Result<SearchResponse>

    /**
     * チャンネル検索
     *
     * @param query 検索クエリ（チャンネル名）
     * @param serviceType 検索対象のプラットフォーム
     * @param maxResults 最大結果数
     * @return 検索結果のチャンネルリスト
     */
    suspend fun searchChannels(
        query: String,
        serviceType: VideoServiceType,
        maxResults: Int = 5,
    ): Result<List<ChannelInfo>>
}
