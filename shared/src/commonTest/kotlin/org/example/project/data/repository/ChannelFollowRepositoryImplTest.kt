package org.example.project.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
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
        assertEquals(1, dao.channels.size)
        assertEquals("Test Channel Updated", dao.channels[0].channelName)
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
        // ただし、同じミリ秒内でフォローされる可能性があるため、
        // followedAtが単調増加していることのみ確認
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
}

/**
 * テスト用のFake DAO実装。
 *
 * インメモリでデータを管理し、実際のRoomの動作をシミュレートする。
 */
private class FakeFollowedChannelDao : FollowedChannelDao {
    val channels = mutableListOf<FollowedChannelEntity>()

    override suspend fun insert(channel: FollowedChannelEntity) {
        // REPLACE動作をシミュレート：既存があれば削除して追加
        channels.removeAll { it.channelId == channel.channelId && it.serviceType == channel.serviceType }
        channels.add(channel)
    }

    override suspend fun delete(channelId: String, serviceType: VideoServiceType) {
        channels.removeAll { it.channelId == channelId && it.serviceType == serviceType }
    }

    override suspend fun isFollowing(channelId: String, serviceType: VideoServiceType): Boolean {
        return channels.any { it.channelId == channelId && it.serviceType == serviceType }
    }

    override suspend fun getAll(): List<FollowedChannelEntity> {
        // followedAtの降順でソート
        return channels.sortedByDescending { it.followedAt }
    }

    override fun observeAll(): kotlinx.coroutines.flow.Flow<List<FollowedChannelEntity>> {
        // Flow監視は今回のテストでは使用しないため、シンプルに現在のリストを返す
        // followedAtの降順でソート
        return kotlinx.coroutines.flow.flowOf(channels.sortedByDescending { it.followedAt })
    }
}
