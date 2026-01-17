package org.example.project.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 同期履歴のデータベースエンティティ。
 *
 * SyncHistoryドメインモデルをRoomで永続化するためのテーブル定義。
 * チャンネル情報は別テーブル（saved_channel）に保存し、リレーションで関連付ける。
 *
 * Story Issue: #36
 * Epic: EPIC-003（同期チャンネル履歴保存）
 */
@Entity(tableName = "sync_history")
data class SyncHistoryEntity(
    /**
     * 履歴の一意識別子（UUID）。
     */
    @PrimaryKey
    val id: String,

    /**
     * ユーザーが付けた履歴の名前（オプション）。
     * nullの場合は自動生成名を使用。
     */
    val name: String?,

    /**
     * 履歴作成日時（epochMillis）。
     */
    val createdAt: Long,

    /**
     * 最終使用日時（epochMillis）。
     */
    val lastUsedAt: Long,

    /**
     * 使用回数（人気順ソート用）。
     */
    val usageCount: Int,
)
