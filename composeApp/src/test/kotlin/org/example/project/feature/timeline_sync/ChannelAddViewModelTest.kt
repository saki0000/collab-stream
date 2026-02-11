@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package org.example.project.feature.timeline_sync

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.example.project.data.datasource.TwitchSearchDataSource
import org.example.project.data.datasource.YouTubeSearchDataSource
import org.example.project.data.model.TwitchSearchResponse
import org.example.project.data.model.TwitchUser
import org.example.project.data.model.TwitchUserResponse
import org.example.project.data.model.YouTubeChannelSearchResponse
import org.example.project.data.model.YouTubeSearchResponse
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SearchQuery
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.TimelineSyncRepository
import org.example.project.domain.usecase.ChannelSearchUseCase

/**
 * ViewModelテスト: TimelineSyncViewModel - チャンネル追加・管理
 *
 * Story 2: チャンネル追加・管理の振る舞いを定義
 * Story 5: マルチプラットフォームチャンネル検索
 *
 * Specification: feature/timeline_sync/channel_add/SPECIFICATION.md
 * Story Issue: #46, #69
 * Epic: Timeline Sync (EPIC-002)
 */
class ChannelAddViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: FakeTimelineSyncRepository
    private lateinit var mockDataSource: FakeTwitchSearchDataSource
    private lateinit var mockYouTubeDataSource: FakeYouTubeSearchDataSource
    private lateinit var channelSearchUseCase: ChannelSearchUseCase
    private lateinit var viewModel: TimelineSyncViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = FakeTimelineSyncRepository()
        mockDataSource = FakeTwitchSearchDataSource()
        mockYouTubeDataSource = FakeYouTubeSearchDataSource()
        channelSearchUseCase = ChannelSearchUseCase(mockDataSource, mockYouTubeDataSource)
        viewModel = TimelineSyncViewModel(mockRepository, channelSearchUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============================================
    // モーダル表示制御
    // ============================================

    @Test
    fun `initial state should have modal hidden`() {
        assertFalse(viewModel.uiState.value.isChannelAddModalVisible)
    }

    @Test
    fun `OpenChannelAddModal should show modal`() = runTest {
        viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isChannelAddModalVisible)
    }

    @Test
    fun `CloseChannelAddModal should hide modal`() = runTest {
        viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
        advanceUntilIdle()
        viewModel.handleIntent(TimelineSyncIntent.CloseChannelAddModal)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isChannelAddModalVisible)
    }

    @Test
    fun `closing modal should reset search state`() = runTest {
        viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
        viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("test"))
        advanceTimeBy(600)
        advanceUntilIdle()

        viewModel.handleIntent(TimelineSyncIntent.CloseChannelAddModal)
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.channelSearchQuery)
        assertTrue(viewModel.uiState.value.channelSuggestions.isEmpty())
    }

    // ============================================
    // チャンネル検索
    // ============================================

    @Test
    fun `should search channels after 500ms debounce`() = runTest {
        viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
        viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("ninja"))

        // デバウンス待機
        advanceTimeBy(600)
        advanceUntilIdle()

        assertTrue(mockDataSource.searchChannelsWasCalled)
    }

    @Test
    fun `should debounce search queries`() = runTest {
        viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
        viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("n"))
        advanceTimeBy(100)
        viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("ni"))
        advanceTimeBy(100)
        viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("nin"))
        advanceTimeBy(600)
        advanceUntilIdle()

        assertEquals(1, mockDataSource.searchChannelsCount)
        assertEquals("nin", mockDataSource.lastChannelQuery)
    }

    @Test
    fun `should not search with empty query`() = runTest {
        viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
        viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery(""))
        advanceTimeBy(600)
        advanceUntilIdle()

        assertFalse(mockDataSource.searchChannelsWasCalled)
        assertTrue(viewModel.uiState.value.channelSuggestions.isEmpty())
    }

    @Test
    fun `should update suggestions on successful search`() = runTest {
        mockDataSource.channelResultToReturn = TwitchUserResponse(
            data = listOf(
                TwitchUser(
                    id = "ch1",
                    displayName = "Channel 1",
                ),
            ),
        )

        viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
        viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("test"))
        advanceTimeBy(600)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.channelSuggestions.size)
    }

    @Test
    fun `should exclude already added channels from search results`() = runTest {
        // 1. チャンネルを1つ追加する
        val existingChannel = ChannelInfo(
            id = "ch1",
            displayName = "Channel 1",
        )
        viewModel.handleIntent(TimelineSyncIntent.AddChannel(existingChannel))
        advanceUntilIdle()

        // 2. 検索結果に同じチャンネルが含まれるようにモックを設定
        mockDataSource.channelResultToReturn = TwitchUserResponse(
            data = listOf(
                TwitchUser(
                    id = "ch1",
                    displayName = "Channel 1",
                ),
                TwitchUser(
                    id = "ch2",
                    displayName = "Channel 2",
                ),
            ),
        )

        // 3. 検索を実行
        viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
        viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("Channel"))
        advanceTimeBy(600)
        advanceUntilIdle()

        // 4. 検索候補に既存チャンネルが含まれていないことを確認
        val suggestions = viewModel.uiState.value.channelSuggestions
        assertEquals(1, suggestions.size)
        assertEquals("ch2", suggestions[0].id)
    }

    // ============================================
    // チャンネル追加
    // ============================================

    @Test
    fun `should add channel to list`() = runTest {
        val channel = ChannelInfo(
            id = "ch1",
            displayName = "Channel 1",
        )

        viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
        advanceUntilIdle()

        // Mock channels from loadScreen + new channel
        val addedChannel = viewModel.uiState.value.channels.find { it.channelId == "ch1" }
        assertNotNull(addedChannel)
        assertEquals("ch1", addedChannel!!.channelId)
    }

    @Test
    fun `added channel should be SyncChannel format`() = runTest {
        val channel = ChannelInfo(
            id = "ch1",
            displayName = "Channel 1",
            thumbnailUrl = "http://example.com/icon.jpg",
        )

        viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
        advanceUntilIdle()

        val addedChannel = viewModel.uiState.value.channels.find { it.channelId == "ch1" }
        assertNotNull(addedChannel)
        assertEquals("ch1", addedChannel!!.channelId)
        assertEquals("Channel 1", addedChannel.channelName)
        assertEquals("http://example.com/icon.jpg", addedChannel.channelIconUrl)
        assertEquals(VideoServiceType.TWITCH, addedChannel.serviceType)
        assertNull(addedChannel.selectedStream)
        assertEquals(SyncStatus.NOT_SYNCED, addedChannel.syncStatus)
    }

    @Test
    fun `should not add duplicate channel`() = runTest {
        val channel = ChannelInfo(
            id = "ch1",
            displayName = "Channel 1",
        )

        viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
        advanceUntilIdle()
        val countAfterFirst = viewModel.uiState.value.channels.count { it.channelId == "ch1" }

        viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
        advanceUntilIdle()
        val countAfterSecond = viewModel.uiState.value.channels.count { it.channelId == "ch1" }

        assertEquals(1, countAfterFirst)
        assertEquals(1, countAfterSecond)
    }

    @Test
    fun `should set error message on duplicate add`() = runTest {
        val channel = ChannelInfo(
            id = "ch1",
            displayName = "Channel 1",
        )

        viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
        advanceUntilIdle()

        viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
        // Don't advance too much to let the auto-dismiss happen
        advanceTimeBy(100)

        assertNotNull(viewModel.uiState.value.channelAddError)
    }

    @Test
    fun `should allow up to 10 channels`() = runTest {
        // Clear initial mock channels by starting fresh state
        repeat(10) { i ->
            viewModel.handleIntent(
                TimelineSyncIntent.AddChannel(
                    ChannelInfo(
                        id = "newch$i",
                        displayName = "Channel $i",
                    ),
                ),
            )
        }
        advanceUntilIdle()

        // Should have at least 10 channels (including mock data)
        assertTrue(viewModel.uiState.value.channels.size >= 10)
    }

    @Test
    fun `should reject 11th channel and show error`() = runTest {
        // Fill up to 10 channels
        repeat(10) {
            viewModel.handleIntent(
                TimelineSyncIntent.AddChannel(
                    ChannelInfo(
                        id = "ch$it",
                        displayName = "Channel $it",
                    ),
                ),
            )
        }
        advanceUntilIdle()
        assertEquals(10, viewModel.uiState.value.channels.size)
        assertFalse(viewModel.uiState.value.canAddChannel)

        // Try to add 11th channel
        viewModel.handleIntent(
            TimelineSyncIntent.AddChannel(
                ChannelInfo(
                    id = "ch11",
                    displayName = "Channel 11",
                ),
            ),
        )
        advanceTimeBy(100) // To check state before error is auto-cleared

        // Assert that the 11th channel was not added and an error is shown
        assertEquals(10, viewModel.uiState.value.channels.size)
        assertNotNull(viewModel.uiState.value.channelAddError)
        assertTrue(viewModel.uiState.value.channels.none { it.channelId == "ch11" })
    }

    // ============================================
    // チャンネル削除
    // ============================================

    @Test
    fun `should remove channel from list`() = runTest {
        val channel = ChannelInfo(
            id = "ch1",
            displayName = "Channel 1",
        )
        viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
        advanceUntilIdle()

        viewModel.handleIntent(TimelineSyncIntent.RemoveChannel("ch1"))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.channels.find { it.channelId == "ch1" })
    }

    @Test
    fun `should not throw on removing non-existent channel`() = runTest {
        viewModel.handleIntent(TimelineSyncIntent.RemoveChannel("non-existent"))
        advanceUntilIdle()

        // No exception should be thrown - test passes if we reach here
        assertTrue(true)
    }

    @Test
    fun `should set recentlyDeletedChannel after deletion`() = runTest {
        val channel = ChannelInfo(
            id = "ch1",
            displayName = "Channel 1",
        )
        viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
        advanceUntilIdle()

        viewModel.handleIntent(TimelineSyncIntent.RemoveChannel("ch1"))
        // Don't advance until idle to avoid the 3-second timeout clearing it
        advanceTimeBy(100)

        assertNotNull(viewModel.uiState.value.recentlyDeletedChannel)
        assertEquals("ch1", viewModel.uiState.value.recentlyDeletedChannel?.channelId)
    }

    @Test
    fun `should restore channel when undo is tapped`() = runTest {
        val channel = ChannelInfo(
            id = "ch1",
            displayName = "Channel 1",
        )
        viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
        advanceUntilIdle()

        viewModel.handleIntent(TimelineSyncIntent.RemoveChannel("ch1"))
        advanceTimeBy(100)

        // 削除を取り消す
        viewModel.handleIntent(TimelineSyncIntent.UndoRemoveChannel)
        advanceUntilIdle()

        val restoredChannel = viewModel.uiState.value.channels.find { it.channelId == "ch1" }
        assertNotNull(restoredChannel)
        assertNull(viewModel.uiState.value.recentlyDeletedChannel)
    }

    @Test
    fun `should clear recentlyDeletedChannel after 3 seconds`() = runTest {
        val channel = ChannelInfo(
            id = "ch1",
            displayName = "Channel 1",
        )
        viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
        advanceUntilIdle()

        viewModel.handleIntent(TimelineSyncIntent.RemoveChannel("ch1"))

        // 3秒経過
        advanceTimeBy(3100)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.recentlyDeletedChannel)
    }

    // ============================================
    // AddChannelボタン
    // ============================================

    @Test
    fun `canAddChannel should be true when less than 10 channels`() {
        val state = TimelineSyncUiState(channels = emptyList())
        assertTrue(state.canAddChannel)
    }

    @Test
    fun `canAddChannel should be false when 10 channels exist`() {
        val channels = (1..10).map { i ->
            SyncChannel(
                channelId = "ch$i",
                channelName = "Channel $i",
                channelIconUrl = "",
                serviceType = VideoServiceType.TWITCH,
            )
        }
        val state = TimelineSyncUiState(channels = channels)

        assertFalse(state.canAddChannel)
    }

    // ============================================
    // プラットフォーム選択（US-5）
    // ============================================

    @Test
    fun `プラットフォーム選択_初期状態はTwitchが選択されていること`() = runTest {
        // TODO: Phase 2でAI実装
        // viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
        // advanceUntilIdle()
        // assertEquals(VideoServiceType.TWITCH, viewModel.uiState.value.selectedPlatform)
    }

    @Test
    fun `プラットフォーム選択_YouTubeタブをタップするとYouTubeが選択されること`() = runTest {
        // TODO: Phase 2でAI実装
        // viewModel.handleIntent(TimelineSyncIntent.SelectPlatform(VideoServiceType.YOUTUBE))
        // advanceUntilIdle()
        // assertEquals(VideoServiceType.YOUTUBE, viewModel.uiState.value.selectedPlatform)
    }

    @Test
    fun `プラットフォーム選択_切り替え時に検索結果がクリアされること`() = runTest {
        // TODO: Phase 2でAI実装
        // 1. Twitchで検索を実行して結果を取得
        // 2. YouTubeタブに切り替え
        // 3. 検索結果が空になることを確認
    }

    @Test
    fun `プラットフォーム選択_切り替え時に検索クエリは保持されること`() = runTest {
        // TODO: Phase 2でAI実装
        // 1. 検索クエリを入力
        // 2. プラットフォームを切り替え
        // 3. 検索クエリが保持されていることを確認
    }

    @Test
    fun `プラットフォーム選択_クエリありで切り替え時に自動再検索が実行されること`() = runTest {
        // TODO: Phase 2でAI実装
        // 1. 検索クエリを入力して検索実行
        // 2. プラットフォームを切り替え
        // 3. 新しいプラットフォームで自動的に検索が実行されることを確認
    }

    @Test
    fun `プラットフォーム選択_YouTubeで検索するとYouTube APIが呼ばれること`() = runTest {
        // TODO: Phase 2でAI実装
        // 1. YouTubeを選択
        // 2. 検索を実行
        // 3. YouTube検索APIが呼ばれることを確認
    }

    @Test
    fun `プラットフォーム選択_追加したチャンネルに正しいserviceTypeが設定されること`() = runTest {
        // TODO: Phase 2でAI実装
        // 1. YouTubeを選択
        // 2. チャンネルを追加
        // 3. 追加されたチャンネルのserviceTypeがYOUTUBEであることを確認
    }
}

