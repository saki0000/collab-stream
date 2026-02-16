package org.example.project.domain.model

import kotlinx.serialization.Serializable

/**
 * チャンネル検索のレスポンスモデル。
 * チャンネル一覧とページネーション情報を含む。
 */
@Serializable
data class ChannelSearchResponse(
    val results: List<ChannelInfo>,
    val nextPageToken: String? = null,
    val totalResults: Int,
    val servicePageTokens: Map<VideoServiceType, String?> = emptyMap(),
    val hasMoreResults: Boolean = nextPageToken != null || servicePageTokens.values.any { it != null },
)
