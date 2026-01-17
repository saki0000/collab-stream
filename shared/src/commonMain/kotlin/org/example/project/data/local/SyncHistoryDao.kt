package org.example.project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.example.project.data.local.entity.SavedChannelEntity
import org.example.project.data.local.entity.SyncHistoryEntity
import org.example.project.data.local.entity.SyncHistoryWithChannels

/**
 * 同期履歴のData Access Object。
 *
 * 同期履歴とそれに紐づくチャンネル情報のCRUD操作を提供する。
 *
 * Story Issue: #36
 * Epic: EPIC-003（同期チャンネル履歴保存）
 */
@Dao
interface SyncHistoryDao {

    // ===================
    // Insert Operations
    // ===================

    /**
     * 履歴を挿入する。同一IDが存在する場合は置換する。
     *
     * @param history 挿入する履歴エンティティ
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SyncHistoryEntity)

    /**
     * チャンネル情報を挿入する。
     *
     * @param channels 挿入するチャンネルエンティティのリスト
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<SavedChannelEntity>)

    /**
     * 履歴とチャンネルをアトミックに保存する。
     *
     * insert()とinsertChannels()を別々に呼ぶと不整合リスクがあるため、
     * このトランザクションメソッドを使用することを推奨。
     *
     * @param history 挿入する履歴エンティティ
     * @param channels 挿入するチャンネルエンティティのリスト
     */
    @Transaction
    suspend fun insertHistoryWithChannels(
        history: SyncHistoryEntity,
        channels: List<SavedChannelEntity>,
    ) {
        insert(history)
        insertChannels(channels)
    }

    // ===================
    // Query Operations
    // ===================

    /**
     * 最終使用日時の降順で履歴を監視する。
     *
     * @return 履歴リストのFlow
     */
    @Transaction
    @Query("SELECT * FROM sync_history ORDER BY lastUsedAt DESC")
    fun observeAllByLastUsed(): Flow<List<SyncHistoryWithChannels>>

    /**
     * 作成日時の降順で履歴を監視する。
     *
     * @return 履歴リストのFlow
     */
    @Transaction
    @Query("SELECT * FROM sync_history ORDER BY createdAt DESC")
    fun observeAllByCreated(): Flow<List<SyncHistoryWithChannels>>

    /**
     * 使用回数の降順で履歴を監視する。
     *
     * @return 履歴リストのFlow
     */
    @Transaction
    @Query("SELECT * FROM sync_history ORDER BY usageCount DESC")
    fun observeAllByMostUsed(): Flow<List<SyncHistoryWithChannels>>

    /**
     * IDで履歴を取得する。
     *
     * @param id 取得する履歴のID
     * @return 履歴とチャンネル情報（見つからない場合はnull）
     */
    @Transaction
    @Query("SELECT * FROM sync_history WHERE id = :id")
    suspend fun getById(id: String): SyncHistoryWithChannels?

    /**
     * すべての履歴を最終使用日時の降順で取得する。
     *
     * @return 履歴とチャンネル情報のリスト
     */
    @Transaction
    @Query("SELECT * FROM sync_history ORDER BY lastUsedAt DESC")
    suspend fun getAllByLastUsed(): List<SyncHistoryWithChannels>

    /**
     * すべての履歴を作成日時の降順で取得する。
     *
     * @return 履歴とチャンネル情報のリスト
     */
    @Transaction
    @Query("SELECT * FROM sync_history ORDER BY createdAt DESC")
    suspend fun getAllByCreated(): List<SyncHistoryWithChannels>

    /**
     * すべての履歴を使用回数の降順で取得する。
     *
     * @return 履歴とチャンネル情報のリスト
     */
    @Transaction
    @Query("SELECT * FROM sync_history ORDER BY usageCount DESC")
    suspend fun getAllByMostUsed(): List<SyncHistoryWithChannels>

    // ===================
    // Update Operations
    // ===================

    /**
     * 履歴の使用回数と最終使用日時を更新する。
     *
     * @param id 更新する履歴のID
     * @param timestamp 新しい最終使用日時（epochMillis）
     */
    @Query("UPDATE sync_history SET usageCount = usageCount + 1, lastUsedAt = :timestamp WHERE id = :id")
    suspend fun recordUsage(id: String, timestamp: Long)

    /**
     * 履歴名を更新する。
     *
     * @param id 更新する履歴のID
     * @param name 新しい名前（nullで自動生成名に戻す）
     */
    @Query("UPDATE sync_history SET name = :name WHERE id = :id")
    suspend fun updateName(id: String, name: String?)

    // ===================
    // Delete Operations
    // ===================

    /**
     * IDで履歴を削除する。
     * ForeignKey CASCADEにより関連するチャンネルも自動削除される。
     *
     * @param id 削除する履歴のID
     */
    @Query("DELETE FROM sync_history WHERE id = :id")
    suspend fun deleteById(id: String)
}
