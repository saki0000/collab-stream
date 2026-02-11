package org.example.project.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.example.project.data.datasource.TwitchSearchDataSource
import org.example.project.data.datasource.YouTubeSearchDataSource
import org.example.project.data.model.TwitchSearchResponse
import org.example.project.data.model.TwitchUser
import org.example.project.data.model.TwitchUserResponse
import org.example.project.data.model.YouTubeChannelSearchId
import org.example.project.data.model.YouTubeChannelSearchItem
import org.example.project.data.model.YouTubeChannelSearchResponse
import org.example.project.data.model.YouTubeChannelSnippet
import org.example.project.data.model.YouTubeSearchResponse
import org.example.project.data.model.YouTubeThumbnail
import org.example.project.data.model.YouTubeThumbnails
import org.example.project.domain.model.SearchQuery
import org.example.project.domain.model.VideoServiceType
import org.example.project.runTest

/**
 * マルチプラットフォームチャンネル検索のテスト
 * Specification: feature/timeline_sync/channel_add/SPECIFICATION.md
 * Story Issue: #46 (US-2), #69 (US-5)
 */
class ChannelSearchUseCaseTest {

    // ========================================
    // テスト用モック DataSource
    // ========================================

    private class FakeTwitchSearchDataSource(
        private val channelsResult: Result<TwitchUserResponse> = Result.success(
            TwitchUserResponse(data = emptyList()),
        ),
    ) : TwitchSearchDataSource {
        var lastQuery: String? = null
        var lastMaxResults: Int? = null

        override suspend fun searchChannels(query: String, maxResults: Int): Result<TwitchUserResponse> {
            lastQuery = query
            lastMaxResults = maxResults
            return channelsResult
        }

        override suspend fun searchVideos(searchQuery: SearchQuery): Result<TwitchSearchResponse> {
            return Result.failure(NotImplementedError())
        }
    }

    private class FakeYouTubeSearchDataSource(
        private val channelsResult: Result<YouTubeChannelSearchResponse> = Result.success(
            YouTubeChannelSearchResponse(items = emptyList()),
        ),
    ) : YouTubeSearchDataSource {
        var lastQuery: String? = null
        var lastMaxResults: Int? = null

        override suspend fun searchChannels(query: String, maxResults: Int): Result<YouTubeChannelSearchResponse> {
            lastQuery = query
            lastMaxResults = maxResults
            return channelsResult
        }

        override suspend fun searchVideos(searchQuery: SearchQuery): Result<YouTubeSearchResponse> {
            return Result.failure(NotImplementedError())
        }
    }

    // ========================================
    // searchChannels - Twitch プラットフォーム
    // ========================================

    @Test
    fun `Twitch検索_正常なクエリで検索結果が返されること`() = runTest {
        // Arrange
        val twitchDataSource = FakeTwitchSearchDataSource(
            channelsResult = Result.success(
                TwitchUserResponse(
                    data = listOf(
                        TwitchUser(
                            id = "twitch_1",
                            displayName = "TestStreamer",
                            thumbnailUrl = "https://example.com/thumb.jpg",
                            broadcasterLanguage = "ja",
                            gameId = "game_1",
                            gameName = "Minecraft",
                        ),
                    ),
                ),
            ),
        )
        val youTubeDataSource = FakeYouTubeSearchDataSource()
        val useCase = ChannelSearchUseCase(twitchDataSource, youTubeDataSource)

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
    fun `Twitch検索_Twitch DataSourceに正しいパラメータが渡されること`() = runTest {
        // Arrange
        val twitchDataSource = FakeTwitchSearchDataSource()
        val youTubeDataSource = FakeYouTubeSearchDataSource()
        val useCase = ChannelSearchUseCase(twitchDataSource, youTubeDataSource)

        // Act
        useCase.searchChannels("  streamer name  ", VideoServiceType.TWITCH, maxResults = 3)

        // Assert
        assertEquals("streamer name", twitchDataSource.lastQuery)
        assertEquals(3, twitchDataSource.lastMaxResults)
    }

    // ========================================
    // searchChannels - YouTube プラットフォーム
    // ========================================

    @Test
    fun `YouTube検索_正常なクエリで検索結果が返されること`() = runTest {
        // Arrange
        val twitchDataSource = FakeTwitchSearchDataSource()
        val youTubeDataSource = FakeYouTubeSearchDataSource(
            channelsResult = Result.success(
                YouTubeChannelSearchResponse(
                    items = listOf(
                        YouTubeChannelSearchItem(
                            id = YouTubeChannelSearchId(
                                kind = "youtube#channel",
                                channelId = "UC_youtube_1",
                            ),
                            snippet = YouTubeChannelSnippet(
                                title = "YouTubeChannel",
                                thumbnails = YouTubeThumbnails(
                                    default = YouTubeThumbnail(url = "https://example.com/yt_thumb.jpg"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
        val useCase = ChannelSearchUseCase(twitchDataSource, youTubeDataSource)

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
    fun `YouTube検索_YouTube DataSourceに正しいパラメータが渡されること`() = runTest {
        // Arrange
        val twitchDataSource = FakeTwitchSearchDataSource()
        val youTubeDataSource = FakeYouTubeSearchDataSource()
        val useCase = ChannelSearchUseCase(twitchDataSource, youTubeDataSource)

        // Act
        useCase.searchChannels("  channel name  ", VideoServiceType.YOUTUBE, maxResults = 5)

        // Assert
        assertEquals("channel name", youTubeDataSource.lastQuery)
        assertEquals(5, youTubeDataSource.lastMaxResults)
    }

    // ========================================
    // 共通バリデーション
    // ========================================

    @Test
    fun `空クエリ_Twitch検索でエラーが返されること`() = runTest {
        // Arrange
        val useCase = ChannelSearchUseCase(
            FakeTwitchSearchDataSource(),
            FakeYouTubeSearchDataSource(),
        )

        // Act
        val result = useCase.searchChannels("", VideoServiceType.TWITCH)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `空白のみクエリ_YouTube検索でエラーが返されること`() = runTest {
        // Arrange
        val useCase = ChannelSearchUseCase(
            FakeTwitchSearchDataSource(),
            FakeYouTubeSearchDataSource(),
        )

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
        val twitchDataSource = FakeTwitchSearchDataSource(
            channelsResult = Result.failure(RuntimeException("Twitch API error")),
        )
        val useCase = ChannelSearchUseCase(twitchDataSource, FakeYouTubeSearchDataSource())

        // Act
        val result = useCase.searchChannels("test", VideoServiceType.TWITCH)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `YouTube検索エラー_エラーResultが返されること`() = runTest {
        // Arrange
        val youTubeDataSource = FakeYouTubeSearchDataSource(
            channelsResult = Result.failure(RuntimeException("YouTube API error")),
        )
        val useCase = ChannelSearchUseCase(FakeTwitchSearchDataSource(), youTubeDataSource)

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
        val twitchDataSource = FakeTwitchSearchDataSource(
            channelsResult = Result.success(
                TwitchUserResponse(
                    data = listOf(
                        TwitchUser(id = "t1", displayName = "Streamer1"),
                    ),
                ),
            ),
        )
        val useCase = ChannelSearchUseCase(twitchDataSource, FakeYouTubeSearchDataSource())

        // Act
        val result = useCase.searchTwitchChannels("test")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals(VideoServiceType.TWITCH, result.getOrThrow()[0].serviceType)
    }
}
