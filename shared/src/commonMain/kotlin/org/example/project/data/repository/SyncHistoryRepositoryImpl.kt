package org.example.project.data.repository

import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.data.local.SyncHistoryDao
import org.example.project.data.local.SyncHistoryMapper
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncHistory
import org.example.project.domain.repository.HistorySortOrder
import org.example.project.domain.repository.SyncHistoryRepository

/**
 * SyncHistoryRepositoryのRoom実装。
 *
 * SyncHistoryDaoを使用してローカルデータベースに履歴を永続化する。
 *
 * Story Issue: #36
 * Epic: EPIC-003（同期チャンネル履歴保存）
 */
@OptIn(ExperimentalTime::class)
class SyncHistoryRepositoryImpl(
    private val dao: SyncHistoryDao,
) : SyncHistoryRepository {

    override suspend fun saveHistory(
        channels: List<SyncChannel>,
        name: String?,
    ): Result<SyncHistory> = runCatching {
        // ビジネスルール: 最小2チャンネル必要
        require(channels.size >= MIN_CHANNELS) {
            "履歴の保存には最低${MIN_CHANNELS}つのチャンネルが必要です"
        }

        val now = kotlin.time.Clock.System.now()
        val id = SyncHistoryMapper.generateId()

        val historyEntity = SyncHistoryMapper.toHistoryEntity(
            id = id,
            name = name,
            createdAt = now,
            lastUsedAt = now,
            usageCount = 0,
        )
        val channelEntities = SyncHistoryMapper.toChannelEntities(channels, id)

        dao.insertHistoryWithChannels(historyEntity, channelEntities)

        // 保存した履歴を返す
        val saved = dao.getById(id)
            ?: error("保存した履歴が見つかりません: $id")

        SyncHistoryMapper.toDomain(saved)
    }

    override suspend fun getAllHistories(
        sortBy: HistorySortOrder,
    ): Result<List<SyncHistory>> = runCatching {
        val entities = when (sortBy) {
            HistorySortOrder.LAST_USED -> dao.getAllByLastUsed()
            HistorySortOrder.CREATED -> dao.getAllByCreated()
            HistorySortOrder.MOST_USED -> dao.getAllByMostUsed()
        }
        entities.map { SyncHistoryMapper.toDomain(it) }
    }

    override suspend fun getHistoryById(historyId: String): Result<SyncHistory?> = runCatching {
        dao.getById(historyId)?.let { SyncHistoryMapper.toDomain(it) }
    }

    override suspend fun deleteHistory(historyId: String): Result<Unit> = runCatching {
        dao.deleteById(historyId)
    }

    override suspend fun recordUsage(historyId: String): Result<Unit> = runCatching {
        val timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds()
        dao.recordUsage(historyId, timestamp)
    }

    override suspend fun updateHistoryName(
        historyId: String,
        newName: String?,
    ): Result<Unit> = runCatching {
        dao.updateName(historyId, newName)
    }

    override fun observeHistories(
        sortBy: HistorySortOrder,
    ): Flow<List<SyncHistory>> {
        val flow = when (sortBy) {
            HistorySortOrder.LAST_USED -> dao.observeAllByLastUsed()
            HistorySortOrder.CREATED -> dao.observeAllByCreated()
            HistorySortOrder.MOST_USED -> dao.observeAllByMostUsed()
        }
        return flow.map { entities ->
            entities.map { SyncHistoryMapper.toDomain(it) }
        }
    }

    companion object {
        private const val MIN_CHANNELS = 2
    }
}
