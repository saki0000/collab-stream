@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package org.example.project.feature.timeline_sync

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.project.domain.model.SelectedStreamInfo
import org.example.project.domain.model.FollowedChannel
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.ChannelFollowRepository

/**
 * ViewModelテスト: TimelineSyncViewModel - 外部アプリ連携
 *
 * Story 4: 外部アプリ連携（DeepLink）の振る舞いを定義
 *
 * Specification: feature/timeline_sync/SPECIFICATION.md
 * Story Issue: #54
 * Epic: Timeline Sync (EPIC-002)
 */
class ExternalAppNavigationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: FakeTimelineSyncRepository
    private lateinit var mockDataSource: FakeTwitchSearchDataSource
    private lateinit var mockYouTubeDataSource: FakeYouTubeSearchDataSource
    private lateinit var mockChannelFollowRepository: FakeChannelFollowRepository
    private lateinit var viewModel: TimelineSyncViewModel

    private val baseTime = Instant.parse("2024-01-01T10:00:00Z")

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = FakeTimelineSyncRepository()
        mockDataSource = FakeTwitchSearchDataSource()
        mockYouTubeDataSource = FakeYouTubeSearchDataSource()
        mockChannelFollowRepository = FakeChannelFollowRepository()
        val channelSearchUseCase = org.example.project.domain.usecase.ChannelSearchUseCase(mockDataSource, mockYouTubeDataSource)
        viewModel = TimelineSyncViewModel(mockRepository, channelSearchUseCase, mockChannelFollowRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================
    // OpenExternalApp Intent - SyncStatus更新
    // ========================================

    @Test
    fun `OpenExternalApp_READY状態のチャンネルでSyncStatusがOPENEDに更新されること`() = runTest {
        // Arrange
        setupViewModelWithChannels()

        // Act
        viewModel.handleIntent(TimelineSyncIntent.OpenExternalApp("yt_ch"))
        advanceUntilIdle()

        // Assert
        val channel = viewModel.uiState.value.channels.find { it.channelId == "yt_ch" }
        assertNotNull(channel)
        assertEquals(SyncStatus.OPENED, channel.syncStatus)
    }

    @Test
    fun `OpenExternalApp_他のチャンネルのSyncStatusは変更されないこと`() = runTest {
        // Arrange
        setupViewModelWithChannels()

        // Act
        viewModel.handleIntent(TimelineSyncIntent.OpenExternalApp("yt_ch"))
        advanceUntilIdle()

        // Assert
        val twitchChannel = viewModel.uiState.value.channels.find { it.channelId == "tw_ch" }
        assertNotNull(twitchChannel)
        assertEquals(SyncStatus.READY, twitchChannel.syncStatus)
    }

    @Test
    fun `OpenExternalApp_存在しないチャンネルIDでは状態が変更されないこと`() = runTest {
        // Arrange
        setupViewModelWithChannels()
        val channelsBefore = viewModel.uiState.value.channels

        // Act
        viewModel.handleIntent(TimelineSyncIntent.OpenExternalApp("non_existent"))
        advanceUntilIdle()

        // Assert
        assertEquals(channelsBefore, viewModel.uiState.value.channels)
    }

    // ========================================
    // OpenExternalApp Intent - SideEffect発行
    // ========================================

    @Test
    fun `OpenExternalApp_NavigateToExternalApp SideEffectが発行されること`() = runTest {
        // Arrange
        setupViewModelWithChannels()

        var sideEffect: TimelineSyncSideEffect? = null
        val collectJob = launch {
            sideEffect = viewModel.sideEffect.first()
        }

        // Act
        viewModel.handleIntent(TimelineSyncIntent.OpenExternalApp("yt_ch"))
        advanceUntilIdle()

        // Assert
        assertNotNull(sideEffect)
        assertTrue(sideEffect is TimelineSyncSideEffect.NavigateToExternalApp)
        collectJob.cancel()
    }

    @Test
    fun `OpenExternalApp_YouTube_正しいDeepLink URIがSideEffectに含まれること`() = runTest {
        // Arrange
        setupViewModelWithChannels()

        var sideEffect: TimelineSyncSideEffect? = null
        val collectJob = launch {
            sideEffect = viewModel.sideEffect.first()
        }

        // Act
        viewModel.handleIntent(TimelineSyncIntent.OpenExternalApp("yt_ch"))
        advanceUntilIdle()

        // Assert
        val navEffect = sideEffect as TimelineSyncSideEffect.NavigateToExternalApp
        assertTrue(navEffect.deepLinkUri.startsWith("youtube://watch?v=yt_video_001"))
        collectJob.cancel()
    }

    @Test
    fun `OpenExternalApp_YouTube_正しいフォールバックURLがSideEffectに含まれること`() = runTest {
        // Arrange
        setupViewModelWithChannels()

        var sideEffect: TimelineSyncSideEffect? = null
        val collectJob = launch {
            sideEffect = viewModel.sideEffect.first()
        }

        // Act
        viewModel.handleIntent(TimelineSyncIntent.OpenExternalApp("yt_ch"))
        advanceUntilIdle()

        // Assert
        val navEffect = sideEffect as TimelineSyncSideEffect.NavigateToExternalApp
        assertTrue(navEffect.fallbackUrl.startsWith("https://www.youtube.com/watch?v=yt_video_001"))
        collectJob.cancel()
    }

    @Test
    fun `OpenExternalApp_Twitch_正しいDeepLink URIがSideEffectに含まれること`() = runTest {
        // Arrange
        setupViewModelWithChannels()

        var sideEffect: TimelineSyncSideEffect? = null
        val collectJob = launch {
            sideEffect = viewModel.sideEffect.first()
        }

        // Act
        viewModel.handleIntent(TimelineSyncIntent.OpenExternalApp("tw_ch"))
        advanceUntilIdle()

        // Assert
        val navEffect = sideEffect as TimelineSyncSideEffect.NavigateToExternalApp
        assertTrue(navEffect.deepLinkUri.startsWith("twitch://video/tw_video_001"))
        collectJob.cancel()
    }

    // ========================================
    // OPENED状態維持
    // ========================================

    @Test
    fun `OPENED状態維持_syncTime変更後もREADY範囲内ならOPENEDが維持されること`() = runTest {
        // Arrange
        setupViewModelWithChannels()
        viewModel.handleIntent(TimelineSyncIntent.OpenExternalApp("yt_ch"))
        advanceUntilIdle()

        // OPENEDに更新されたことを確認
        assertEquals(
            SyncStatus.OPENED,
            viewModel.uiState.value.channels.find { it.channelId == "yt_ch" }?.syncStatus,
        )

        // Act: syncTimeを同じストリーム範囲内で変更
        val newSyncTime = baseTime + 2.hours // まだ10:00-13:00の範囲内
        viewModel.handleIntent(TimelineSyncIntent.UpdateSyncTime(newSyncTime))
        advanceUntilIdle()

        // Assert: OPENEDが維持される
        val channel = viewModel.uiState.value.channels.find { it.channelId == "yt_ch" }
        assertNotNull(channel)
        assertEquals(SyncStatus.OPENED, channel.syncStatus)
    }

    @Test
    fun `OPENED状態維持_syncTimeがストリーム範囲外になるとWAITINGになること`() = runTest {
        // Arrange
        setupViewModelWithChannels()
        viewModel.handleIntent(TimelineSyncIntent.OpenExternalApp("yt_ch"))
        advanceUntilIdle()

        // Act: syncTimeをストリーム開始前に変更
        val beforeStart = baseTime - 1.hours // 09:00 < 10:00
        viewModel.handleIntent(TimelineSyncIntent.UpdateSyncTime(beforeStart))
        advanceUntilIdle()

        // Assert: WAITINGになる
        val channel = viewModel.uiState.value.channels.find { it.channelId == "yt_ch" }
        assertNotNull(channel)
        assertEquals(SyncStatus.WAITING, channel.syncStatus)
    }

    @Test
    fun `OPENED状態_再度Openボタンをタップできること`() = runTest {
        // Arrange
        setupViewModelWithChannels()
        viewModel.handleIntent(TimelineSyncIntent.OpenExternalApp("yt_ch"))
        advanceUntilIdle()

        var sideEffectCount = 0
        val collectJob = launch {
            viewModel.sideEffect.collect { sideEffectCount++ }
        }

        // Act: 再度Openボタン
        viewModel.handleIntent(TimelineSyncIntent.OpenExternalApp("yt_ch"))
        advanceUntilIdle()

        // Assert: SideEffectが再度発行される
        assertTrue(sideEffectCount >= 1)
        collectJob.cancel()
    }

    // ========================================
    // ヘルパー
    // ========================================

    /**
     * テスト用にViewModelにチャンネルとsyncTimeを設定する。
     */
    private fun setupViewModelWithChannels() {
        val channels = listOf(
            SyncChannel(
                channelId = "yt_ch",
                channelName = "YouTube Channel",
                channelIconUrl = "",
                serviceType = VideoServiceType.YOUTUBE,
                selectedStream = SelectedStreamInfo(
                    id = "yt_video_001",
                    title = "Stream",
                    thumbnailUrl = "",
                    startTime = baseTime,
                    endTime = baseTime + 3.hours,
                    duration = 3.hours,
                ),
                syncStatus = SyncStatus.READY,
                targetSeekPosition = 3600f, // 1時間
            ),
            SyncChannel(
                channelId = "tw_ch",
                channelName = "Twitch Channel",
                channelIconUrl = "",
                serviceType = VideoServiceType.TWITCH,
                selectedStream = SelectedStreamInfo(
                    id = "tw_video_001",
                    title = "Stream",
                    thumbnailUrl = "",
                    startTime = baseTime,
                    endTime = baseTime + 3.hours,
                    duration = 3.hours,
                ),
                syncStatus = SyncStatus.READY,
                targetSeekPosition = 3600f,
            ),
        )

        // UiStateを直接設定（テスト用）
        val field = viewModel.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<TimelineSyncUiState>
        stateFlow.value = TimelineSyncUiState(
            channels = channels,
            syncTime = baseTime + 1.hours,
        )
    }
}