/**
 * Fake implementation of TimelineSyncRepository for testing.
 */
class FakeTimelineSyncRepository : TimelineSyncRepository {
    var shouldReturnError = false
    var errorToReturn: Throwable = RuntimeException("Mock error")

    override suspend fun getVideoDetails(
        videoId: String,
        serviceType: VideoServiceType,
    ): Result<VideoDetails> {
        return if (shouldReturnError) {
            Result.failure(errorToReturn)
        } else {
            Result.failure(NoSuchElementException("Not implemented for mock"))
        }
    }

    override suspend fun getChannelVideos(
        channelId: String,
        serviceType: VideoServiceType,
        dateRange: ClosedRange<LocalDate>,
    ): Result<List<VideoDetails>> {
        return if (shouldReturnError) {
            Result.failure(errorToReturn)
        } else {
            Result.success(emptyList())
        }
    }
}

/**
 * テスト用 YouTubeSearchDataSource のフェイク実装。
 */
class FakeYouTubeSearchDataSource : YouTubeSearchDataSource {
    var searchChannelsWasCalled = false
    var searchChannelsCount = 0
    var lastChannelQuery: String? = null
    var channelResultToReturn: YouTubeChannelSearchResponse = YouTubeChannelSearchResponse(items = emptyList())
    var shouldReturnError = false

