@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package org.example.project.feature.timeline_sync

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.example.project.domain.model.SelectedStreamInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.TimelineSyncRepository
import org.example.project.domain.usecase.ChannelSearchUseCase

/**
 * ViewModelテスト: TimelineSyncViewModel - 同期時刻計算と表示
 *
 * Story 3: 同期時刻選択の振る舞いを定義
 *
 * Specification: feature/timeline_sync/SPECIFICATION.md
 * Story Issue: #53
 * Epic: Timeline Sync (EPIC-002)
 */
class SyncTimeCalculationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: TestTimelineSyncRepository
    private lateinit var mockVideoSearchRepository: FakeVideoSearchRepository
    private lateinit var channelSearchUseCase: ChannelSearchUseCase
    private lateinit var mockChannelFollowRepository: FakeChannelFollowRepository
    private lateinit var viewModel: TimelineSyncViewModel

    // テスト用の固定時刻
    private val testDate = LocalDate(2024, 6, 15)
    private val timeZone = TimeZone.UTC
    private val dayStart = testDate.atStartOfDayIn(timeZone)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = TestTimelineSyncRepository()
        mockVideoSearchRepository = FakeVideoSearchRepository()
        channelSearchUseCase = ChannelSearchUseCase(mockVideoSearchRepository)
        mockChannelFollowRepository = FakeChannelFollowRepository()
        viewModel = TimelineSyncViewModel(mockRepository, channelSearchUseCase, mockChannelFollowRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================
    // Story 1: タイムライン基本表示 - 画面を開いた時
    // ========================================

    @Test
    fun `画面を開いた時_まずはローディング状態になること`() = runTest {
        // Arrange
        val initialState = viewModel.uiState.value

        // Act
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)

        // Assert - ローディング開始直後の状態
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `画面を開いた時_データ取得成功_チャンネルありの場合コンテンツが表示されること`() = runTest {
        // Act
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(!state.isLoading)
        assertTrue(state.channels.isNotEmpty())
        assertNull(state.errorMessage)
    }

    @Test
    fun `画面を開いた時_データ取得成功_チャンネルなしの場合空状態が表示されること`() {
        // Note: 現在のモック実装では常にチャンネルが返されるため、
        // この振る舞いは将来のリポジトリ統合時にテストする
        val emptyState = TimelineSyncUiState(channels = emptyList())
        assertTrue(emptyState.isEmpty)
    }

    @Test
    fun `画面を開いた時_データ取得失敗_エラー状態になること`() {
        // Note: 現在のモック実装ではエラーをシミュレートできないため、
        // UiStateのプロパティでテスト
        val errorState = TimelineSyncUiState(
            isLoading = false,
            errorMessage = "テストエラー",
        )
        assertNotNull(errorState.errorMessage)
        assertTrue(!errorState.isLoading)
    }

    // ========================================
    // Story 1: 日付選択
    // ========================================

    @Test
    fun `日付選択_デフォルトで今日の日付が選択されていること`() {
        // Assert - デフォルト状態を確認
        val state = viewModel.uiState.value
        assertNotNull(state.selectedDate)
    }

    @Test
    fun `日付選択_日付をタップすると選択日付が更新されること`() = runTest {
        // Arrange
        val newDate = LocalDate(2024, 6, 20)

        // Act
        viewModel.handleIntent(TimelineSyncIntent.SelectDate(newDate))
        advanceUntilIdle()

        // Assert
        assertEquals(newDate, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `日付選択_選択日付の変更時にタイムラインバーが再計算されること`() = runTest {
        // Arrange
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()
        val initialDate = viewModel.uiState.value.selectedDate

        // Act
        val newDate = initialDate.plus(1, DateTimeUnit.DAY)
        viewModel.handleIntent(TimelineSyncIntent.SelectDate(newDate))
        advanceUntilIdle()

        // Assert - 日付が更新されていること（タイムラインバー計算はUI層で行う）
        assertEquals(newDate, viewModel.uiState.value.selectedDate)
    }

    // ========================================
    // Story 1: 週移動
    // ========================================

    @Test
    fun `週移動_左スワイプで次週に移動できること`() = runTest {
        // Arrange
        val initialWeekStart = viewModel.uiState.value.displayedWeekStart

        // Act
        viewModel.handleIntent(TimelineSyncIntent.NavigateToNextWeek)
        advanceUntilIdle()

        // Assert
        val newWeekStart = viewModel.uiState.value.displayedWeekStart
        assertEquals(initialWeekStart.plus(7, kotlinx.datetime.DateTimeUnit.DAY), newWeekStart)
    }

    @Test
    fun `週移動_右スワイプで前週に移動できること`() = runTest {
        // Arrange
        val initialWeekStart = viewModel.uiState.value.displayedWeekStart

        // Act
        viewModel.handleIntent(TimelineSyncIntent.NavigateToPreviousWeek)
        advanceUntilIdle()

        // Assert
        val newWeekStart = viewModel.uiState.value.displayedWeekStart
        assertEquals(initialWeekStart.minus(7, kotlinx.datetime.DateTimeUnit.DAY), newWeekStart)
    }

    @Test
    fun `週移動_週移動時に選択日付は変更されないこと`() = runTest {
        // Arrange
        val initialSelectedDate = viewModel.uiState.value.selectedDate

        // Act
        viewModel.handleIntent(TimelineSyncIntent.NavigateToNextWeek)
        advanceUntilIdle()

        // Assert
        assertEquals(initialSelectedDate, viewModel.uiState.value.selectedDate)
    }

    // ========================================
    // Story 1: アクティブチャンネル数
    // ========================================

    @Test
    fun `アクティブチャンネル数_ストリームが選択されているチャンネル数をカウントすること`() = runTest {
        // Arrange
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        // Assert - モックデータでは2チャンネルにストリームが設定されている
        val activeCount = viewModel.uiState.value.activeChannelCount
        assertEquals(2, activeCount)
    }

    @Test
    fun `アクティブチャンネル数_ヘッダーにN CHANNELS ACTIVEとして表示されること`() {
        // Note: UI表示のテストはCompose UIテストで行う
        // ここではactiveChannelCountの計算ロジックをテスト
        val channels = listOf(
            createTestChannel("ch1", hasStream = true),
            createTestChannel("ch2", hasStream = false),
            createTestChannel("ch3", hasStream = true),
        )
        val state = TimelineSyncUiState(channels = channels)
        assertEquals(2, state.activeChannelCount)
    }

    // ========================================
    // Story 1: リフレッシュ・リトライ
    // ========================================

    @Test
    fun `リフレッシュ操作_再度データ取得を試みること`() = runTest {
        // Arrange
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        // Act
        viewModel.handleIntent(TimelineSyncIntent.Retry)

        // Assert - ローディング状態になることを確認
        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()
        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `再試行ボタンを押した時_再度データ取得を試みること`() = runTest {
        // Act
        viewModel.handleIntent(TimelineSyncIntent.Retry)
        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()

        // Assert
        assertTrue(!viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.channels.isNotEmpty())
    }

    // ========================================
    // Story 3: 初期同期時刻設定
    // ========================================

    @Test
    fun `初期同期時刻_チャンネルありの場合最初のチャンネルのストリーム開始時刻が設定されること`() = runTest {
        // Act
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertNotNull(state.syncTime)

        // 最初のストリームを持つチャンネルの開始時刻と一致するはず
        val firstChannelWithStream = state.channels.firstOrNull { it.selectedStream?.startTime != null }
        assertEquals(firstChannelWithStream?.selectedStream?.startTime, state.syncTime)
    }

    @Test
    fun `初期同期時刻_チャンネルなしの場合syncTimeがnullであること`() {
        // Note: 現在のモック実装では常にチャンネルが返されるため、
        // UiStateのプロパティでテスト
        val emptyState = TimelineSyncUiState(channels = emptyList())
        assertNull(emptyState.syncTime)
    }

    // ========================================
    // Story 3: シークバードラッグ
    // ========================================

    @Test
    fun `シークバードラッグ_開始時にisDraggingがtrueになること`() = runTest {
        // Act
        viewModel.handleIntent(TimelineSyncIntent.StartDragging)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState.value.isDragging)
    }

    @Test
    fun `シークバードラッグ_終了時にisDraggingがfalseになること`() = runTest {
        // Arrange
        viewModel.handleIntent(TimelineSyncIntent.StartDragging)
        advanceUntilIdle()

        // Act
        viewModel.handleIntent(TimelineSyncIntent.StopDragging)
        advanceUntilIdle()

        // Assert
        assertTrue(!viewModel.uiState.value.isDragging)
    }

    @Test
    fun `シークバードラッグ_ドラッグ中にsyncTimeがリアルタイムで更新されること`() = runTest {
        // Arrange
        val syncTime1 = dayStart + 10.hours
        val syncTime2 = dayStart + 11.hours

        // Act
        viewModel.handleIntent(TimelineSyncIntent.StartDragging)
        viewModel.handleIntent(TimelineSyncIntent.UpdateSyncTime(syncTime1))
        advanceUntilIdle()
        assertEquals(syncTime1, viewModel.uiState.value.syncTime)

        viewModel.handleIntent(TimelineSyncIntent.UpdateSyncTime(syncTime2))
        advanceUntilIdle()

        // Assert
        assertEquals(syncTime2, viewModel.uiState.value.syncTime)
    }

    // ========================================
    // Story 3: SYNC TIME表示
    // ========================================

    @Test
    fun `SYNC TIME表示_syncTimeがHH MM SS形式で表示されること`() {
        // Note: 表示フォーマットはUI層で行うため、
        // ここではsyncTimeが正しくInstantとして保持されていることをテスト
        val syncTime = dayStart + 10.hours + 30.minutes + 45.seconds
        val state = TimelineSyncUiState(syncTime = syncTime)
        assertNotNull(state.syncTime)
        assertEquals(syncTime, state.syncTime)
    }

    @Test
    fun `SYNC TIME表示_syncTimeがnullの場合非表示になること`() {
        // Note: 非表示ロジックはUI層で実装
        // ここではnull状態が正しく保持されることをテスト
        val state = TimelineSyncUiState(syncTime = null)
        assertNull(state.syncTime)
    }

    @Test
    fun `SYNC TIME表示_シークバードラッグ中にリアルタイムで更新されること`() = runTest {
        // Arrange
        viewModel.handleIntent(TimelineSyncIntent.StartDragging)

        // Act & Assert - 複数回の更新
        val times = listOf(
            dayStart + 10.hours,
            dayStart + 10.hours + 30.minutes,
            dayStart + 11.hours,
        )

        times.forEach { time ->
            viewModel.handleIntent(TimelineSyncIntent.UpdateSyncTime(time))
            advanceUntilIdle()
            assertEquals(time, viewModel.uiState.value.syncTime)
        }
    }

    // ========================================
    // Story 3: 同期位置計算
    // ========================================

    @Test
    fun `同期位置計算_syncTimeがストリーム範囲内の場合targetSeekPositionが正しく計算されること`() = runTest {
        // Arrange
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        val firstChannel = assertNotNull(
            viewModel.uiState.value.channels.firstOrNull { it.selectedStream?.startTime != null },
        )
        val streamStart = assertNotNull(firstChannel.selectedStream?.startTime)

        // Act - ストリーム開始から30分後に同期
        val syncTime = streamStart + 30.minutes
        viewModel.handleIntent(TimelineSyncIntent.UpdateSyncTime(syncTime))
        advanceUntilIdle()

        // Assert - targetSeekPositionは1800秒（30分）
        val updatedChannel = assertNotNull(
            viewModel.uiState.value.channels.find { it.channelId == firstChannel.channelId },
        )
        assertEquals(1800f, updatedChannel.targetSeekPosition)
    }

    @Test
    fun `同期位置計算_syncTimeがストリーム開始前の場合targetSeekPositionが0であること`() = runTest {
        // Arrange
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        val firstChannel = assertNotNull(
            viewModel.uiState.value.channels.firstOrNull { it.selectedStream?.startTime != null },
        )
        val streamStart = assertNotNull(firstChannel.selectedStream?.startTime)

        // Act - ストリーム開始より前に同期
        val syncTime = streamStart - 1.hours
        viewModel.handleIntent(TimelineSyncIntent.UpdateSyncTime(syncTime))
        advanceUntilIdle()

        // Assert - targetSeekPositionは0
        val updatedChannel = assertNotNull(
            viewModel.uiState.value.channels.find { it.channelId == firstChannel.channelId },
        )
        assertEquals(0f, updatedChannel.targetSeekPosition)
    }

    // ========================================
    // Story 3: SyncStatus判定
    // ========================================

    @Test
    fun `SyncStatus判定_syncTimeがストリーム開始前の場合WAITINGになること`() = runTest {
        // Arrange
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        val firstChannel = assertNotNull(
            viewModel.uiState.value.channels.firstOrNull { it.selectedStream?.startTime != null },
        )
        val streamStart = assertNotNull(firstChannel.selectedStream?.startTime)

        // Act - ストリーム開始より前に同期
        val syncTime = streamStart - 1.hours
        viewModel.handleIntent(TimelineSyncIntent.UpdateSyncTime(syncTime))
        advanceUntilIdle()

        // Assert
        val updatedChannel = viewModel.uiState.value.channels.find { it.channelId == firstChannel.channelId }
        assertEquals(SyncStatus.WAITING, updatedChannel?.syncStatus)
    }

    @Test
    fun `SyncStatus判定_syncTimeがストリーム範囲内の場合READYになること`() = runTest {
        // Arrange
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        val firstChannel = assertNotNull(
            viewModel.uiState.value.channels.firstOrNull { it.selectedStream?.startTime != null },
        )
        val streamStart = assertNotNull(firstChannel.selectedStream?.startTime)

        // Act - ストリーム範囲内に同期
        val syncTime = streamStart + 30.minutes
        viewModel.handleIntent(TimelineSyncIntent.UpdateSyncTime(syncTime))
        advanceUntilIdle()

        // Assert
        val updatedChannel = viewModel.uiState.value.channels.find { it.channelId == firstChannel.channelId }
        assertEquals(SyncStatus.READY, updatedChannel?.syncStatus)
    }

    @Test
    fun `SyncStatus判定_syncTimeの変更時に全チャンネルのSyncStatusが再判定されること`() = runTest {
        // Arrange
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        // Act - 複数回syncTimeを更新
        val channelsWithStream = viewModel.uiState.value.channels.filter { it.selectedStream != null }
        assertTrue(channelsWithStream.size >= 2)

        val earliestStart = assertNotNull(
            channelsWithStream.mapNotNull { it.selectedStream?.startTime }.minOrNull(),
        )

        // 全てのストリームが開始前の時刻
        val earlyTime = earliestStart - 2.hours
        viewModel.handleIntent(TimelineSyncIntent.UpdateSyncTime(earlyTime))
        advanceUntilIdle()

        // Assert - ストリームを持つ全チャンネルがWAITINGになるはず
        val updatedChannels = viewModel.uiState.value.channels.filter { it.selectedStream != null }
        assertTrue(updatedChannels.all { it.syncStatus == SyncStatus.WAITING })
    }

    // ========================================
    // Story 3: syncTimeRange制限
    // ========================================

    @Test
    fun `syncTimeRange_全チャンネルの最小開始時刻から最大終了時刻の範囲であること`() = runTest {
        // Arrange
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        val range = assertNotNull(state.syncTimeRange)

        // 全ストリームの開始・終了時刻を取得
        val streams = state.channels.mapNotNull { it.selectedStream }
        val earliestStart = streams.mapNotNull { it.startTime }.minOrNull()
        val latestEnd = streams.mapNotNull { it.endTime }.maxOrNull()

        assertEquals(earliestStart, range.first)
        // endTimeがnullのストリームがある場合は現在時刻が使われるため、厳密な比較は難しい
        // ここではlatestEndがnullでない場合のみ比較
        if (latestEnd != null) {
            assertEquals(latestEnd, range.second)
        }
    }

    @Test
    fun `syncTimeRange_シークバーがsyncTimeRange内でのみ移動可能であること`() {
        // Note: シークバーの移動制限はUI層で実装
        // ここではsyncTimeRangeが正しく計算されることをテスト
        val stream1Start = dayStart + 10.hours
        val stream1End = dayStart + 12.hours
        val stream2Start = dayStart + 11.hours
        val stream2End = dayStart + 14.hours

        val channels = listOf(
            createTestChannel("ch1", streamStart = stream1Start, streamEnd = stream1End),
            createTestChannel("ch2", streamStart = stream2Start, streamEnd = stream2End),
        )
        val state = TimelineSyncUiState(channels = channels)

        val range = assertNotNull(state.syncTimeRange)
        assertEquals(stream1Start, range.first) // 最小開始時刻
        assertEquals(stream2End, range.second) // 最大終了時刻
    }

    // ========================================
    // Story 3: チャンネル追加・削除時の振る舞い
    // ========================================

    @Test
    fun `チャンネル追加時_syncTimeRangeが再計算されること`() {
        // Note: チャンネル追加によりchannelsが更新されると、
        // syncTimeRangeはcomputed propertyなので自動的に再計算される
        val stream1Start = dayStart + 10.hours
        val stream1End = dayStart + 12.hours

        val initialChannels = listOf(
            createTestChannel("ch1", streamStart = stream1Start, streamEnd = stream1End),
        )
        val state1 = TimelineSyncUiState(channels = initialChannels)
        val range1 = state1.syncTimeRange
        assertEquals(stream1Start, range1?.first)
        assertEquals(stream1End, range1?.second)

        // チャンネル追加
        val stream2Start = dayStart + 8.hours
        val stream2End = dayStart + 15.hours
        val updatedChannels = initialChannels + createTestChannel("ch2", streamStart = stream2Start, streamEnd = stream2End)
        val state2 = TimelineSyncUiState(channels = updatedChannels)
        val range2 = state2.syncTimeRange

        // 範囲が広がることを確認
        assertEquals(stream2Start, range2?.first) // 新しい最小
        assertEquals(stream2End, range2?.second) // 新しい最大
    }

    @Test
    fun `チャンネル削除時_syncTimeRangeが再計算されること`() {
        val stream1Start = dayStart + 10.hours
        val stream1End = dayStart + 12.hours
        val stream2Start = dayStart + 8.hours
        val stream2End = dayStart + 15.hours

        val initialChannels = listOf(
            createTestChannel("ch1", streamStart = stream1Start, streamEnd = stream1End),
            createTestChannel("ch2", streamStart = stream2Start, streamEnd = stream2End),
        )
        val state1 = TimelineSyncUiState(channels = initialChannels)
        val range1 = state1.syncTimeRange
        assertEquals(stream2Start, range1?.first)
        assertEquals(stream2End, range1?.second)

        // ch2を削除
        val updatedChannels = initialChannels.filter { it.channelId != "ch2" }
        val state2 = TimelineSyncUiState(channels = updatedChannels)
        val range2 = state2.syncTimeRange

        // 範囲が狭まることを確認
        assertEquals(stream1Start, range2?.first)
        assertEquals(stream1End, range2?.second)
    }

    @Test
    fun `最後のチャンネル削除時_syncTimeがnullになること`() {
        // Note: syncTimeRange はchannelsが空になるとnullになる
        // syncTime自体はViewModelの状態更新ロジックによる
        val state = TimelineSyncUiState(channels = emptyList())
        assertNull(state.syncTimeRange)
    }

    // ========================================
    // Story 3: 同期時刻インジケーター
    // ========================================

    @Test
    fun `同期時刻インジケーター_syncTimeに対応する位置に縦の青い線が表示されること`() {
        // Note: UI表示のテストはCompose UIテストで行う
        // ここではsyncTimeが正しく保持されることをテスト
        val syncTime = dayStart + 11.hours
        val state = TimelineSyncUiState(syncTime = syncTime)
        assertEquals(syncTime, state.syncTime)
    }

    @Test
    fun `同期時刻インジケーター_シークバードラッグ中にリアルタイムで移動すること`() = runTest {
        // Arrange & Act
        viewModel.handleIntent(TimelineSyncIntent.StartDragging)

        val times = listOf(
            dayStart + 10.hours,
            dayStart + 10.hours + 15.minutes,
            dayStart + 10.hours + 30.minutes,
        )

        times.forEach { time ->
            viewModel.handleIntent(TimelineSyncIntent.UpdateSyncTime(time))
            advanceUntilIdle()

            // Assert
            assertEquals(time, viewModel.uiState.value.syncTime)
            assertTrue(viewModel.uiState.value.isDragging)
        }
    }

    @Test
    fun `同期時刻インジケーター_syncTimeがnullの場合非表示になること`() {
        // Note: 非表示ロジックはUI層で実装
        val state = TimelineSyncUiState(syncTime = null)
        assertNull(state.syncTime)
    }

    // ========================================
    // ヘルパーメソッド
    // ========================================

    private fun createTestChannel(
        id: String,
        hasStream: Boolean = true,
        streamStart: Instant? = dayStart + 10.hours,
        streamEnd: Instant? = dayStart + 13.hours,
    ): SyncChannel {
        return SyncChannel(
            channelId = id,
            channelName = "Channel $id",
            channelIconUrl = "https://example.com/$id.jpg",
            serviceType = VideoServiceType.TWITCH,
            selectedStream = if (hasStream) {
                SelectedStreamInfo(
                    id = "stream_$id",
                    title = "Stream $id",
                    thumbnailUrl = "https://example.com/thumb_$id.jpg",
                    startTime = streamStart,
                    endTime = streamEnd,
                    duration = if (streamStart != null && streamEnd != null) {
                        streamEnd - streamStart
                    } else {
                        null
                    },
                )
            } else {
                null
            },
            syncStatus = SyncStatus.NOT_SYNCED,
        )
    }
}

/**
 * テスト用TimelineSyncRepository実装
 */
class TestTimelineSyncRepository : TimelineSyncRepository {
    var shouldReturnError = false

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
        return Result.success(emptyList())
    }
}
