@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package org.example.project.feature.archive_home

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.TwitchStreamInfo
import org.example.project.domain.model.TwitchVideoDetailsImpl
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.model.VideoSnippet
import org.example.project.domain.repository.TimelineSyncRepository
import org.example.project.domain.usecase.ChannelSearchUseCase
import org.example.project.feature.timeline_sync.FakeChannelFollowRepository
import org.example.project.testing.repository.FakeVideoSearchRepository

/**
 * ViewModelテスト: ArchiveHomeViewModel
 *
 * アーカイブHome画面の振る舞いを検証する。
 *
 * Epic: Channel Follow & Archive Home (US-3)
 * Story: US-3 (Archive Home Display)
 */
class ArchiveHomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeTimelineSyncRepository: FakeArchiveTimelineSyncRepository
    private lateinit var fakeChannelFollowRepository: FakeChannelFollowRepository
    private lateinit var channelSearchUseCase: ChannelSearchUseCase
    private lateinit var viewModel: ArchiveHomeViewModel

    // 固定時刻: 2024-01-15T10:00:00Z（月曜日）
    private val fixedInstant = Instant.parse("2024-01-15T10:00:00Z")
    private val fixedClock = object : kotlin.time.Clock {
        override fun now(): Instant = fixedInstant
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeTimelineSyncRepository = FakeArchiveTimelineSyncRepository()
        fakeChannelFollowRepository = FakeChannelFollowRepository()
        channelSearchUseCase = ChannelSearchUseCase(
            FakeVideoSearchRepository(),
        )

        viewModel = ArchiveHomeViewModel(
            timelineSyncRepository = fakeTimelineSyncRepository,
            channelFollowRepository = fakeChannelFollowRepository,
            channelSearchUseCase = channelSearchUseCase,
            clock = fixedClock,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================
    // 初期状態
    // ========================================

    @Test
    fun `初期状態_ローディングfalse_空アーカイブであること`() = runTest {
        // Act
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.archives.isEmpty())
    }

    // ========================================
    // LoadScreen - フォロー0件
    // ========================================

    @Test
    fun `LoadScreen_フォロー0件_hasNoFollowedChannelsがtrueになること`() = runTest {
        // Act
        viewModel.handleIntent(ArchiveHomeIntent.LoadScreen)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.hasNoFollowedChannels)
        assertTrue(state.archives.isEmpty())
    }

    // ========================================
    // LoadScreen - フォローあり、アーカイブあり
    // ========================================

    @Test
    fun `LoadScreen_フォローありアーカイブあり_アーカイブが表示されること`() = runTest {
        // Arrange
        fakeChannelFollowRepository.follow(
            channelId = "ch1",
            channelName = "Channel 1",
            channelIconUrl = "https://example.com/icon1.jpg",
            serviceType = VideoServiceType.TWITCH,
        )
        advanceUntilIdle()

        val mockVideo = TwitchVideoDetailsImpl(
            id = "video1",
            snippet = VideoSnippet(
                channelId = "ch1",
                channelTitle = "Channel 1",
                title = "Test Video",
                description = "",
            ),
            streamInfo = TwitchStreamInfo(
                streamId = "stream1",
                createdAt = "2024-01-15T08:00:00Z",
                publishedAt = "2024-01-15T08:00:00Z",
                type = "archive",
                duration = "1h0m0s",
                viewable = "public",
            ),
        )
        fakeTimelineSyncRepository.channelVideosToReturn = listOf(mockVideo)

        // Act
        viewModel.handleIntent(ArchiveHomeIntent.LoadScreen)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.hasNoFollowedChannels)
        assertFalse(state.hasNoArchives)
        assertEquals(1, state.archives.size)
        assertEquals("video1", state.archives[0].videoId)
        assertEquals("Test Video", state.archives[0].title)
        assertEquals("Channel 1", state.archives[0].channelName)
    }

    // ========================================
    // LoadScreen - フォローあり、アーカイブ0件
    // ========================================

    @Test
    fun `LoadScreen_フォローありアーカイブ0件_hasNoArchivesがtrueになること`() = runTest {
        // Arrange
        fakeChannelFollowRepository.follow(
            channelId = "ch1",
            channelName = "Channel 1",
            channelIconUrl = "",
            serviceType = VideoServiceType.TWITCH,
        )
        advanceUntilIdle()

        fakeTimelineSyncRepository.channelVideosToReturn = emptyList()

        // Act
        viewModel.handleIntent(ArchiveHomeIntent.LoadScreen)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.hasNoFollowedChannels)
        assertTrue(state.hasNoArchives)
        assertTrue(state.archives.isEmpty())
    }

    // ========================================
    // SelectDate - 日付変更
    // ========================================

    @Test
    fun `SelectDate_日付変更_選択日が更新されること`() = runTest {
        // Arrange
        val newDate = LocalDate.parse("2024-01-16")
        advanceUntilIdle()

        // Act
        viewModel.handleIntent(ArchiveHomeIntent.SelectDate(newDate))
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(newDate, state.selectedDate)
    }

    // ========================================
    // NavigateToPreviousWeek / NavigateToNextWeek
    // ========================================

    @Test
    fun `NavigateToPreviousWeek_表示週が7日前に移動すること`() = runTest {
        // Arrange
        advanceUntilIdle()
        val initialWeekStart = viewModel.uiState.value.displayedWeekStart

        // Act
        viewModel.handleIntent(ArchiveHomeIntent.NavigateToPreviousWeek)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.displayedWeekStart < initialWeekStart)
    }

    @Test
    fun `NavigateToNextWeek_表示週が7日後に移動すること`() = runTest {
        // Arrange
        advanceUntilIdle()
        val initialWeekStart = viewModel.uiState.value.displayedWeekStart

        // Act
        viewModel.handleIntent(ArchiveHomeIntent.NavigateToNextWeek)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.displayedWeekStart > initialWeekStart)
    }

    // ========================================
    // OpenChannelAddModal / CloseChannelAddModal
    // ========================================

    @Test
    fun `OpenChannelAddModal_モーダルが表示されること`() = runTest {
        // Act
        viewModel.handleIntent(ArchiveHomeIntent.OpenChannelAddModal)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState.value.isChannelAddModalVisible)
    }

    @Test
    fun `CloseChannelAddModal_モーダルが非表示になり検索状態がリセットされること`() = runTest {
        // Arrange
        viewModel.handleIntent(ArchiveHomeIntent.OpenChannelAddModal)
        advanceUntilIdle()

        // Act
        viewModel.handleIntent(ArchiveHomeIntent.CloseChannelAddModal)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isChannelAddModalVisible)
        assertEquals("", state.channelSearchQuery)
        assertTrue(state.channelSuggestions.isEmpty())
    }

    // ========================================
    // ToggleFollow - フォロー追加
    // ========================================

    @Test
    fun `ToggleFollow_未フォローチャンネル_フォローが追加されること`() = runTest {
        // Arrange
        advanceUntilIdle()
        val channel = ChannelInfo(
            id = "ch1",
            displayName = "Test Channel",
            thumbnailUrl = "https://example.com/icon.jpg",
            gameName = "Game",
            serviceType = VideoServiceType.TWITCH,
        )
        fakeTimelineSyncRepository.channelVideosToReturn = emptyList()

        // Act
        viewModel.handleIntent(ArchiveHomeIntent.ToggleFollow(channel))
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(1, state.followedChannels.size)
        assertEquals("ch1", state.followedChannels[0].channelId)
        assertEquals("Test Channel", state.followedChannels[0].channelName)
    }

    // ========================================
    // ToggleArchiveSelection - アーカイブ選択トグル
    // ========================================

    @Test
    fun `ToggleArchiveSelection_未選択アーカイブをタップ_選択状態になること`() = runTest {
        // Arrange
        advanceUntilIdle()

        // Act
        viewModel.handleIntent(ArchiveHomeIntent.ToggleArchiveSelection("video1"))
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.selectedArchiveIds.contains("video1"))
        assertEquals(1, state.selectedArchiveIds.size)
    }

    @Test
    fun `ToggleArchiveSelection_選択済みアーカイブをタップ_選択解除されること`() = runTest {
        // Arrange
        advanceUntilIdle()
        viewModel.handleIntent(ArchiveHomeIntent.ToggleArchiveSelection("video1"))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.selectedArchiveIds.contains("video1"))

        // Act
        viewModel.handleIntent(ArchiveHomeIntent.ToggleArchiveSelection("video1"))
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.selectedArchiveIds.contains("video1"))
        assertEquals(0, state.selectedArchiveIds.size)
    }

    @Test
    fun `ToggleArchiveSelection_最大10件選択後に11件目をタップ_選択されないこと`() = runTest {
        // Arrange
        advanceUntilIdle()
        // 10件選択
        repeat(10) { index ->
            viewModel.handleIntent(ArchiveHomeIntent.ToggleArchiveSelection("video$index"))
        }
        advanceUntilIdle()
        assertEquals(10, viewModel.uiState.value.selectedArchiveIds.size)

        // Act: 11件目を選択しようとする
        viewModel.handleIntent(ArchiveHomeIntent.ToggleArchiveSelection("video99"))
        advanceUntilIdle()

        // Assert: 10件のまま
        val state = viewModel.uiState.value
        assertEquals(10, state.selectedArchiveIds.size)
        assertFalse(state.selectedArchiveIds.contains("video99"))
    }

    // ========================================
    // SelectDate - 日付変更時に選択クリア
    // ========================================

    @Test
    fun `SelectDate_日付変更時_選択中アーカイブがクリアされること`() = runTest {
        // Arrange
        advanceUntilIdle()
        viewModel.handleIntent(ArchiveHomeIntent.ToggleArchiveSelection("video1"))
        viewModel.handleIntent(ArchiveHomeIntent.ToggleArchiveSelection("video2"))
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.selectedArchiveIds.size)

        // Act
        val newDate = LocalDate.parse("2024-01-16")
        viewModel.handleIntent(ArchiveHomeIntent.SelectDate(newDate))
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.selectedArchiveIds.isEmpty())
    }

    // ========================================
    // OpenTimeline - タイムライン遷移
    // ========================================

    @Test
    fun `OpenTimeline_選択なし_SideEffectが発行されないこと`() = runTest {
        // Arrange
        advanceUntilIdle()
        val sideEffects = mutableListOf<ArchiveHomeSideEffect>()
        val job = launch {
            viewModel.sideEffect.collect { sideEffects.add(it) }
        }

        // Act: 選択なしでOpenTimeline
        viewModel.handleIntent(ArchiveHomeIntent.OpenTimeline)
        advanceUntilIdle()

        // Assert: NavigateToTimelineのSideEffectが発行されていないこと
        val navigateSideEffects = sideEffects.filterIsInstance<ArchiveHomeSideEffect.NavigateToTimeline>()
        assertTrue(navigateSideEffects.isEmpty())
        job.cancel()
    }

    @Test
    fun `OpenTimeline_アーカイブ選択後_NavigateToTimelineSideEffectが発行されること`() = runTest {
        // Arrange: フォロー済みチャンネルとアーカイブを設定
        fakeChannelFollowRepository.follow(
            channelId = "ch1",
            channelName = "Channel 1",
            channelIconUrl = "https://example.com/icon1.jpg",
            serviceType = VideoServiceType.TWITCH,
        )
        advanceUntilIdle()

        val mockVideo = TwitchVideoDetailsImpl(
            id = "video1",
            snippet = VideoSnippet(
                channelId = "ch1",
                channelTitle = "Channel 1",
                title = "Test Video",
                description = "",
            ),
            streamInfo = TwitchStreamInfo(
                streamId = "stream1",
                createdAt = "2024-01-15T08:00:00Z",
                publishedAt = "2024-01-15T08:00:00Z",
                type = "archive",
                duration = "1h0m0s",
                viewable = "public",
            ),
        )
        fakeTimelineSyncRepository.channelVideosToReturn = listOf(mockVideo)

        viewModel.handleIntent(ArchiveHomeIntent.LoadScreen)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.archives.size)

        val sideEffects = mutableListOf<ArchiveHomeSideEffect>()
        val job = launch {
            viewModel.sideEffect.collect { sideEffects.add(it) }
        }

        // アーカイブ選択
        viewModel.handleIntent(ArchiveHomeIntent.ToggleArchiveSelection("video1"))
        advanceUntilIdle()

        // Act
        viewModel.handleIntent(ArchiveHomeIntent.OpenTimeline)
        advanceUntilIdle()

        // Assert
        val navigateSideEffects = sideEffects.filterIsInstance<ArchiveHomeSideEffect.NavigateToTimeline>()
        assertEquals(1, navigateSideEffects.size)
        val navigateSideEffect = navigateSideEffects.first()
        assertEquals("2024-01-15", navigateSideEffect.presetDate)
        assertTrue(navigateSideEffect.presetChannelsJson.contains("ch1"))
        job.cancel()
    }

    // ========================================
    // エラーハンドリング
    // ========================================

    @Test
    fun `LoadScreen_エラー発生_エラーメッセージが設定されること`() = runTest {
        // Arrange
        fakeChannelFollowRepository.follow(
            channelId = "ch1",
            channelName = "Channel 1",
            channelIconUrl = "",
            serviceType = VideoServiceType.TWITCH,
        )
        advanceUntilIdle()

        fakeTimelineSyncRepository.shouldReturnError = true
        fakeTimelineSyncRepository.errorToReturn = Exception("Network error")

        // Act
        viewModel.handleIntent(ArchiveHomeIntent.LoadScreen)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage?.contains("データの読み込みに失敗しました") == true)
    }

    @Test
    fun `Retry_エラー後再試行_再度データ取得が実行されること`() = runTest {
        // Arrange
        fakeChannelFollowRepository.follow(
            channelId = "ch1",
            channelName = "Channel 1",
            channelIconUrl = "",
            serviceType = VideoServiceType.TWITCH,
        )
        advanceUntilIdle()

        fakeTimelineSyncRepository.shouldReturnError = true
        viewModel.handleIntent(ArchiveHomeIntent.LoadScreen)
        advanceUntilIdle()

        // Act
        fakeTimelineSyncRepository.shouldReturnError = false
        fakeTimelineSyncRepository.channelVideosToReturn = emptyList()
        viewModel.handleIntent(ArchiveHomeIntent.Retry)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }
}

// ============================================
// テスト用Fake実装
// ============================================

/**
 * テスト用 TimelineSyncRepository のFake実装。
 * channelVideosToReturn で返却データを制御可能。
 */
class FakeArchiveTimelineSyncRepository : TimelineSyncRepository {
    var channelVideosToReturn: List<VideoDetails> = emptyList()
    var shouldReturnError: Boolean = false
    var errorToReturn: Throwable = RuntimeException("Fake error")

    override suspend fun getVideoDetails(
        videoId: String,
        serviceType: VideoServiceType,
    ): Result<VideoDetails> {
        return Result.failure(NoSuchElementException("Not implemented"))
    }

    override suspend fun getChannelVideos(
        channelId: String,
        serviceType: VideoServiceType,
        dateRange: ClosedRange<LocalDate>,
    ): Result<List<VideoDetails>> {
        return if (shouldReturnError) {
            Result.failure(errorToReturn)
        } else {
            Result.success(channelVideosToReturn)
        }
    }
}
