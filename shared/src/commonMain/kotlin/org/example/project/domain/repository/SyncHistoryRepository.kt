package org.example.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncHistory

/**
 * 同期履歴のローカル永続化を担当するRepository。
 *
 * Room KMPによる実装を想定し、同期チャンネル組み合わせの
 * 保存、取得、削除、更新を提供する。
 *
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 */
interface SyncHistoryRepository {

    /**
     * 現在の同期チャンネル組み合わせを履歴として保存する。
     *
     * @param channels 保存するチャンネルリスト（最小2つ）
     * @param name オプションの履歴名（nullの場合は自動生成）
     * @return 保存された履歴のResult
     * @throws IllegalArgumentException チャンネル数が2未満の場合
     */
    suspend fun saveHistory(
        channels: List<SyncChannel>,
        name: String? = null,
    ): Result<SyncHistory>

    /**
     * すべての履歴を取得する。
     *
     * @param sortBy ソート順
     * @return 履歴リストのResult
     */
    suspend fun getAllHistories(
        sortBy: HistorySortOrder = HistorySortOrder.LAST_USED,
    ): Result<List<SyncHistory>>

    /**
     * IDで履歴を取得する。
     *
     * @param historyId 取得する履歴のID
     * @return 履歴のResult（見つからない場合はnull）
     */
    suspend fun getHistoryById(historyId: String): Result<SyncHistory?>

    /**
     * 特定の履歴を削除する。
     *
     * @param historyId 削除する履歴のID
     * @return 削除成功のResult
     */
    suspend fun deleteHistory(historyId: String): Result<Unit>

    /**
     * 履歴の使用回数と最終使用日時を更新する。
     *
     * 履歴から再同期を実行した際に呼び出される。
     * usageCountをインクリメントし、lastUsedAtを現在時刻に更新する。
     *
     * @param historyId 更新する履歴のID
     * @return 更新成功のResult
     */
    suspend fun recordUsage(historyId: String): Result<Unit>

    /**
     * 履歴名を更新する。
     *
     * @param historyId 更新する履歴のID
     * @param newName 新しい名前（nullで自動生成名に戻す）
     * @return 更新成功のResult
     */
    suspend fun updateHistoryName(
        historyId: String,
        newName: String?,
    ): Result<Unit>

    /**
     * 履歴の変更をFlowとして監視する。
     *
     * UIでのリアルタイム更新に使用。
     * 履歴の追加、削除、更新時に新しいリストが発行される。
     *
     * @param sortBy ソート順
     * @return 履歴リストのFlow
     */
    fun observeHistories(
        sortBy: HistorySortOrder = HistorySortOrder.LAST_USED,
    ): Flow<List<SyncHistory>>
}

/**
 * 履歴のソート順を定義するEnum。
 */
enum class HistorySortOrder {
    /**
     * 最終使用日時の降順（最近使用したものが先頭）。
     */
    LAST_USED,

    /**
     * 作成日時の降順（新しいものが先頭）。
     */
    CREATED,

    /**
     * 使用回数の降順（よく使うものが先頭）。
     */
    MOST_USED,
}
