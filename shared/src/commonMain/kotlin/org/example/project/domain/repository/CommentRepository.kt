package org.example.project.domain.repository

import org.example.project.domain.model.CommentTimestampResult

/**
 * コメント取得とタイムスタンプ抽出のためのRepositoryインターフェース。
 *
 * YouTube動画のコメントを取得し、タイムスタンプを抽出して
 * マーカー表示用のデータを提供する。
 *
 * Epic: コメントタイムスタンプ同期
 * Shared across: US-2 (実装), US-3, US-4
 */
interface CommentRepository {
    /**
     * 動画のコメントを取得し、タイムスタンプを抽出する。
     *
     * @param videoId YouTube動画ID
     * @param maxResults 取得するコメントの最大数（デフォルト100）
     * @param pageToken ページネーション用トークン
     * @param order ソート順（"relevance" または "time"）
     * @return コメントとタイムスタンプマーカーを含む結果
     */
    suspend fun getVideoComments(
        videoId: String,
        maxResults: Int = 100,
        pageToken: String? = null,
        order: String = "relevance",
    ): Result<CommentTimestampResult>
}
