package org.example.project.data.repository

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.project.data.local.UserDeviceDao
import org.example.project.data.local.entity.UserDeviceEntity
import org.example.project.domain.repository.UserRepository

/**
 * UserRepositoryの実装。
 *
 * デバイスIDの生成と永続化を担当する。
 * 初回呼び出し時にUUID v4を生成し、Room DBに保存する。
 *
 * Story Issue: US-2（匿名認証 - デバイスID）
 * Epic: サブスクリプション基盤
 */
@OptIn(ExperimentalUuidApi::class)
class UserRepositoryImpl(
    private val userDeviceDao: UserDeviceDao,
) : UserRepository {

    private val mutex = Mutex()

    /**
     * デバイスIDを取得する。
     *
     * 未生成の場合は新しいUUID v4を生成・永続化して返す。
     * Mutexにより、複数の同時呼び出しでも競合状態なく安全に処理される。
     *
     * @return デバイスID（UUID v4形式の文字列）
     */
    override suspend fun getDeviceId(): String {
        // 既存のデバイスIDがあれば即時返す（ロックのオーバーヘッドを避ける）
        userDeviceDao.getDeviceId()?.let { return it }

        return mutex.withLock {
            // ロック取得後、再度チェック（Double-Checked Locking）
            val existingId = userDeviceDao.getDeviceId()
            if (existingId != null) {
                return@withLock existingId
            }

            // 新しいUUID v4を生成
            val newDeviceId = Uuid.random().toString()

            // DBに保存
            userDeviceDao.insert(
                UserDeviceEntity(
                    id = 1,
                    deviceId = newDeviceId,
                ),
            )

            newDeviceId
        }
    }

    /**
     * デバイスIDが既に生成・保存済みかどうかを確認する。
     *
     * @return 保存済みならtrue
     */
    override suspend fun hasDeviceId(): Boolean {
        return userDeviceDao.getDeviceId() != null
    }
}
