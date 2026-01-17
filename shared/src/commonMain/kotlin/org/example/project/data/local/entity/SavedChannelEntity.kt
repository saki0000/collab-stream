package org.example.project.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 保存されたチャンネル情報のデータベースエンティティ。
 *
 * SyncHistoryEntityと1対多のリレーションを持つ。
 * 親の履歴が削除された場合、CASCADE削除により自動的に削除される。
 *
 * Story Issue: #36
 * Epic: EPIC-003（同期チャンネル履歴保存）
 */
@Entity(
    tableName = "saved_channel",
    foreignKeys = [
        ForeignKey(
            entity = SyncHistoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["historyId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("historyId")],
)
data class SavedChannelEntity(
    /**
     * 自動生成のプライマリキー。
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 親となるSyncHistoryのID（外部キー）。
     */
    val historyId: String,

    /**
     * チャンネルID（プラットフォーム固有）。
     */
    val channelId: String,

    /**
     * チャンネル名（表示用、キャッシュ）。
     */
    val channelName: String,

    /**
     * チャンネルアイコンURL（表示用、キャッシュ）。
     */
    val channelIconUrl: String,

    /**
     * 動画サービスタイプ（YOUTUBE or TWITCH）。
     * TypeConverterでString変換される。
     */
    val serviceType: String,
)
