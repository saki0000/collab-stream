@file:OptIn(kotlin.time.ExperimentalTime::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package org.example.project.feature.timeline_sync.sync_history

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.project.domain.model.SavedChannelInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncHistory
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.HistorySortOrder
import org.example.project.domain.repository.SyncHistoryRepository

/**
 * SyncHistoryListViewModelのテスト。
 *
 * Specification: feature/timeline_sync/sync_history/SPECIFICATION.md
 * Story: EPIC-003 US-3 (履歴一覧表示)
 */
@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class SyncHistoryListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================
    // テスト用フィクスチャ
    // ========================================

    private val sampleHistories = listOf(
        SyncHistory(
            id = "hist-1",
            name = "Apex大会グループ",
            channels = listOf(
                SavedChannelInfo("ch1", "チャンネルA", "", VideoServiceType.TWITCH),
                SavedChannelInfo("ch2", "チャンネルB", "", VideoServiceType.YOUTUBE),
            ),
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            lastUsedAt = Instant.parse("2024-01-15T10:00:00Z"),
            usageCount = 5,
        ),
        SyncHistory(
            id = "hist-2",
            name = null,
            channels = listOf(
                SavedChannelInfo("ch3", "チャンネルC", "", VideoServiceType.TWITCH),
                SavedChannelInfo("ch4", "チャンネルD", "", VideoServiceType.TWITCH),
            ),
            createdAt = Instant.parse("2024-01-05T00:00:00Z"),
            lastUsedAt = Instant.parse("2024-01-10T15:00:00Z"),
            usageCount = 2,
        ),
    )

    // ========================================
    // 初期状態
    // ========================================

    @Test
    fun `初期状態_isLoadingがfalseであること`() {
        // Arrange
        val viewModel = createViewModel()

        // Assert
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `初期状態_historiesが空であること`() {
        // Arrange
        val viewModel = createViewModel()

        // Assert
        assertTrue(viewModel.uiState.value.histories.isEmpty())
    }

    @Test
    fun `初期状態_sortOrderがLAST_USEDであること`() {
        // Arrange
        val viewModel = createViewModel()

        // Assert
        assertEquals(HistorySortOrder.LAST_USED, viewModel.uiState.value.sortOrder)
    }

    // ========================================
    // LoadScreen
    // ========================================

    @Test
    fun `LoadScreen_履歴取得成功時にhistoriesに値が設定されること`() = runTest {
        // Arrange
        val repository = FakeSyncHistoryRepository(initialHistories = sampleHistories)
        val viewModel = createViewModel(repository)

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.LoadScreen)
        advanceUntilIdle() // コルーチン完了を待つ

        // Assert
        val histories = viewModel.uiState.value.histories
        assertEquals(2, histories.size)
        assertEquals("hist-1", histories[0].id)
    }

    @Test
    fun `LoadScreen_isLoadingがfalseになること`() = runTest {
        // Arrange
        val repository = FakeSyncHistoryRepository(initialHistories = sampleHistories)
        val viewModel = createViewModel(repository)

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.LoadScreen)
        advanceUntilIdle() // コルーチン完了を待つ

        // Assert
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ========================================
    // ソート機能
    // ========================================

    @Test
    fun `OpenSortMenu_isSortMenuVisibleがtrueになること`() {
        // Arrange
        val viewModel = createViewModel()

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.OpenSortMenu)

        // Assert
        assertTrue(viewModel.uiState.value.isSortMenuVisible)
    }

    @Test
    fun `CloseSortMenu_isSortMenuVisibleがfalseになること`() {
        // Arrange
        val viewModel = createViewModel()
        viewModel.handleIntent(SyncHistoryListIntent.OpenSortMenu)

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.CloseSortMenu)

        // Assert
        assertFalse(viewModel.uiState.value.isSortMenuVisible)
    }

    @Test
    fun `ChangeSortOrder_sortOrderが更新されること`() = runTest {
        // Arrange
        val repository = FakeSyncHistoryRepository(initialHistories = sampleHistories)
        val viewModel = createViewModel(repository)
        viewModel.handleIntent(SyncHistoryListIntent.LoadScreen)
        advanceUntilIdle()

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.ChangeSortOrder(HistorySortOrder.MOST_USED))
        advanceUntilIdle()

        // Assert
        assertEquals(HistorySortOrder.MOST_USED, viewModel.uiState.value.sortOrder)
    }

    @Test
    fun `ChangeSortOrder_isSortMenuVisibleがfalseになること`() = runTest {
        // Arrange
        val repository = FakeSyncHistoryRepository(initialHistories = sampleHistories)
        val viewModel = createViewModel(repository)
        viewModel.handleIntent(SyncHistoryListIntent.OpenSortMenu)

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.ChangeSortOrder(HistorySortOrder.CREATED))
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.uiState.value.isSortMenuVisible)
    }

    // ========================================
    // 削除機能
    // ========================================

    @Test
    fun `ShowDeleteDialog_deletingHistoryIdが設定されること`() {
        // Arrange
        val viewModel = createViewModel()

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.ShowDeleteDialog("hist-1"))

        // Assert
        assertEquals("hist-1", viewModel.uiState.value.deletingHistoryId)
        assertTrue(viewModel.uiState.value.isDeleteDialogVisible)
    }

    @Test
    fun `DismissDeleteDialog_deletingHistoryIdがnullになること`() {
        // Arrange
        val viewModel = createViewModel()
        viewModel.handleIntent(SyncHistoryListIntent.ShowDeleteDialog("hist-1"))

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.DismissDeleteDialog)

        // Assert
        assertNull(viewModel.uiState.value.deletingHistoryId)
        assertFalse(viewModel.uiState.value.isDeleteDialogVisible)
    }

    @Test
    fun `ConfirmDelete_削除成功時にShowDeleteSuccessSideEffectが発行されること`() = runTest {
        // Arrange
        val repository = FakeSyncHistoryRepository(initialHistories = sampleHistories)
        val viewModel = createViewModel(repository)
        viewModel.handleIntent(SyncHistoryListIntent.ShowDeleteDialog("hist-1"))

        var receivedSideEffect: SyncHistoryListSideEffect? = null
        val job = launch {
            viewModel.sideEffect.collect { receivedSideEffect = it }
        }

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.ConfirmDelete)
        advanceUntilIdle()

        // Assert
        assertEquals(SyncHistoryListSideEffect.ShowDeleteSuccess, receivedSideEffect)
        job.cancel()
    }

    @Test
    fun `ConfirmDelete_削除後にdeletingHistoryIdがnullになること`() = runTest {
        // Arrange
        val repository = FakeSyncHistoryRepository(initialHistories = sampleHistories)
        val viewModel = createViewModel(repository)
        viewModel.handleIntent(SyncHistoryListIntent.ShowDeleteDialog("hist-1"))

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.ConfirmDelete)
        advanceUntilIdle()

        // Assert
        assertNull(viewModel.uiState.value.deletingHistoryId)
    }

    @Test
    fun `ConfirmDelete_削除失敗時にShowDeleteErrorSideEffectが発行されること`() = runTest {
        // Arrange
        val repository = FakeSyncHistoryRepository(
            initialHistories = sampleHistories,
            shouldDeleteFail = true,
        )
        val viewModel = createViewModel(repository)
        viewModel.handleIntent(SyncHistoryListIntent.ShowDeleteDialog("hist-1"))

        var receivedSideEffect: SyncHistoryListSideEffect? = null
        val job = launch {
            viewModel.sideEffect.collect { receivedSideEffect = it }
        }

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.ConfirmDelete)
        advanceUntilIdle()

        // Assert
        assertEquals(SyncHistoryListSideEffect.ShowDeleteError, receivedSideEffect)
        job.cancel()
    }

    // ========================================
    // 名前変更機能
    // ========================================

    @Test
    fun `ShowRenameDialog_renamingHistoryIdとrenameInputが設定されること`() {
        // Arrange
        val viewModel = createViewModel()

        // Act
        viewModel.handleIntent(
            SyncHistoryListIntent.ShowRenameDialog(
                historyId = "hist-1",
                currentName = "Apex大会グループ",
            ),
        )

        // Assert
        assertEquals("hist-1", viewModel.uiState.value.renamingHistoryId)
        assertEquals("Apex大会グループ", viewModel.uiState.value.renameInput)
        assertTrue(viewModel.uiState.value.isRenameDialogVisible)
    }

    @Test
    fun `DismissRenameDialog_renamingHistoryIdがnullになること`() {
        // Arrange
        val viewModel = createViewModel()
        viewModel.handleIntent(
            SyncHistoryListIntent.ShowRenameDialog(
                historyId = "hist-1",
                currentName = "Apex大会グループ",
            ),
        )

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.DismissRenameDialog)

        // Assert
        assertNull(viewModel.uiState.value.renamingHistoryId)
        assertEquals("", viewModel.uiState.value.renameInput)
        assertFalse(viewModel.uiState.value.isRenameDialogVisible)
    }

    @Test
    fun `UpdateRenameInput_renameInputが更新されること`() {
        // Arrange
        val viewModel = createViewModel()

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.UpdateRenameInput("新しい名前"))

        // Assert
        assertEquals("新しい名前", viewModel.uiState.value.renameInput)
    }

    @Test
    fun `ConfirmRename_名前変更失敗時にShowRenameErrorSideEffectが発行されること`() = runTest {
        // Arrange
        val repository = FakeSyncHistoryRepository(
            initialHistories = sampleHistories,
            shouldRenameFail = true,
        )
        val viewModel = createViewModel(repository)
        viewModel.handleIntent(
            SyncHistoryListIntent.ShowRenameDialog(
                historyId = "hist-1",
                currentName = "Apex大会グループ",
            ),
        )
        viewModel.handleIntent(SyncHistoryListIntent.UpdateRenameInput("新しい名前"))

        var receivedSideEffect: SyncHistoryListSideEffect? = null
        val job = launch {
            viewModel.sideEffect.collect { receivedSideEffect = it }
        }

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.ConfirmRename)
        advanceUntilIdle()

        // Assert
        assertEquals(SyncHistoryListSideEffect.ShowRenameError, receivedSideEffect)
        job.cancel()
    }

    @Test
    fun `ConfirmRename_成功後にrenamingHistoryIdがnullになること`() = runTest {
        // Arrange
        val repository = FakeSyncHistoryRepository(initialHistories = sampleHistories)
        val viewModel = createViewModel(repository)
        viewModel.handleIntent(
            SyncHistoryListIntent.ShowRenameDialog(
                historyId = "hist-1",
                currentName = "Apex大会グループ",
            ),
        )
        viewModel.handleIntent(SyncHistoryListIntent.UpdateRenameInput("新しい名前"))

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.ConfirmRename)
        advanceUntilIdle()

        // Assert
        assertNull(viewModel.uiState.value.renamingHistoryId)
    }

    // ========================================
    // 空状態の判定
    // ========================================

    @Test
    fun `isEmpty_初期状態では空状態であること`() {
        // Arrange: 初期状態はisLoadingがfalse、historiesが空
        val viewModel = createViewModel()

        // Assert
        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `isEmpty_LoadScreen呼び出し後ローディング中は空状態でないこと`() = runTest {
        // Arrange
        val viewModel = createViewModel()

        // Act: LoadScreen呼び出し直後はisLoadingがtrue
        viewModel.handleIntent(SyncHistoryListIntent.LoadScreen)
        // advanceUntilIdle を呼ばずにすぐ確認

        // Assert: ローディング中は空状態と見なさない
        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `isEmpty_データ取得後に履歴が0件の場合trueであること`() = runTest {
        // Arrange
        val repository = FakeSyncHistoryRepository(initialHistories = emptyList())
        val viewModel = createViewModel(repository)

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.LoadScreen)
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `isEmpty_データ取得後に履歴が1件以上ある場合falseであること`() = runTest {
        // Arrange
        val repository = FakeSyncHistoryRepository(initialHistories = sampleHistories)
        val viewModel = createViewModel(repository)

        // Act
        viewModel.handleIntent(SyncHistoryListIntent.LoadScreen)
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.uiState.value.isEmpty)
    }

    // ========================================
    // ヘルパー
    // ========================================

    private fun createViewModel(
        repository: SyncHistoryRepository = FakeSyncHistoryRepository(),
    ): SyncHistoryListViewModel = SyncHistoryListViewModel(
        syncHistoryRepository = repository,
    )
}

