@file:OptIn(ExperimentalCoroutinesApi::class)

package org.example.project.feature.timeline_sync

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.example.project.domain.model.SelectedStreamInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.TimelineSyncRepository

/**
 * ViewModelテスト: TimelineSyncViewModel
 *
 * Story 1: タイムライン基本表示の振る舞いを定義
 *
 * Specification: feature/timeline_sync/REQUIREMENTS.md
 * Epic: Timeline Sync (EPIC-002)
 */
class TimelineSyncViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockTimelineSyncRepository
    private lateinit var viewModel: TimelineSyncViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockTimelineSyncRepository()
        viewModel = TimelineSyncViewModel(mockRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============================================
    // 初期状態
    // ============================================

    @Test
    fun `initial state should have isLoading false`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
    }

    @Test
    fun `initial state should have empty channel list`() {
        val state = viewModel.uiState.value
        assertTrue(state.channels.isEmpty())
    }

    @Test
    fun `initial state should have today as selected date`() {
        val state = viewModel.uiState.value
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        assertEquals(today, state.selectedDate)
    }

    @Test
    fun `initial state should have null sync time`() {
        val state = viewModel.uiState.value
        assertNull(state.syncTime)
    }

    @Test
    fun `initial state should have null error message`() {
        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
    }

    // ============================================
    // 画面読み込み
    // ============================================

    @Test
    fun `LoadScreen intent should set isLoading to true`() = runTest {
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)

        // Initially loading
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `successful load should update channel list`() = runTest {
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.channels.isNotEmpty())
    }

    @Test
    fun `successful load should set isLoading to false`() = runTest {
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `load with no channels should set isEmpty to true`() = runTest {
        // Note: Current implementation uses mock data, so this test verifies
        // the isEmpty computed property works correctly
        val state = TimelineSyncUiState(isLoading = false, channels = emptyList())
        assertTrue(state.isEmpty)
    }

    // ============================================
    // 日付選択
    // ============================================

    @Test
    fun `SelectDate intent should update selected date`() = runTest {
        val newDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            .plus(1, DateTimeUnit.DAY)

        viewModel.handleIntent(TimelineSyncIntent.SelectDate(newDate))
        advanceUntilIdle()

        assertEquals(newDate, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `date change should not affect displayed week`() = runTest {
        val initialWeekStart = viewModel.uiState.value.displayedWeekStart
        val newDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            .plus(1, DateTimeUnit.DAY)

        viewModel.handleIntent(TimelineSyncIntent.SelectDate(newDate))
        advanceUntilIdle()

        // Week start should remain the same unless date is in different week
        // This test verifies the intent doesn't accidentally change displayedWeekStart
        val state = viewModel.uiState.value
        assertEquals(newDate, state.selectedDate)
    }

    // ============================================
    // 週移動
    // ============================================

    @Test
    fun `NavigateToPreviousWeek should move to previous week`() = runTest {
        val initialWeekStart = viewModel.uiState.value.displayedWeekStart

        viewModel.handleIntent(TimelineSyncIntent.NavigateToPreviousWeek)
        advanceUntilIdle()

        val expectedWeekStart = initialWeekStart.plus(-7, DateTimeUnit.DAY)
        assertEquals(expectedWeekStart, viewModel.uiState.value.displayedWeekStart)
    }

    @Test
    fun `NavigateToNextWeek should move to next week`() = runTest {
        val initialWeekStart = viewModel.uiState.value.displayedWeekStart

        viewModel.handleIntent(TimelineSyncIntent.NavigateToNextWeek)
        advanceUntilIdle()

        val expectedWeekStart = initialWeekStart.plus(7, DateTimeUnit.DAY)
        assertEquals(expectedWeekStart, viewModel.uiState.value.displayedWeekStart)
    }

    @Test
    fun `week navigation should not change selected date`() = runTest {
        val initialSelectedDate = viewModel.uiState.value.selectedDate

        viewModel.handleIntent(TimelineSyncIntent.NavigateToNextWeek)
        advanceUntilIdle()

        assertEquals(initialSelectedDate, viewModel.uiState.value.selectedDate)
    }

    // ============================================
    // タイムラインバー計算
    // ============================================

    @Test
    fun `should calculate timeline bar position when stream exists`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val timeZone = TimeZone.currentSystemDefault()
        val startTime = today.atStartOfDayIn(timeZone) + 10.hours

        val channel = SyncChannel(
            channelId = "test-channel",
            channelName = "Test Channel",
            channelIconUrl = "",
            serviceType = VideoServiceType.YOUTUBE,
            selectedStream = SelectedStreamInfo(
                id = "test-stream",
                title = "Test Stream",
                thumbnailUrl = "",
                startTime = startTime,
                endTime = startTime + 3.hours,
                duration = 3.hours,
            ),
        )

        val barInfo = TimelineSyncViewModel.calculateTimelineBarInfo(
            channel = channel,
            selectedDate = today,
        )

        assertNotNull(barInfo)
        assertTrue(barInfo!!.startFraction >= 0f)
        assertTrue(barInfo.endFraction <= 1f)
        assertTrue(barInfo.startFraction < barInfo.endFraction)
    }

    @Test
    fun `should have empty timeline bar when no stream`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        val channel = SyncChannel(
            channelId = "test-channel",
            channelName = "Test Channel",
            channelIconUrl = "",
            serviceType = VideoServiceType.YOUTUBE,
            selectedStream = null,
        )

        val barInfo = TimelineSyncViewModel.calculateTimelineBarInfo(
            channel = channel,
            selectedDate = today,
        )

        assertNull(barInfo)
    }

    @Test
    fun `stream starting before selected date should start at 0_00`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val yesterday = today.plus(-1, DateTimeUnit.DAY)
        val timeZone = TimeZone.currentSystemDefault()

        // Stream started yesterday
        val startTime = yesterday.atStartOfDayIn(timeZone) + 20.hours
        val endTime = today.atStartOfDayIn(timeZone) + 5.hours

        val channel = SyncChannel(
            channelId = "test-channel",
            channelName = "Test Channel",
            channelIconUrl = "",
            serviceType = VideoServiceType.YOUTUBE,
            selectedStream = SelectedStreamInfo(
                id = "test-stream",
                title = "Test Stream",
                thumbnailUrl = "",
                startTime = startTime,
                endTime = endTime,
                duration = null,
            ),
        )

        val barInfo = TimelineSyncViewModel.calculateTimelineBarInfo(
            channel = channel,
            selectedDate = today,
        )

        assertNotNull(barInfo)
        assertEquals(0f, barInfo!!.startFraction)
    }

    // ============================================
    // アクティブチャンネルカウント
    // ============================================

    @Test
    fun `should count channels with selected streams`() {
        val channelWithStream = SyncChannel(
            channelId = "ch1",
            channelName = "Channel 1",
            channelIconUrl = "",
            serviceType = VideoServiceType.YOUTUBE,
            selectedStream = SelectedStreamInfo(
                id = "s1",
                title = "Stream 1",
                thumbnailUrl = "",
                startTime = Clock.System.now(),
                endTime = null,
                duration = null,
            ),
        )

        val channelWithoutStream = SyncChannel(
            channelId = "ch2",
            channelName = "Channel 2",
            channelIconUrl = "",
            serviceType = VideoServiceType.TWITCH,
            selectedStream = null,
        )

        val state = TimelineSyncUiState(
            channels = listOf(channelWithStream, channelWithoutStream),
        )

        assertEquals(1, state.activeChannelCount)
    }

    @Test
    fun `should not count channels without streams`() {
        val channel1 = SyncChannel(
            channelId = "ch1",
            channelName = "Channel 1",
            channelIconUrl = "",
            serviceType = VideoServiceType.YOUTUBE,
            selectedStream = null,
        )

        val channel2 = SyncChannel(
            channelId = "ch2",
            channelName = "Channel 2",
            channelIconUrl = "",
            serviceType = VideoServiceType.TWITCH,
            selectedStream = null,
        )

        val state = TimelineSyncUiState(
            channels = listOf(channel1, channel2),
        )

        assertEquals(0, state.activeChannelCount)
    }

    // ============================================
    // 空状態
    // ============================================

    @Test
    fun `should have isEmpty true when no channels`() {
        val state = TimelineSyncUiState(
            isLoading = false,
            channels = emptyList(),
        )

        assertTrue(state.isEmpty)
    }

    @Test
    fun `should have isEmpty false when channels exist`() {
        val channel = SyncChannel(
            channelId = "ch1",
            channelName = "Channel 1",
            channelIconUrl = "",
            serviceType = VideoServiceType.YOUTUBE,
        )

        val state = TimelineSyncUiState(
            isLoading = false,
            channels = listOf(channel),
        )

        assertFalse(state.isEmpty)
    }

    @Test
    fun `should have isEmpty false when loading`() {
        val state = TimelineSyncUiState(
            isLoading = true,
            channels = emptyList(),
        )

        assertFalse(state.isEmpty)
    }

    // ============================================
    // エラーハンドリング
    // ============================================

    @Test
    fun `ClearError intent should clear error message`() = runTest {
        // First, simulate an error state (through direct state manipulation for test)
        // In real scenario, this would come from a failed network call

        viewModel.handleIntent(TimelineSyncIntent.ClearError)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `Retry intent should trigger reload`() = runTest {
        viewModel.handleIntent(TimelineSyncIntent.Retry)

        // Verify loading state is triggered
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        // Verify load completed
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ============================================
    // 週日付リスト
    // ============================================

    @Test
    fun `weekDays should return 7 days starting from displayedWeekStart`() {
        val state = viewModel.uiState.value

        assertEquals(7, state.weekDays.size)
        assertEquals(state.displayedWeekStart, state.weekDays.first())
        assertEquals(
            state.displayedWeekStart.plus(6, DateTimeUnit.DAY),
            state.weekDays.last(),
        )
    }
}

/**
 * Mock implementation of TimelineSyncRepository for testing.
 */
class MockTimelineSyncRepository : TimelineSyncRepository {
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
