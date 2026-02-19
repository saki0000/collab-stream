package org.example.project.service

import org.example.project.domain.model.VideoCommentsResponse

/**
 * コメント取得サービス
 *
 * YouTube API との連携して動画のコメント情報を取得する。
 *
 * Epic: コメントタイムスタンプ同期
 * US-1: Comment API Proxy
 */
interface CommentService {
    /**
     * YouTube動画のコメント一覧を取得
     *
     * @param videoId YouTube動画ID
     * @param maxResults 取得件数（1-100、デフォルト100）
     * @param pageToken ページネーショントークン
     * @param order ソート順（relevance/time、デフォルトrelevance）
     * @return コメント一覧レスポンス
     * @throws org.example.project.plugins.ServiceUnavailableException APIキー未設定時
     * @throws org.example.project.plugins.CommentsDisabledException コメント無効化時
     * @throws org.example.project.plugins.ExternalApiException 外部API呼び出しエラー
     */
    suspend fun getYouTubeComments(
        videoId: String,
        maxResults: Int = 100,
        pageToken: String? = null,
        order: String = "relevance"
    ): VideoCommentsResponse
}
