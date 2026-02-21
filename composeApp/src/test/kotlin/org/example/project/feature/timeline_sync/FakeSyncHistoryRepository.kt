@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.feature.timeline_sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.domain.model.SavedChannelInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncHistory
import org.example.project.domain.model.toSavedChannelInfo
import org.example.project.domain.repository.HistorySortOrder
import org.example.project.domain.repository.SyncHistoryRepository

/**
 * テスト用 SyncHistoryRepository のフェイク実装。
 *
 * デフォルトでは正常ケース（成功）を返す。
 * テストケースごとにフィールドを設定してエラーシミュレーションが可能。
 *
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 * Story: US-2 (履歴保存機能)
 */
class FakeSyncHistoryRepository : SyncHistoryRepository {

    private val histories = mutableListOf<SyncHistory>()
    private val _flow = MutableStateFlow<List<SyncHistory>>(emptyList())

    /** trueに設定するとsaveHistoryがResult.failureを返す */
    var shouldFailSave = false

    /** trueに設定するとgetAllHistoriesがResult.failureを返す */
    var shouldFailGetAll = false

    override suspend fun saveHistory(
        channels: List<SyncChannel>,
        name: String?,
    ): Result<SyncHistory> {
        if (shouldFailSave) {
            return Result.failure(Exception("保存に失敗しました（テスト用）"))
        }
        if (channels.size < 2) {
            return Result.failure(IllegalArgumentException("チャンネル数が2未満です"))
        }

        val now = kotlin.time.Clock.System.now()
        val history = SyncHistory(
            id = "fake-history-${histories.size + 1}",
            name = name,
            channels = channels.map { it.toSavedChannelInfo() },
            createdAt = now,
            lastUsedAt = now,
            usageCount = 0,
        )
        histories.add(history)
        _flow.value = histories.toList()
        return Result.success(history)
    }

    override suspend fun getAllHistories(
        sortBy: HistorySortOrder,
    ): Result<List<SyncHistory>> {
        if (shouldFailGetAll) {
            return Result.failure(Exception("取得に失敗しました（テスト用）"))
        }
        return Result.success(histories.toList())
    }

    override suspend fun getHistoryById(historyId: String): Result<SyncHistory?> {
        return Result.success(histories.find { it.id == historyId })
    }

    override suspend fun deleteHistory(historyId: String): Result<Unit> {
        histories.removeAll { it.id == historyId }
        _flow.value = histories.toList()
        return Result.success(Unit)
    }

    override suspend fun recordUsage(historyId: String): Result<Unit> {
        val index = histories.indexOfFirst { it.id == historyId }
        if (index >= 0) {
            val history = histories[index]
            histories[index] = history.copy(
                usageCount = history.usageCount + 1,
                lastUsedAt = kotlin.time.Clock.System.now(),
            )
            _flow.value = histories.toList()
        }
        return Result.success(Unit)
    }

    override suspend fun updateHistoryName(historyId: String, newName: String?): Result<Unit> {
        val index = histories.indexOfFirst { it.id == historyId }
        if (index >= 0) {
            histories[index] = histories[index].copy(name = newName)
            _flow.value = histories.toList()
        }
        return Result.success(Unit)
    }

    override fun observeHistories(sortBy: HistorySortOrder): Flow<List<SyncHistory>> {
        return _flow
    }

    /** テスト用: 保存されている履歴数を取得する */
    fun getHistoryCount(): Int = histories.size

    /** テスト用: 最後に保存された履歴を取得する */
    fun getLastSavedHistory(): SyncHistory? = histories.lastOrNull()

    /** テスト用: すべての履歴をリセットする */
    fun reset() {
        histories.clear()
        _flow.value = emptyList()
        shouldFailSave = false
        shouldFailGetAll = false
    }
}
