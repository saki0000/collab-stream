package org.example.project.domain.model

/**
 * コメント取得とタイムスタンプ抽出の結果を表すドメインモデル。
 *
 * Epic: コメントタイムスタンプ同期
 * Shared across: US-2, US-3, US-4
 */
data class CommentTimestampResult(
    val videoId: String,
    val comments: List<VideoComment>,
    val timestampMarkers: List<TimestampMarker>,
    val nextPageToken: String?,
    val commentsDisabled: Boolean = false,
)
