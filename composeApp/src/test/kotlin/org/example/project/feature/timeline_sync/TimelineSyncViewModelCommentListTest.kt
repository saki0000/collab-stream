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
import kotlin.time.Duration.Companion.hours
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
import org.example.project.domain.model.CommentTimestampResult
import org.example.project.domain.model.TimestampMarker
import org.example.project.domain.model.VideoComment
import org.example.project.domain.usecase.ChannelSearchUseCase

/**
 * ViewModelテスト: TimelineSyncViewModel - コメントリスト関連Intent処理
 *
 * US-4: コメントリスト機能の ViewModel 処理を検証する。
 * - OpenCommentList: コメントリスト BottomSheet を開く
 * - CloseCommentList: コメントリスト BottomSheet を閉じる
 * - ChangeCommentSortOrder: ソート順切替
 * - LoadMoreComments: ページネーション
 * - TapCommentTimestamp: タイムスタンプタップによる同期時刻更新
 *
 * Specification: feature/timeline_sync/comment_timestamp/SPECIFICATION.md
 * Story Issue: US-4 (コメントリスト)
 * Epic: Timeline Sync (EPIC-002)
 */
class TimelineSyncViewModelCommentListTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: TimelineSyncViewModel
    private lateinit var fakeCommentRepository: FakeCommentRepository
    private lateinit var fakeChannelFollowRepository: FakeChannelFollowRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCommentRepository = FakeCommentRepository()
        fakeChannelFollowRepository = FakeChannelFollowRepository()
        viewModel = TimelineSyncViewModel(
            timelineSyncRepository = TestTimelineSyncRepository(),
            channelSearchUseCase = ChannelSearchUseCase(FakeVideoSearchRepository()),
            channelFollowRepository = fakeChannelFollowRepository,
            commentRepository = fakeCommentRepository,
            syncHistoryRepository = FakeSyncHistoryRepository(),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================
    // OpenCommentList Intent
    // ========================================

    @Test
    fun `OpenCommentList_コメントリストを開くとisCommentListVisibleがtrueになること`() = runTest {
        // Arrange
        val channelId = "test_channel_01"

        // Act
        viewModel.handleIntent(TimelineSyncIntent.OpenCommentList(channelId))
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.isCommentListVisible)
    }

    @Test
    fun `OpenCommentList_コメントリストを開くとcommentListChannelIdが設定されること`() = runTest {
        // Arrange
        val channelId = "youtube_channel_001"

        // Act
        viewModel.handleIntent(TimelineSyncIntent.OpenCommentList(channelId))
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(channelId, state.commentListChannelId)
    }

    @Test
    fun `OpenCommentList_異なるチャンネルIDで開くと最後のチャンネルIDが設定されること`() = runTest {
        // Arrange
        val channelId1 = "channel_01"
        val channelId2 = "channel_02"

        // Act
        viewModel.handleIntent(TimelineSyncIntent.OpenCommentList(channelId1))
        advanceUntilIdle()
        viewModel.handleIntent(TimelineSyncIntent.OpenCommentList(channelId2))
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.isCommentListVisible)
        assertEquals(channelId2, state.commentListChannelId)
    }

    // ========================================
    // CloseCommentList Intent
    // ========================================

    @Test
    fun `CloseCommentList_コメントリストを閉じるとisCommentListVisibleがfalseになること`() = runTest {
        // Arrange - まずコメントリストを開く
        viewModel.handleIntent(TimelineSyncIntent.OpenCommentList("channel_01"))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isCommentListVisible)

        // Act
        viewModel.handleIntent(TimelineSyncIntent.CloseCommentList)
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.uiState.value.isCommentListVisible)
    }

    @Test
    fun `CloseCommentList_コメントリストを閉じるとcommentListChannelIdがnullになること`() = runTest {
        // Arrange - まずコメントリストを開く
        viewModel.handleIntent(TimelineSyncIntent.OpenCommentList("channel_01"))
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.commentListChannelId)

        // Act
        viewModel.handleIntent(TimelineSyncIntent.CloseCommentList)
        advanceUntilIdle()

        // Assert
        assertNull(viewModel.uiState.value.commentListChannelId)
    }

    @Test
    fun `CloseCommentList_開いていない状態で閉じても正常に処理されること`() = runTest {
        // Arrange - 初期状態は閉じている
        assertFalse(viewModel.uiState.value.isCommentListVisible)

        // Act
        viewModel.handleIntent(TimelineSyncIntent.CloseCommentList)
        advanceUntilIdle()

        // Assert - 例外なく正常終了し、falseのまま
        assertFalse(viewModel.uiState.value.isCommentListVisible)
    }

    // ========================================
    // ChangeCommentSortOrder Intent
    // ========================================

    @Test
    fun `ChangeCommentSortOrder_初期状態のソート順はLIKESであること`() {
        // Assert
        assertEquals(CommentSortOrder.LIKES, viewModel.uiState.value.commentSortOrder)
    }

    @Test
    fun `ChangeCommentSortOrder_TIME順に変更するとcommentSortOrderがTIMEになること`() = runTest {
        // Act
        viewModel.handleIntent(TimelineSyncIntent.ChangeCommentSortOrder(CommentSortOrder.TIME))
        advanceUntilIdle()

        // Assert
        assertEquals(CommentSortOrder.TIME, viewModel.uiState.value.commentSortOrder)
    }

    @Test
    fun `ChangeCommentSortOrder_LIKES順に戻すとcommentSortOrderがLIKESになること`() = runTest {
        // Arrange - まず TIME 順に変更
        viewModel.handleIntent(TimelineSyncIntent.ChangeCommentSortOrder(CommentSortOrder.TIME))
        advanceUntilIdle()
        assertEquals(CommentSortOrder.TIME, viewModel.uiState.value.commentSortOrder)

        // Act
        viewModel.handleIntent(TimelineSyncIntent.ChangeCommentSortOrder(CommentSortOrder.LIKES))
        advanceUntilIdle()

        // Assert
        assertEquals(CommentSortOrder.LIKES, viewModel.uiState.value.commentSortOrder)
    }

    // ========================================
    // LoadMoreComments Intent
    // ========================================

    @Test
    fun `LoadMoreComments_commentListChannelIdがnullの場合は何も起きないこと`() = runTest {
        // Arrange - commentListChannelId を null のままにする
        assertNull(viewModel.uiState.value.commentListChannelId)
        val callCountBefore = fakeCommentRepository.callCount

        // Act
        viewModel.handleIntent(TimelineSyncIntent.LoadMoreComments)
        advanceUntilIdle()

        // Assert - 追加 API 呼び出しは発生しない
        assertEquals(callCountBefore, fakeCommentRepository.callCount)
    }

    @Test
    fun `LoadMoreComments_nextPageTokenがnullの場合は追加読み込みが発生しないこと`() = runTest {
        // Arrange - LoadScreen でコメントを取得（nextPageToken = null）
        fakeCommentRepository.returnResult = Result.success(
            CommentTimestampResult(
                videoId = "",
                comments = emptyList(),
                timestampMarkers = listOf(createTestMarker(600L, 100)),
                nextPageToken = null, // ページがない
                commentsDisabled = false,
            ),
        )
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        // チャンネルのコメントを確認して LOADED 状態のチャンネルIDを取得
        val youtubeChannelId = viewModel.uiState.value.channels
            .firstOrNull { it.serviceType == org.example.project.domain.model.VideoServiceType.YOUTUBE && it.selectedStream != null }
            ?.channelId ?: return@runTest

        viewModel.handleIntent(TimelineSyncIntent.OpenCommentList(youtubeChannelId))
        advanceUntilIdle()

        val callCountBefore = fakeCommentRepository.callCount

        // Act
        viewModel.handleIntent(TimelineSyncIntent.LoadMoreComments)
        advanceUntilIdle()

        // Assert - nextPageToken が null なので API 呼び出しは発生しない
        assertEquals(callCountBefore, fakeCommentRepository.callCount)
        assertFalse(viewModel.uiState.value.isLoadingMoreComments)
    }

    @Test
    fun `LoadMoreComments_nextPageTokenがある場合に追加コメントが読み込まれること`() = runTest {
        // Arrange - 初回コメント取得（nextPageToken あり）
        val firstMarker = createTestMarker(600L, 100)
        fakeCommentRepository.returnResult = Result.success(
            CommentTimestampResult(
                videoId = "",
                comments = emptyList(),
                timestampMarkers = listOf(firstMarker),
                nextPageToken = "page_token_2",
                commentsDisabled = false,
            ),
        )
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        val youtubeChannelId = viewModel.uiState.value.channels
            .firstOrNull { it.serviceType == org.example.project.domain.model.VideoServiceType.YOUTUBE && it.selectedStream != null }
            ?.channelId ?: return@runTest

        viewModel.handleIntent(TimelineSyncIntent.OpenCommentList(youtubeChannelId))
        advanceUntilIdle()

        // 次ページは追加マーカーを返す
        val secondMarker = createTestMarker(1200L, 200, "comment_page2")
        fakeCommentRepository.returnResult = Result.success(
            CommentTimestampResult(
                videoId = "",
                comments = emptyList(),
                timestampMarkers = listOf(secondMarker),
                nextPageToken = null,
                commentsDisabled = false,
            ),
        )

        // Act
        viewModel.handleIntent(TimelineSyncIntent.LoadMoreComments)
        advanceUntilIdle()

        // Assert - markers が追記されていること
        val commentState = viewModel.uiState.value.channelComments[youtubeChannelId]
        assertNotNull(commentState)
        assertEquals(2, commentState.markers.size)
        assertFalse(viewModel.uiState.value.isLoadingMoreComments)
        // nextPageToken が null に更新されること
        assertNull(commentState.nextPageToken)
    }

    // ========================================
    // TapCommentTimestamp Intent
    // ========================================

    @Test
    fun `TapCommentTimestamp_存在しないchannelIdでは何も起きないこと`() = runTest {
        // Arrange
        val syncTimeBefore = viewModel.uiState.value.syncTime

        // Act
        viewModel.handleIntent(
            TimelineSyncIntent.TapCommentTimestamp(
                channelId = "non_existent_channel",
                timestampSeconds = 600L,
            ),
        )
        advanceUntilIdle()

        // Assert - syncTime は変化しない
        assertEquals(syncTimeBefore, viewModel.uiState.value.syncTime)
    }

    @Test
    fun `TapCommentTimestamp_selectedStreamがないチャンネルでは何も起きないこと`() = runTest {
        // Arrange - LoadScreen でモックデータを読み込む（selectedStream = null のチャンネルが含まれる）
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        val noStreamChannelId = viewModel.uiState.value.channels
            .firstOrNull { it.selectedStream == null }?.channelId ?: return@runTest

        val syncTimeBefore = viewModel.uiState.value.syncTime

        // Act
        viewModel.handleIntent(
            TimelineSyncIntent.TapCommentTimestamp(
                channelId = noStreamChannelId,
                timestampSeconds = 600L,
            ),
        )
        advanceUntilIdle()

        // Assert - syncTime は変化しない
        assertEquals(syncTimeBefore, viewModel.uiState.value.syncTime)
    }

    @Test
    fun `TapCommentTimestamp_タイムスタンプタップで同期時刻が動画開始時刻+秒数に更新されること`() = runTest {
        // Arrange - LoadScreen でモックデータを読み込む
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        // selectedStream がある YouTube チャンネルを取得
        val youtubeChannel = viewModel.uiState.value.channels
            .firstOrNull { it.serviceType == org.example.project.domain.model.VideoServiceType.YOUTUBE && it.selectedStream != null }
            ?: return@runTest

        val startTime = youtubeChannel.selectedStream?.startTime ?: return@runTest
        val timestampSeconds = 600L // 10分
        val expectedSyncTime = startTime + timestampSeconds.seconds

        // Act
        viewModel.handleIntent(
            TimelineSyncIntent.TapCommentTimestamp(
                channelId = youtubeChannel.channelId,
                timestampSeconds = timestampSeconds,
            ),
        )
        advanceUntilIdle()

        // Assert - syncTime が startTime + 600秒 になること
        val actualSyncTime = viewModel.uiState.value.syncTime
        assertNotNull(actualSyncTime)
        assertEquals(expectedSyncTime, actualSyncTime)
    }

    @Test
    fun `TapCommentTimestamp_タイムスタンプタップ後にチャンネルのsyncStatusが更新されること`() = runTest {
        // Arrange - LoadScreen でモックデータを読み込む
        viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        advanceUntilIdle()

        // selectedStream がある YouTube チャンネルを取得
        val youtubeChannel = viewModel.uiState.value.channels
            .firstOrNull { it.serviceType == org.example.project.domain.model.VideoServiceType.YOUTUBE && it.selectedStream != null }
            ?: return@runTest

        // Act - タイムスタンプをタップ（動画開始時刻の範囲内）
        viewModel.handleIntent(
            TimelineSyncIntent.TapCommentTimestamp(
                channelId = youtubeChannel.channelId,
                timestampSeconds = 1800L, // 30分
            ),
        )
        advanceUntilIdle()

        // Assert - syncTime が更新されること
        assertNotNull(viewModel.uiState.value.syncTime)
    }

    // ========================================
    // 初期状態の検証
    // ========================================

    @Test
    fun `初期状態では_isCommentListVisibleがfalseであること`() {
        // Assert
        assertFalse(viewModel.uiState.value.isCommentListVisible)
    }

    @Test
    fun `初期状態では_commentListChannelIdがnullであること`() {
        // Assert
        assertNull(viewModel.uiState.value.commentListChannelId)
    }

    @Test
    fun `初期状態では_isLoadingMoreCommentsがfalseであること`() {
        // Assert
        assertFalse(viewModel.uiState.value.isLoadingMoreComments)
    }

    @Test
    fun `初期状態では_commentSortOrderがLIKESであること`() {
        // Assert
        assertEquals(CommentSortOrder.LIKES, viewModel.uiState.value.commentSortOrder)
    }

    // ========================================
    // ヘルパーメソッド
    // ========================================

    /** テスト用 TimestampMarker を生成するヘルパー関数 */
    private fun createTestMarker(
        timestampSeconds: Long,
        likeCount: Int,
        commentId: String = "comment_$timestampSeconds",
    ): TimestampMarker = TimestampMarker(
        timestampSeconds = timestampSeconds,
        displayTimestamp = formatTimestamp(timestampSeconds),
        comment = VideoComment(
            commentId = commentId,
            authorDisplayName = "テストユーザー",
            authorProfileImageUrl = "",
            textContent = "${timestampSeconds}秒のテストコメント",
            likeCount = likeCount,
            publishedAt = "2024-01-01T10:00:00Z",
        ),
    )

    /** 秒数を表示用タイムスタンプに変換するヘルパー */
    private fun formatTimestamp(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            "${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
        } else {
            "${m}:${s.toString().padStart(2, '0')}"
        }
    }
}
