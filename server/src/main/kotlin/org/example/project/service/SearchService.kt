package org.example.project.service

import org.example.project.domain.model.ChannelSearchResponse
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.VideoServiceType

/**
 * 検索サービス
 *
 * YouTube および Twitch の外部APIと連携して検索を実行する。
 */
interface SearchService {
    /**
     * 動画を検索
     *
     * @param query 検索キーワード
     * @param serviceType サービスタイプ（null の場合は両方を検索）
     * @param maxResults 最大取得件数（デフォルト: 25）
     * @param pageToken YouTubeのページネーショントークン（任意）
     * @param cursor Twitchのページネーションカーソル（任意）
     * @param eventType YouTube イベントタイプ（completed, live, upcoming, any）
     * @param order ソート順（viewCount, date, relevance, rating）
     * @return 検索結果
     * @throws org.example.project.plugins.ServiceUnavailableException APIキー未設定時
     * @throws org.example.project.plugins.ExternalApiException 外部API呼び出しエラー
     */
    suspend fun searchVideos(
        query: String,
        serviceType: VideoServiceType? = null,
        maxResults: Int = 25,
        pageToken: String? = null,
        cursor: String? = null,
        eventType: String = "completed",
        order: String = "viewCount",
    ): SearchResponse

    /**
     * チャンネルを検索
     *
     * @param query 検索キーワード
     * @param serviceType サービスタイプ（null の場合は両方を検索）
     * @param maxResults 最大取得件数（デフォルト: 25）
     * @param pageToken YouTubeのページネーショントークン（任意）
     * @param cursor Twitchのページネーションカーソル（任意）
     * @return チャンネル検索結果
     * @throws org.example.project.plugins.ServiceUnavailableException APIキー未設定時
     * @throws org.example.project.plugins.ExternalApiException 外部API呼び出しエラー
     */
    suspend fun searchChannels(
        query: String,
        serviceType: VideoServiceType? = null,
        maxResults: Int = 25,
        pageToken: String? = null,
        cursor: String? = null,
    ): ChannelSearchResponse
}
