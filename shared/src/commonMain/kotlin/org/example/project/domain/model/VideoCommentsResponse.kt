package org.example.project.domain.model

import kotlinx.serialization.Serializable

/**
 * サーバーからのコメントリストレスポンス。
 *
 * Epic: コメントタイムスタンプ同期
 * US-1: Comment API Proxy
 */
@Serializable
data class VideoCommentsResponse(
    val videoId: String,
    val comments: List<VideoComment>,
    val nextPageToken: String?,
)