    override suspend fun searchVideos(searchQuery: SearchQuery): Result<YouTubeSearchResponse> {
        return Result.failure(NotImplementedError())
    }

    override suspend fun searchChannels(
        query: String,
        maxResults: Int,
    ): Result<YouTubeChannelSearchResponse> {
        searchChannelsWasCalled = true
        searchChannelsCount++
        lastChannelQuery = query

        return if (shouldReturnError) {
            Result.failure(RuntimeException("Search error"))
        } else {
            Result.success(channelResultToReturn)
        }
    }
}

/**
 * Fake implementation of TwitchSearchDataSource for testing.
 */
class FakeTwitchSearchDataSource : TwitchSearchDataSource {
    var searchChannelsWasCalled = false
    var searchChannelsCount = 0
    var lastChannelQuery: String? = null
    var channelResultToReturn: TwitchUserResponse = TwitchUserResponse(data = emptyList())
    var shouldReturnError = false

    override suspend fun searchVideos(searchQuery: SearchQuery): Result<TwitchSearchResponse> {
        return Result.success(TwitchSearchResponse(data = emptyList()))
    }

    override suspend fun searchChannels(
        query: String,
        maxResults: Int,
    ): Result<TwitchUserResponse> {
        searchChannelsWasCalled = true
        searchChannelsCount++
        lastChannelQuery = query

        return if (shouldReturnError) {
            Result.failure(RuntimeException("Search error"))
        } else {
            Result.success(channelResultToReturn)
        }
    }
}
