package org.example.project.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 同期履歴とそれに紐づくチャンネル情報を結合したデータクラス。
 *
 * Roomの@Relationアノテーションにより、1対多のリレーションを
 * 1回のクエリで取得できる。
 *
 * Story Issue: #36
 * Epic: EPIC-003（同期チャンネル履歴保存）
 */
data class SyncHistoryWithChannels(
    /**
     * 同期履歴のエンティティ。
     */
    @Embedded
    val history: SyncHistoryEntity,

    /**
     * 履歴に紐づくチャンネル情報のリスト。
     */
    @Relation(
        parentColumn = "id",
        entityColumn = "historyId",
    )
    val channels: List<SavedChannelEntity>,
)
