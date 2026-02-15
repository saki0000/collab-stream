package org.example.project.domain.model

/**
 * コメントから抽出されたタイムスタンプマーカー。
 * タイムラインバー上に表示するマーカーの情報を保持する。
 *
 * Epic: コメントタイムスタンプ同期
 * Shared across: US-2, US-3, US-4
 */
data class TimestampMarker(
    /** 動画内の秒数 */
    val timestampSeconds: Long,
    /** 表示用タイムスタンプ文字列（例: "1:23:45"） */
    val displayTimestamp: String,
    /** タイムスタンプが含まれるコメント */
    val comment: VideoComment,
)
