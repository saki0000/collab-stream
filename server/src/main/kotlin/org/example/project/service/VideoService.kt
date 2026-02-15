package org.example.project.service

import kotlinx.datetime.LocalDate
import org.example.project.domain.model.TwitchVideoDetails
import org.example.project.domain.model.YouTubeVideoDetails

/**
 * 動画情報取得サービス
 *
 * YouTube および Twitch の外部APIと連携して動画情報を取得する。
 */
interface VideoService {
    /**
     * YouTube動画の詳細を取得
     *
     * @param videoId YouTube動画ID
     * @return YouTube動画詳細
     * @throws org.example.project.plugins.ServiceUnavailableException APIキー未設定時
     * @throws org.example.project.plugins.ExternalApiException 外部API呼び出しエラー
     * @throws org.example.project.plugins.NotFoundException 動画が見つからない場合
     */
    suspend fun getYouTubeVideoDetails(videoId: String): YouTubeVideoDetails

    /**
     * Twitch動画の詳細を取得
     *
     * @param videoId Twitch動画ID
     * @return Twitch動画詳細
     * @throws org.example.project.plugins.ServiceUnavailableException APIキー未設定時
     * @throws org.example.project.plugins.ExternalApiException 外部API呼び出しエラー
     * @throws org.example.project.plugins.NotFoundException 動画が見つからない場合
     */
    suspend fun getTwitchVideoDetails(videoId: String): TwitchVideoDetails

    /**
     * YouTubeチャンネルの日付範囲内の動画一覧を取得
     *
     * @param channelId YouTubeチャンネルID
     * @param startDate 開始日（inclusive）
     * @param endDate 終了日（inclusive）
     * @return YouTube動画一覧
     * @throws org.example.project.plugins.ServiceUnavailableException APIキー未設定時
     * @throws org.example.project.plugins.ExternalApiException 外部API呼び出しエラー
     */
    suspend fun getYouTubeChannelVideos(
        channelId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<YouTubeVideoDetails>

    /**
     * Twitchチャンネルの日付範囲内の動画一覧を取得
     *
     * @param channelId TwitchユーザーID
     * @param startDate 開始日（inclusive）
     * @param endDate 終了日（inclusive）
     * @return Twitch動画一覧
     * @throws org.example.project.plugins.ServiceUnavailableException APIキー未設定時
     * @throws org.example.project.plugins.ExternalApiException 外部API呼び出しエラー
     */
    suspend fun getTwitchChannelVideos(
        channelId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TwitchVideoDetails>
}
