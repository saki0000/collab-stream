package org.example.project.domain.usecase

import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.VideoSearchRepository

/**
 * マルチプラットフォーム対応のチャンネル検索ユースケース。
 * Twitch と YouTube のチャンネル検索をサポートする。
 *
 * サーバーAPI経由でチャンネル検索を実行する。
 * ADR-005 Phase 2: データソース依存からRepository依存に変更。
 */
class ChannelSearchUseCase(
    private val videoSearchRepository: VideoSearchRepository,
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
        return videoSearchRepository.searchChannels(query, serviceType, maxResults)
    }

    /**
     * Twitch チャンネルを検索する。
     * 既存の searchTwitchChannels メソッド（後方互換性のため残存）。
     */
    suspend fun searchTwitchChannels(
        query: String,
        maxResults: Int = 5,
    ): Result<List<ChannelInfo>> {
        return searchChannels(query, VideoServiceType.TWITCH, maxResults)
    }
}
