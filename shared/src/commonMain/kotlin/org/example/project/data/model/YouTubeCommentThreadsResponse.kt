package org.example.project.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * YouTube Data API v3 commentThreads.list エンドポイントのレスポンスDTO。
 *
 * API Reference: https://developers.google.com/youtube/v3/docs/commentThreads/list
 *
 * Epic: コメントタイムスタンプ同期
 * US-1: Comment API Proxy
 */
@Serializable
data class YouTubeCommentThreadsResponse(
    @SerialName("kind")
    val kind: String,

    @SerialName("etag")
    val etag: String,

    @SerialName("pageInfo")
    val pageInfo: PageInfo,

    @SerialName("nextPageToken")
    val nextPageToken: String? = null,

    @SerialName("items")
    val items: List<CommentThreadItem>,
)

/**
 * ページネーション情報。
 */
@Serializable
data class PageInfo(
    @SerialName("totalResults")
    val totalResults: Int,

    @SerialName("resultsPerPage")
    val resultsPerPage: Int,
)

/**
 * コメントスレッド単位の情報。
 */
@Serializable
data class CommentThreadItem(
    @SerialName("kind")
    val kind: String,

    @SerialName("id")
    val id: String,

    @SerialName("snippet")
    val snippet: CommentThreadSnippet,
)

/**
 * コメントスレッドのスニペット。
 */
@Serializable
data class CommentThreadSnippet(
    @SerialName("videoId")
    val videoId: String,

    @SerialName("topLevelComment")
    val topLevelComment: CommentItem,

    @SerialName("totalReplyCount")
    val totalReplyCount: Int? = null,
)

/**
 * 個別コメント情報。
 */
@Serializable
data class CommentItem(
    @SerialName("kind")
    val kind: String,

    @SerialName("id")
    val id: String,

    @SerialName("snippet")
    val snippet: CommentSnippet,
)

/**
 * コメント詳細スニペット。
 */
@Serializable
data class CommentSnippet(
    @SerialName("textDisplay")
    val textDisplay: String,

    @SerialName("authorDisplayName")
    val authorDisplayName: String,

    @SerialName("authorProfileImageUrl")
    val authorProfileImageUrl: String,

    @SerialName("likeCount")
    val likeCount: Int,

    @SerialName("publishedAt")
    val publishedAt: String,
)
