package org.example.project.data.repository

/**
 * コメントAPI固有のエラー。
 * サーバーからエラーレスポンスが返された場合にスローされる。
 */
class CommentApiException(
    val code: Int,
    override val message: String,
) : Exception("API Error: $message (code: $code)")

/**
 * コメント取得時のネットワーク・通信エラー。
 * ネットワーク障害やタイムアウト等で発生する。
 */
class CommentFetchException(
    val videoId: String,
    override val cause: Throwable?,
) : Exception("Failed to fetch video comments for video ID '$videoId': ${cause?.message}", cause)
