package org.example.project.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.example.project.data.local.UserDeviceDao
import org.example.project.data.local.entity.UserDeviceEntity

/**
 * UserRepositoryImplのテスト。
 *
 * Story Issue: US-2（匿名認証 - デバイスID）
 * Epic: サブスクリプション基盤
 */
class UserRepositoryImplTest {

    // ========================================
    // デバイスID取得
    // ========================================

    @Test
    fun `デバイスID取得_初回呼び出しで新しいUUIDを生成すること`() = runTest {
        // Arrange
        val dao = FakeUserDeviceDao()
        val repository = UserRepositoryImpl(dao)

        // Act
        val deviceId = repository.getDeviceId()

        // Assert
        assertNotNull(deviceId)
        // UUID v4形式の検証（8-4-4-4-12のハイフン区切り）
        assertTrue(deviceId.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
    }

    @Test
    fun `デバイスID取得_2回目以降は同じIDを返すこと`() = runTest {
        // Arrange
        val dao = FakeUserDeviceDao()
        val repository = UserRepositoryImpl(dao)

        // Act
        val deviceId1 = repository.getDeviceId()
        val deviceId2 = repository.getDeviceId()
        val deviceId3 = repository.getDeviceId()

        // Assert
        assertEquals(deviceId1, deviceId2)
        assertEquals(deviceId2, deviceId3)
    }

    @Test
    fun `デバイスID取得_初回呼び出しでDBに保存されること`() = runTest {
        // Arrange
        val dao = FakeUserDeviceDao()
        val repository = UserRepositoryImpl(dao)

        // Act
        val deviceId = repository.getDeviceId()

        // Assert
        assertEquals(deviceId, dao.getDeviceId())
    }

    // ========================================
    // デバイスID存在確認
    // ========================================

    @Test
    fun `デバイスID存在確認_未生成時はfalseを返すこと`() = runTest {
        // Arrange
        val dao = FakeUserDeviceDao()
        val repository = UserRepositoryImpl(dao)

        // Act
        val hasDeviceId = repository.hasDeviceId()

        // Assert
        assertFalse(hasDeviceId)
    }

    @Test
    fun `デバイスID存在確認_生成後はtrueを返すこと`() = runTest {
        // Arrange
        val dao = FakeUserDeviceDao()
        val repository = UserRepositoryImpl(dao)

        // Act
        repository.getDeviceId() // デバイスIDを生成
        val hasDeviceId = repository.hasDeviceId()

        // Assert
        assertTrue(hasDeviceId)
    }

    // ========================================
    // エッジケース
    // ========================================

    @Test
    fun `複数回の初回呼び出し_同じIDが返されること`() = runTest {
        // Arrange
        val dao = FakeUserDeviceDao()
        val repository = UserRepositoryImpl(dao)

        // Act - 複数のgetDeviceId()呼び出しをシミュレート
        val deviceId1 = repository.getDeviceId()
        val deviceId2 = repository.getDeviceId()

        // Assert
        assertEquals(deviceId1, deviceId2)
        // DAOには1回のみ保存される（REPLACE動作により重複しない）
        val savedId = dao.getDeviceId()
        assertEquals(deviceId1, savedId)
    }
}

/**
 * テスト用のFake DAO実装。
 *
 * インメモリでデバイスIDを保持する。
 */
private class FakeUserDeviceDao : UserDeviceDao {
    private var deviceId: String? = null

    override suspend fun getDeviceId(): String? {
        return deviceId
    }

    override suspend fun insert(entity: UserDeviceEntity) {
        // REPLACE動作：常に上書き
        deviceId = entity.deviceId
    }
}
