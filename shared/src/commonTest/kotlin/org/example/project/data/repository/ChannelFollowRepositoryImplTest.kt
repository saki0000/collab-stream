package org.example.project.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.example.project.data.local.FollowedChannelDao
import org.example.project.data.local.entity.FollowedChannelEntity
import org.example.project.domain.model.VideoServiceType

/**
 * ChannelFollowRepositoryImplのテスト。
 *
 * Story Issue: US-1（チャンネルフォロー データ層）
 * Specification: feature/channel_follow/SPECIFICATION.md
 */
@OptIn(ExperimentalTime::class)
class ChannelFollowRepositoryImplTest {

    // ========================================
    // フォロー操作
    // ========================================

    @Test
    fun `フォロー操作_新規チャンネルをフォローできること`() = runTest {
        // Arrange
        val dao = FakeFollowedChannelDao()
        val repository = ChannelFollowRepositoryImpl(dao)

        // Act
        val result = repository.follow(
            channelId = "channel1",
            channelName = "Test Channel",
            channelIconUrl = "https://example.com/icon.png",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Assert
        assertTrue(result.isSuccess)
        val followedChannel = result.getOrThrow()
        assertEquals("channel1", followedChannel.channelId)
        assertEquals("Test Channel", followedChannel.channelName)
        assertEquals("https://example.com/icon.png", followedChannel.channelIconUrl)
        assertEquals(VideoServiceType.YOUTUBE, followedChannel.serviceType)
    }

    @Test
    fun `フォロー操作_既存フォローを再度フォローしても正常終了すること_冪等性`() = runTest {
        // Arrange
        val dao = FakeFollowedChannelDao()
        val repository = ChannelFollowRepositoryImpl(dao)

        // 初回フォロー
        repository.follow(
            channelId = "channel1",
            channelName = "Test Channel",
            channelIconUrl = "https://example.com/icon.png",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Act - 同じチャンネルを再フォロー
        val result = repository.follow(
            channelId = "channel1",
            channelName = "Test Channel Updated",
            channelIconUrl = "https://example.com/icon2.png",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Assert
        assertTrue(result.isSuccess)
        val allChannels = dao.getAll()
        assertEquals(1, allChannels.size)
        assertEquals("Test Channel Updated", allChannels[0].channelName)
    }

    // ========================================
    // アンフォロー操作
    // ========================================

    @Test
    fun `アンフォロー操作_フォロー済みチャンネルをアンフォローできること`() = runTest {
        // Arrange
        val dao = FakeFollowedChannelDao()
        val repository = ChannelFollowRepositoryImpl(dao)

        repository.follow(
            channelId = "channel1",
            channelName = "Test Channel",
            channelIconUrl = "https://example.com/icon.png",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Act
        val result = repository.unfollow(
            channelId = "channel1",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(dao.isFollowing("channel1", VideoServiceType.YOUTUBE))
    }

    @Test
    fun `アンフォロー操作_存在しないチャンネルをアンフォローしても正常終了すること_冪等性`() = runTest {
        // Arrange
        val dao = FakeFollowedChannelDao()
        val repository = ChannelFollowRepositoryImpl(dao)

        // Act - 存在しないチャンネルをアンフォロー
        val result = repository.unfollow(
            channelId = "nonexistent",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Assert
        assertTrue(result.isSuccess)
    }

    // ========================================
    // フォロー状態確認
    // ========================================

    @Test
    fun `フォロー状態確認_フォロー済みチャンネルはtrueを返すこと`() = runTest {
        // Arrange
        val dao = FakeFollowedChannelDao()
        val repository = ChannelFollowRepositoryImpl(dao)

        repository.follow(
            channelId = "channel1",
            channelName = "Test Channel",
            channelIconUrl = "https://example.com/icon.png",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Act
        val isFollowing = repository.isFollowing("channel1", VideoServiceType.YOUTUBE)

        // Assert
        assertTrue(isFollowing)
    }

    @Test
    fun `フォロー状態確認_未フォローチャンネルはfalseを返すこと`() = runTest {
        // Arrange
        val dao = FakeFollowedChannelDao()
        val repository = ChannelFollowRepositoryImpl(dao)

        // Act
        val isFollowing = repository.isFollowing("channel1", VideoServiceType.YOUTUBE)

        // Assert
        assertFalse(isFollowing)
    }

    @Test
    fun `フォロー状態確認_同じチャンネルIDでも異なるserviceTypeは別扱いとなること`() = runTest {
        // Arrange
        val dao = FakeFollowedChannelDao()
        val repository = ChannelFollowRepositoryImpl(dao)

        repository.follow(
            channelId = "channel1",
            channelName = "Test Channel",
            channelIconUrl = "https://example.com/icon.png",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Act
        val isFollowingYoutube = repository.isFollowing("channel1", VideoServiceType.YOUTUBE)
        val isFollowingTwitch = repository.isFollowing("channel1", VideoServiceType.TWITCH)

        // Assert
        assertTrue(isFollowingYoutube)
        assertFalse(isFollowingTwitch)
    }

    // ========================================
    // フォロー一覧取得
    // ========================================

    @Test
    fun `フォロー一覧取得_フォロー日時の降順でソートされること`() = runTest {
        // Arrange
        val dao = FakeFollowedChannelDao()
        val repository = ChannelFollowRepositoryImpl(dao)

        // 3つのチャンネルを順にフォロー
        repository.follow(
            channelId = "channel1",
            channelName = "Channel 1",
            channelIconUrl = "https://example.com/1.png",
            serviceType = VideoServiceType.YOUTUBE,
        )

        repository.follow(
            channelId = "channel2",
            channelName = "Channel 2",
            channelIconUrl = "https://example.com/2.png",
            serviceType = VideoServiceType.YOUTUBE,
        )

        repository.follow(
            channelId = "channel3",
            channelName = "Channel 3",
            channelIconUrl = "https://example.com/3.png",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Act
        val result = repository.getAllFollowedChannels()

        // Assert
        assertTrue(result.isSuccess)
        val channels = result.getOrThrow()
        assertEquals(3, channels.size)
        // 新しいフォローが先頭（降順）であることを確認
        assertTrue(channels[0].followedAt >= channels[1].followedAt)
        assertTrue(channels[1].followedAt >= channels[2].followedAt)
    }

    @Test
    fun `フォロー一覧取得_フォローがない場合は空リストを返すこと`() = runTest {
        // Arrange
        val dao = FakeFollowedChannelDao()
        val repository = ChannelFollowRepositoryImpl(dao)

        // Act
        val result = repository.getAllFollowedChannels()

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    // ========================================
    // フォロー一覧監視（Flow）
    // ========================================

    @Test
    fun `フォロー一覧監視_フォロー追加でFlowが更新されること`() = runTest {
        // Arrange
        val dao = FakeFollowedChannelDao()
        val repository = ChannelFollowRepositoryImpl(dao)

        // Act - 初期状態を確認
        val initialList = repository.observeFollowedChannels().first()
        assertEquals(0, initialList.size)

        // フォロー追加
        repository.follow(
            channelId = "channel1",
            channelName = "Test Channel",
            channelIconUrl = "https://example.com/icon.png",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Assert - 追加後のFlowを確認
        val updatedList = repository.observeFollowedChannels().first()
        assertEquals(1, updatedList.size)
        assertEquals("channel1", updatedList[0].channelId)
    }

    @Test
    fun `フォロー一覧監視_アンフォローでFlowが更新されること`() = runTest {
        // Arrange
        val dao = FakeFollowedChannelDao()
        val repository = ChannelFollowRepositoryImpl(dao)

        repository.follow(
            channelId = "channel1",
            channelName = "Test Channel",
            channelIconUrl = "https://example.com/icon.png",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Act - アンフォロー
        repository.unfollow("channel1", VideoServiceType.YOUTUBE)

        // Assert - 削除後のFlowを確認
        val updatedList = repository.observeFollowedChannels().first()
        assertTrue(updatedList.isEmpty())
    }
}

/**
 * テスト用のFake DAO実装。
 *
 * MutableStateFlowを使用してリアクティブな更新をシミュレートする。
 */
private class FakeFollowedChannelDao : FollowedChannelDao {
    private val channelsFlow = MutableStateFlow<List<FollowedChannelEntity>>(emptyList())

    override suspend fun insert(channel: FollowedChannelEntity) {
        // REPLACE動作をシミュレート：既存があれば削除して追加
        val current = channelsFlow.value.toMutableList()
        current.removeAll { it.channelId == channel.channelId && it.serviceType == channel.serviceType }
        current.add(channel)
        channelsFlow.value = current
    }

    override suspend fun delete(channelId: String, serviceType: VideoServiceType) {
        channelsFlow.value = channelsFlow.value.filterNot {
            it.channelId == channelId && it.serviceType == serviceType
        }
    }

    override suspend fun isFollowing(channelId: String, serviceType: VideoServiceType): Boolean {
        return channelsFlow.value.any { it.channelId == channelId && it.serviceType == serviceType }
    }

    override suspend fun getAll(): List<FollowedChannelEntity> {
        return channelsFlow.value.sortedByDescending { it.followedAt }
    }

    override fun observeAll(): Flow<List<FollowedChannelEntity>> {
        return channelsFlow.map { list -> list.sortedByDescending { it.followedAt } }
    }
}
