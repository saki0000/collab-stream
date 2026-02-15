package org.example.project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.example.project.data.local.entity.UserDeviceEntity

/**
 * ユーザーデバイス情報のDAO。
 *
 * 単一行テーブル（id = 1固定）からデバイスIDを取得・保存する。
 *
 * Story Issue: US-2（匿名認証 - デバイスID）
 * Epic: サブスクリプション基盤
 */
@Dao
interface UserDeviceDao {
    /**
     * デバイスIDを取得する。
     *
     * 未登録の場合はnullを返す。
     *
     * @return デバイスID（UUID v4形式）、未登録の場合はnull
     */
    @Query("SELECT deviceId FROM user_device WHERE id = 1")
    suspend fun getDeviceId(): String?

    /**
     * デバイスIDを保存する。
     *
     * 既存レコードがある場合はREPLACEされる（冪等性を保証）。
     *
     * @param entity デバイスエンティティ（id = 1固定）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UserDeviceEntity)
}