/**
 * テスト用のFake SyncHistoryRepository。
 */
@OptIn(ExperimentalTime::class)
private class FakeSyncHistoryRepository(
    initialHistories: List<SyncHistory> = emptyList(),
    private val shouldDeleteFail: Boolean = false,
    private val shouldRenameFail: Boolean = false,
) : SyncHistoryRepository {

    private val historiesFlow = MutableStateFlow(initialHistories)

    override fun observeHistories(sortBy: HistorySortOrder): Flow<List<SyncHistory>> = historiesFlow

    override suspend fun saveHistory(
        channels: List<SyncChannel>,
        name: String?,
    ): Result<SyncHistory> = Result.failure(NotImplementedError())

    override suspend fun getAllHistories(
        sortBy: HistorySortOrder,
    ): Result<List<SyncHistory>> = Result.success(historiesFlow.value)

    override suspend fun getHistoryById(historyId: String): Result<SyncHistory?> =
        Result.success(historiesFlow.value.find { it.id == historyId })

    override suspend fun deleteHistory(historyId: String): Result<Unit> {
        if (shouldDeleteFail) return Result.failure(RuntimeException("削除失敗"))
        historiesFlow.value = historiesFlow.value.filter { it.id != historyId }
        return Result.success(Unit)
    }

    override suspend fun recordUsage(historyId: String): Result<Unit> = Result.success(Unit)

    override suspend fun updateHistoryName(
        historyId: String,
        newName: String?,
    ): Result<Unit> {
        if (shouldRenameFail) return Result.failure(RuntimeException("名前変更失敗"))
        historiesFlow.value = historiesFlow.value.map { history ->
            if (history.id == historyId) history.copy(name = newName) else history
        }
        return Result.success(Unit)
    }
}
