package org.example.project.data.datasource

import org.example.project.data.model.YouTubeChannelSearchResponse
import org.example.project.data.model.YouTubeSearchResponse
import org.example.project.domain.model.SearchQuery

interface YouTubeSearchDataSource {
    suspend fun searchVideos(searchQuery: SearchQuery): Result<YouTubeSearchResponse>

    /**
     * YouTube チャンネルを検索する。
     * YouTube Data API v3 の /search エンドポイント（type=channel）を使用。
     *
     * @param query 検索クエリ（チャンネル名）
     * @param maxResults 最大結果数（デフォルト: 5）
     * @return YouTubeChannelSearchResponse を含む Result
     */
    suspend fun searchChannels(query: String, maxResults: Int = 5): Result<YouTubeChannelSearchResponse>
}
