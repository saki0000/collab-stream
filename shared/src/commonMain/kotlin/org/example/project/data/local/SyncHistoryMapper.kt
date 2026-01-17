package org.example.project.data.local

import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.Instant
import org.example.project.data.local.entity.SavedChannelEntity
import org.example.project.data.local.entity.SyncHistoryEntity
import org.example.project.data.local.entity.SyncHistoryWithChannels
import org.example.project.domain.model.SavedChannelInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncHistory
import org.example.project.domain.model.VideoServiceType

/**
 * Entity層とDomain層のモデル変換を行うMapper。
 *
 * Story Issue: #36
 * Epic: EPIC-003（同期チャンネル履歴保存）
 */
object SyncHistoryMapper {

    // ===================
    // Entity → Domain
    // ===================

    /**
     * SyncHistoryWithChannelsをSyncHistoryドメインモデルに変換する。
     *
     * @param entity 変換元のエンティティ
     * @return 変換後のドメインモデル
     */
    @OptIn(ExperimentalTime::class)
    fun toDomain(entity: SyncHistoryWithChannels): SyncHistory {
        return SyncHistory(
            id = entity.history.id,
            name = entity.history.name,
            channels = entity.channels.map { toDomain(it) },
            createdAt = Instant.fromEpochMilliseconds(entity.history.createdAt),
            lastUsedAt = Instant.fromEpochMilliseconds(entity.history.lastUsedAt),
            usageCount = entity.history.usageCount,
        )
    }

    /**
     * SavedChannelEntityをSavedChannelInfoドメインモデルに変換する。
     *
     * @param entity 変換元のエンティティ
     * @return 変換後のドメインモデル
     */
    private fun toDomain(entity: SavedChannelEntity): SavedChannelInfo {
        return SavedChannelInfo(
            channelId = entity.channelId,
            channelName = entity.channelName,
            channelIconUrl = entity.channelIconUrl,
            serviceType = VideoServiceType.valueOf(entity.serviceType),
        )
    }

    // ===================
    // Domain → Entity
    // ===================

    /**
     * SyncChannelリストからSyncHistoryEntityを作成する。
     *
     * @param id 履歴のID
     * @param name 履歴の名前（オプション）
     * @param createdAt 作成日時
     * @param lastUsedAt 最終使用日時
     * @param usageCount 使用回数
     * @return 変換後のエンティティ
     */
    @OptIn(ExperimentalTime::class)
    fun toHistoryEntity(
        id: String,
        name: String?,
        createdAt: Instant,
        lastUsedAt: Instant,
        usageCount: Int,
    ): SyncHistoryEntity {
        return SyncHistoryEntity(
            id = id,
            name = name,
            createdAt = createdAt.toEpochMilliseconds(),
            lastUsedAt = lastUsedAt.toEpochMilliseconds(),
            usageCount = usageCount,
        )
    }

    /**
     * SyncChannelリストからSavedChannelEntityリストを作成する。
     *
     * @param channels 変換元のSyncChannelリスト
     * @param historyId 親となる履歴のID
     * @return 変換後のエンティティリスト
     */
    fun toChannelEntities(
        channels: List<SyncChannel>,
        historyId: String,
    ): List<SavedChannelEntity> {
        return channels.map { channel ->
            SavedChannelEntity(
                historyId = historyId,
                channelId = channel.channelId,
                channelName = channel.channelName,
                channelIconUrl = channel.channelIconUrl,
                serviceType = channel.serviceType.name,
            )
        }
    }

    /**
     * 新しい履歴IDを生成する。
     *
     * @return UUID形式の新しいID
     */
    @OptIn(ExperimentalUuidApi::class)
    fun generateId(): String = Uuid.random().toString()
}
