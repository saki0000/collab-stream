package org.example.project.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.Serializable

/**
 * 同期セッションの履歴を表すEntity。
 *
 * 複数のチャンネルの組み合わせを1つのグループとして保存し、
 * 後から同じチャンネル構成を素早く復元できるようにする。
 *
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 * Shared across: US-1 (永続化基盤), US-2 (履歴保存), US-3 (履歴一覧), US-4 (再同期)
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class SyncHistory(
    /**
     * 履歴の一意識別子（UUID）。
     */
    val id: String,

    /**
     * ユーザーが付けた履歴の名前（オプション）。
     *
     * 例: "コラボ配信視聴用", "Apex大会グループ"
     * nullの場合は自動生成名（例: "チャンネル名1 + チャンネル名2"）を使用。
     */
    val name: String?,

    /**
     * 保存されたチャンネル情報のリスト。
     *
     * 同期に使用されたチャンネルの組み合わせを保持。
     * 最小2チャンネル以上が必要。
     */
    val channels: List<SavedChannelInfo>,

    /**
     * 履歴作成日時。
     */
    val createdAt: Instant,

    /**
     * 最終使用日時（再同期に使用された日時）。
     *
     * 履歴から復元を実行するたびに更新される。
     * 初回は createdAt と同じ値。
     */
    val lastUsedAt: Instant,

    /**
     * 使用回数（人気順ソート用）。
     *
     * 履歴から復元を実行するたびにインクリメントされる。
     * 初回作成時は 0。
     */
    val usageCount: Int = 0,
)

/**
 * 履歴の表示名を取得する拡張プロパティ。
 *
 * ユーザー設定名がある場合はそれを返し、
 * ない場合はチャンネル名を連結した自動生成名を返す。
 */
@OptIn(ExperimentalTime::class)
val SyncHistory.displayName: String
    get() = name ?: channels.joinToString(" + ") { it.channelName }

/**
 * 履歴に含まれるチャンネル数を取得する拡張プロパティ。
 */
@OptIn(ExperimentalTime::class)
val SyncHistory.channelCount: Int
    get() = channels.size
