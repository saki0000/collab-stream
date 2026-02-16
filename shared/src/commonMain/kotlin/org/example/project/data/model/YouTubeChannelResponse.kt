package org.example.project.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * YouTube Data API v3 のチャンネル検索レスポンスモデル。
 * /search エンドポイントを type=channel で呼び出した際のレスポンス。
 */
@Serializable
data class YouTubeChannelSearchResponse(
    @SerialName("items")
    val items: List<YouTubeChannelSearchItem> = emptyList(),
    @SerialName("nextPageToken")
    val nextPageToken: String? = null,
    @SerialName("pageInfo")
    val pageInfo: YouTubePageInfo? = null,
)

/**
 * YouTube チャンネル検索結果の個別アイテム。
 */
@Serializable
data class YouTubeChannelSearchItem(
    @SerialName("id")
    val id: YouTubeChannelSearchId,
    @SerialName("snippet")
    val snippet: YouTubeChannelSnippet,
)

/**
 * YouTube チャンネル検索結果のID情報。
 */
@Serializable
data class YouTubeChannelSearchId(
    @SerialName("kind")
    val kind: String,
    @SerialName("channelId")
    val channelId: String,
)

/**
 * YouTube チャンネル検索結果のスニペット情報。
 */
@Serializable
data class YouTubeChannelSnippet(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String = "",
    @SerialName("thumbnails")
    val thumbnails: YouTubeThumbnails? = null,
    @SerialName("channelTitle")
    val channelTitle: String = "",
)
