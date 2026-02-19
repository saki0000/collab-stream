package org.example.project.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SearchQuery
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.VideoSearchRepository
import org.example.project.runTest

/**
 * マルチプラットフォームチャンネル検索のテスト
 * Specification: feature/timeline_sync/channel_add/SPECIFICATION.md
 * Story Issue: #46 (US-2), #69 (US-5)
 *
 * 注: ADR-005 Phase 2 移行後、サーバーAPI経由での実装に対応
 */
class ChannelSearchUseCaseTest {

    // ========================================
    // テスト用モック Repository
    // ========================================

    private class FakeVideoSearchRepository(
        private val channelsResult: Result<List<ChannelInfo>> = Result.success(emptyList()),
    ) : VideoSearchRepository {
        var lastQuery: String? = null
        var lastServiceType: VideoServiceType? = null
        var lastMaxResults: Int? = null

        override suspend fun searchChannels(
            query: String,
            serviceType: VideoServiceType,
            maxResults: Int,
        ): Result<List<ChannelInfo>> {
            lastQuery = query
            lastServiceType = serviceType
            lastMaxResults = maxResults
            return channelsResult
        }

        override suspend fun searchVideos(searchQuery: SearchQuery): Result<SearchResponse> {
            return Result.failure(NotImplementedError())
        }

        override suspend fun searchVideosByService(
            searchQuery: SearchQuery,
            serviceType: VideoServiceType,
        ): Result<SearchResponse> {
            return Result.failure(NotImplementedError())
        }
    }

    // ========================================
    // searchChannels - Twitch プラットフォーム
    // ========================================

    @Test
    fun `Twitch検索_正常なクエリで検索結果が返されること`() = runTest {
        // Arrange
        val repository = FakeVideoSearchRepository(
            channelsResult = Result.success(
                listOf(
                    ChannelInfo(
                        id = "twitch_1",
                        displayName = "TestStreamer",
                        thumbnailUrl = "https://example.com/thumb.jpg",
                        broadcasterLanguage = "ja",
                        gameId = "game_1",
                        gameName = "Minecraft",
                        serviceType = VideoServiceType.TWITCH,
                    ),
                ),
            ),
        )
        val useCase = ChannelSearchUseCase(repository)

        // Act
        val result = useCase.searchChannels("test", VideoServiceType.TWITCH)

        // Assert
        assertTrue(result.isSuccess)
        val channels = result.getOrThrow()
        assertEquals(1, channels.size)
        assertEquals("twitch_1", channels[0].id)
        assertEquals("TestStreamer", channels[0].displayName)
        assertEquals(VideoServiceType.TWITCH, channels[0].serviceType)
    }

    @Test
    fun `Twitch検索_Repositoryに正しいパラメータが渡されること`() = runTest {
        // Arrange
        val repository = FakeVideoSearchRepository()
        val useCase = ChannelSearchUseCase(repository)

        // Act
        useCase.searchChannels("streamer name", VideoServiceType.TWITCH, maxResults = 3)

        // Assert
        assertEquals("streamer name", repository.lastQuery)
        assertEquals(VideoServiceType.TWITCH, repository.lastServiceType)
        assertEquals(3, repository.lastMaxResults)
    }

    // ========================================
    // searchChannels - YouTube プラットフォーム
    // ========================================

    @Test
    fun `YouTube検索_正常なクエリで検索結果が返されること`() = runTest {
        // Arrange
        val repository = FakeVideoSearchRepository(
            channelsResult = Result.success(
                listOf(
                    ChannelInfo(
                        id = "UC_youtube_1",
                        displayName = "YouTubeChannel",
                        thumbnailUrl = "https://example.com/yt_thumb.jpg",
                        serviceType = VideoServiceType.YOUTUBE,
                    ),
                ),
            ),
        )
        val useCase = ChannelSearchUseCase(repository)

        // Act
        val result = useCase.searchChannels("test", VideoServiceType.YOUTUBE)

        // Assert
        assertTrue(result.isSuccess)
        val channels = result.getOrThrow()
        assertEquals(1, channels.size)
        assertEquals("UC_youtube_1", channels[0].id)
        assertEquals("YouTubeChannel", channels[0].displayName)
        assertEquals(VideoServiceType.YOUTUBE, channels[0].serviceType)
        assertEquals("https://example.com/yt_thumb.jpg", channels[0].thumbnailUrl)
    }

    @Test
    fun `YouTube検索_Repositoryに正しいパラメータが渡されること`() = runTest {
        // Arrange
        val repository = FakeVideoSearchRepository()
        val useCase = ChannelSearchUseCase(repository)

        // Act
        useCase.searchChannels("channel name", VideoServiceType.YOUTUBE, maxResults = 5)

        // Assert
        assertEquals("channel name", repository.lastQuery)
        assertEquals(VideoServiceType.YOUTUBE, repository.lastServiceType)
        assertEquals(5, repository.lastMaxResults)
    }

    // ========================================
    // 共通バリデーション
    // ========================================

    @Test
    fun `空クエリ_Twitch検索でエラーが返されること`() = runTest {
        // Arrange
        val repository = FakeVideoSearchRepository(
            channelsResult = Result.failure(IllegalArgumentException("Search query cannot be empty")),
        )
        val useCase = ChannelSearchUseCase(repository)

        // Act
        val result = useCase.searchChannels("", VideoServiceType.TWITCH)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `空白のみクエリ_YouTube検索でエラーが返されること`() = runTest {
        // Arrange
        val repository = FakeVideoSearchRepository(
            channelsResult = Result.failure(IllegalArgumentException("Search query cannot be empty")),
        )
        val useCase = ChannelSearchUseCase(repository)

        // Act
        val result = useCase.searchChannels("   ", VideoServiceType.YOUTUBE)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    // ========================================
    // エラーハンドリング
    // ========================================

    @Test
    fun `Twitch検索エラー_エラーResultが返されること`() = runTest {
        // Arrange
        val repository = FakeVideoSearchRepository(
            channelsResult = Result.failure(RuntimeException("Twitch API error")),
        )
        val useCase = ChannelSearchUseCase(repository)

        // Act
        val result = useCase.searchChannels("test", VideoServiceType.TWITCH)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `YouTube検索エラー_エラーResultが返されること`() = runTest {
        // Arrange
        val repository = FakeVideoSearchRepository(
            channelsResult = Result.failure(RuntimeException("YouTube API error")),
        )
        val useCase = ChannelSearchUseCase(repository)

        // Act
        val result = useCase.searchChannels("test", VideoServiceType.YOUTUBE)

        // Assert
        assertTrue(result.isFailure)
    }

    // ========================================
    // 後方互換性
    // ========================================

    @Test
    fun `searchTwitchChannels_後方互換性メソッドが動作すること`() = runTest {
        // Arrange
        val repository = FakeVideoSearchRepository(
            channelsResult = Result.success(
                listOf(
                    ChannelInfo(
                        id = "t1",
                        displayName = "Streamer1",
                        serviceType = VideoServiceType.TWITCH,
                    ),
                ),
            ),
        )
        val useCase = ChannelSearchUseCase(repository)

        // Act
        val result = useCase.searchTwitchChannels("test")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals(VideoServiceType.TWITCH, result.getOrThrow()[0].serviceType)
    }
}
