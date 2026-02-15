package org.example.project.domain.model

/**
 * YouTube動画のコメント情報を表すドメインモデル。
 *
 * Epic: コメントタイムスタンプ同期
 * Shared across: US-2, US-3, US-4
 */
data class VideoComment(
    val commentId: String,
    val authorDisplayName: String,
    val authorProfileImageUrl: String,
    val textContent: String,
    val likeCount: Int,
    val publishedAt: String,
)
