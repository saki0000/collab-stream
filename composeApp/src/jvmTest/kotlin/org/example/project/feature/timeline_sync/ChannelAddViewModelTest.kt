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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.usecase.ChannelSearchUseCase
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested

/**
 * ViewModelテスト: TimelineSyncViewModel - チャンネル追加・管理
 *
 * Story 2: チャンネル追加・管理の振る舞いを定義
 *
 * Specification: feature/timeline_sync/channel_add/REQUIREMENTS.md
 * Story Issue: #46
 * Epic: Timeline Sync (EPIC-002)
 */
@DisplayName("TimelineSyncViewModel - チャンネル追加・管理")
class ChannelAddViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============================================
    // モーダル表示制御
    // ============================================

    @Nested
    @DisplayName("モーダル表示制御")
    inner class ModalVisibility {

        @Test
        @DisplayName("初期状態ではモーダルが非表示であること")
        fun `initial state should have modal hidden`() {
            // TODO: Phase 2で実装
            // assertFalse(viewModel.uiState.value.isChannelAddModalVisible)
        }

        @Test
        @DisplayName("OpenChannelAddModal IntentでモーダルがTrueになること")
        fun `OpenChannelAddModal should show modal`() = runTest {
            // TODO: Phase 2で実装
            // viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
            // advanceUntilIdle()
            // assertTrue(viewModel.uiState.value.isChannelAddModalVisible)
        }

        @Test
        @DisplayName("CloseChannelAddModal IntentでモーダルがFalseになること")
        fun `CloseChannelAddModal should hide modal`() = runTest {
            // TODO: Phase 2で実装
            // viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
            // viewModel.handleIntent(TimelineSyncIntent.CloseChannelAddModal)
            // advanceUntilIdle()
            // assertFalse(viewModel.uiState.value.isChannelAddModalVisible)
        }

        @Test
        @DisplayName("モーダルを閉じると検索状態がリセットされること")
        fun `closing modal should reset search state`() = runTest {
            // TODO: Phase 2で実装
            // viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
            // viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("test"))
            // advanceUntilIdle()
            //
            // viewModel.handleIntent(TimelineSyncIntent.CloseChannelAddModal)
            // advanceUntilIdle()
            //
            // assertEquals("", viewModel.uiState.value.channelSearchQuery)
            // assertTrue(viewModel.uiState.value.channelSuggestions.isEmpty())
        }
    }

    // ============================================
    // チャンネル検索
    // ============================================

    @Nested
    @DisplayName("チャンネル検索")
    inner class ChannelSearch {

        @Test
        @DisplayName("検索クエリを更新すると500msデバウンス後に検索が実行されること")
        fun `should search channels after 500ms debounce`() = runTest {
            // TODO: Phase 2で実装
            // viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
            // viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("ninja"))
            //
            // // デバウンス待機
            // advanceTimeBy(600)
            // advanceUntilIdle()
            //
            // assertTrue(mockChannelSearchUseCase.searchWasCalled)
        }

        @Test
        @DisplayName("デバウンス内の連続入力では最後のクエリのみ検索されること")
        fun `should debounce search queries`() = runTest {
            // TODO: Phase 2で実装
            // viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
            // viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("n"))
            // advanceTimeBy(100)
            // viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("ni"))
            // advanceTimeBy(100)
            // viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("nin"))
            // advanceTimeBy(600)
            // advanceUntilIdle()
            //
            // assertEquals(1, mockChannelSearchUseCase.searchCount)
            // assertEquals("nin", mockChannelSearchUseCase.lastQuery)
        }

        @Test
        @DisplayName("空クエリでは検索が実行されないこと")
        fun `should not search with empty query`() = runTest {
            // TODO: Phase 2で実装
            // viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
            // viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery(""))
            // advanceTimeBy(600)
            // advanceUntilIdle()
            //
            // assertFalse(mockChannelSearchUseCase.searchWasCalled)
            // assertTrue(viewModel.uiState.value.channelSuggestions.isEmpty())
        }

        @Test
        @DisplayName("検索成功時にチャンネル候補が更新されること")
        fun `should update suggestions on successful search`() = runTest {
            // TODO: Phase 2で実装
            // mockChannelSearchUseCase.resultToReturn = listOf(
            //     ChannelInfo(id = "ch1", displayName = "Channel 1")
            // )
            //
            // viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
            // viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("test"))
            // advanceTimeBy(600)
            // advanceUntilIdle()
            //
            // assertEquals(1, viewModel.uiState.value.channelSuggestions.size)
        }

        @Test
        @DisplayName("検索中はisSearchingChannelsがTrueになること")
        fun `should set isSearchingChannels while searching`() = runTest {
            // TODO: Phase 2で実装
            // viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
            // viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("test"))
            // advanceTimeBy(500) // デバウンス完了直後
            //
            // assertTrue(viewModel.uiState.value.isSearchingChannels)
        }

        @Test
        @DisplayName("検索結果に追加済みのチャンネルが含まれないこと")
        fun `should exclude already added channels from search results`() = runTest {
            // TODO: Phase 2で実装
            // // 1. チャンネルを1つ追加する
            // val existingChannel = ChannelInfo(id = "ch1", displayName = "Channel 1")
            // viewModel.handleIntent(TimelineSyncIntent.AddChannel(existingChannel))
            // advanceUntilIdle()
            //
            // // 2. 検索結果に同じチャンネルが含まれるようにモックを設定
            // mockChannelSearchUseCase.resultToReturn = listOf(
            //     existingChannel,
            //     ChannelInfo(id = "ch2", displayName = "Channel 2")
            // )
            //
            // // 3. 検索を実行
            // viewModel.handleIntent(TimelineSyncIntent.OpenChannelAddModal)
            // viewModel.handleIntent(TimelineSyncIntent.UpdateChannelSearchQuery("Channel"))
            // advanceTimeBy(600)
            // advanceUntilIdle()
            //
            // // 4. 検索候補に既存チャンネルが含まれていないことを確認
            // val suggestions = viewModel.uiState.value.channelSuggestions
            // assertEquals(1, suggestions.size)
            // assertEquals("ch2", suggestions[0].id)
        }
    }

    // ============================================
    // チャンネル追加
    // ============================================

    @Nested
    @DisplayName("チャンネル追加")
    inner class AddChannel {

        @Test
        @DisplayName("チャンネルを追加するとリストに追加されること")
        fun `should add channel to list`() = runTest {
            // TODO: Phase 2で実装
            // val channel = ChannelInfo(id = "ch1", displayName = "Channel 1")
            //
            // viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
            // advanceUntilIdle()
            //
            // assertEquals(1, viewModel.uiState.value.channels.size)
            // assertEquals("ch1", viewModel.uiState.value.channels[0].channelId)
        }

        @Test
        @DisplayName("追加されたチャンネルはSyncChannel形式であること")
        fun `added channel should be SyncChannel format`() = runTest {
            // TODO: Phase 2で実装
            // val channel = ChannelInfo(
            //     id = "ch1",
            //     displayName = "Channel 1",
            //     thumbnailUrl = "http://example.com/icon.jpg"
            // )
            //
            // viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
            // advanceUntilIdle()
            //
            // val addedChannel = viewModel.uiState.value.channels[0]
            // assertEquals("ch1", addedChannel.channelId)
            // assertEquals("Channel 1", addedChannel.channelName)
            // assertEquals("http://example.com/icon.jpg", addedChannel.channelIconUrl)
            // assertEquals(VideoServiceType.TWITCH, addedChannel.serviceType)
            // assertNull(addedChannel.selectedStream)
            // assertEquals(SyncStatus.NOT_SYNCED, addedChannel.syncStatus)
        }

        @Test
        @DisplayName("重複チャンネルは追加されないこと")
        fun `should not add duplicate channel`() = runTest {
            // TODO: Phase 2で実装
            // val channel = ChannelInfo(id = "ch1", displayName = "Channel 1")
            //
            // viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
            // viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
            // advanceUntilIdle()
            //
            // assertEquals(1, viewModel.uiState.value.channels.size)
        }

        @Test
        @DisplayName("重複追加時にエラーメッセージが設定されること")
        fun `should set error message on duplicate add`() = runTest {
            // TODO: Phase 2で実装
            // val channel = ChannelInfo(id = "ch1", displayName = "Channel 1")
            //
            // viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
            // viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
            // advanceUntilIdle()
            //
            // assertNotNull(viewModel.uiState.value.channelAddError)
        }

        @Test
        @DisplayName("最大10チャンネルまで追加可能であること")
        fun `should allow up to 10 channels`() = runTest {
            // TODO: Phase 2で実装
            // repeat(10) { i ->
            //     viewModel.handleIntent(TimelineSyncIntent.AddChannel(
            //         ChannelInfo(id = "ch$i", displayName = "Channel $i")
            //     ))
            // }
            // advanceUntilIdle()
            //
            // assertEquals(10, viewModel.uiState.value.channels.size)
        }

        @Test
        @DisplayName("11チャンネル目の追加はエラーになること")
        fun `should reject 11th channel`() = runTest {
            // TODO: Phase 2で実装
            // repeat(11) { i ->
            //     viewModel.handleIntent(TimelineSyncIntent.AddChannel(
            //         ChannelInfo(id = "ch$i", displayName = "Channel $i")
            //     ))
            // }
            // advanceUntilIdle()
            //
            // assertEquals(10, viewModel.uiState.value.channels.size)
            // assertNotNull(viewModel.uiState.value.channelAddError)
        }
    }

    // ============================================
    // チャンネル削除
    // ============================================

    @Nested
    @DisplayName("チャンネル削除")
    inner class RemoveChannel {

        @Test
        @DisplayName("チャンネルを削除するとリストから除去されること")
        fun `should remove channel from list`() = runTest {
            // TODO: Phase 2で実装
            // val channel = ChannelInfo(id = "ch1", displayName = "Channel 1")
            // viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
            // advanceUntilIdle()
            //
            // viewModel.handleIntent(TimelineSyncIntent.RemoveChannel("ch1"))
            // advanceUntilIdle()
            //
            // assertTrue(viewModel.uiState.value.channels.isEmpty())
        }

        @Test
        @DisplayName("存在しないチャンネルIDで削除しても例外が発生しないこと")
        fun `should not throw on removing non-existent channel`() = runTest {
            // TODO: Phase 2で実装
            // viewModel.handleIntent(TimelineSyncIntent.RemoveChannel("non-existent"))
            // advanceUntilIdle()
            //
            // // No exception should be thrown
            // assertTrue(viewModel.uiState.value.channels.isEmpty())
        }

        @Test
        @DisplayName("削除後にisEmptyがTrueになること")
        fun `isEmpty should be true after removing last channel`() = runTest {
            // TODO: Phase 2で実装
            // val channel = ChannelInfo(id = "ch1", displayName = "Channel 1")
            // viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
            // advanceUntilIdle()
            //
            // viewModel.handleIntent(TimelineSyncIntent.RemoveChannel("ch1"))
            // advanceUntilIdle()
            //
            // assertTrue(viewModel.uiState.value.isEmpty)
        }

        @Test
        @DisplayName("削除後に「元に戻す」スナックバーが表示されること")
        fun `should show undo snackbar after deletion`() = runTest {
            // TODO: Phase 2で実装
            // val channel = ChannelInfo(id = "ch1", displayName = "Channel 1")
            // viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
            // advanceUntilIdle()
            //
            // viewModel.handleIntent(TimelineSyncIntent.RemoveChannel("ch1"))
            // advanceUntilIdle()
            //
            // assertNotNull(viewModel.uiState.value.recentlyDeletedChannel)
        }

        @Test
        @DisplayName("「元に戻す」をタップすると削除が取り消されること")
        fun `should restore channel when undo is tapped`() = runTest {
            // TODO: Phase 2で実装
            // val channel = ChannelInfo(id = "ch1", displayName = "Channel 1")
            // viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
            // advanceUntilIdle()
            //
            // viewModel.handleIntent(TimelineSyncIntent.RemoveChannel("ch1"))
            // advanceUntilIdle()
            //
            // // 削除を取り消す
            // viewModel.handleIntent(TimelineSyncIntent.UndoRemoveChannel)
            // advanceUntilIdle()
            //
            // assertEquals(1, viewModel.uiState.value.channels.size)
            // assertEquals("ch1", viewModel.uiState.value.channels[0].channelId)
            // assertNull(viewModel.uiState.value.recentlyDeletedChannel)
        }

        @Test
        @DisplayName("3秒経過後に削除が確定すること")
        fun `should confirm deletion after 3 seconds`() = runTest {
            // TODO: Phase 2で実装
            // val channel = ChannelInfo(id = "ch1", displayName = "Channel 1")
            // viewModel.handleIntent(TimelineSyncIntent.AddChannel(channel))
            // advanceUntilIdle()
            //
            // viewModel.handleIntent(TimelineSyncIntent.RemoveChannel("ch1"))
            // advanceUntilIdle()
            //
            // // 3秒経過
            // advanceTimeBy(3100)
            // advanceUntilIdle()
            //
            // assertNull(viewModel.uiState.value.recentlyDeletedChannel)
            // assertTrue(viewModel.uiState.value.channels.isEmpty())
        }
    }

    // ============================================
    // AddChannelボタン
    // ============================================

    @Nested
    @DisplayName("AddChannelボタン")
    inner class AddChannelButton {

        @Test
        @DisplayName("チャンネル数が10未満の場合はcanAddChannelがTrueであること")
        fun `canAddChannel should be true when less than 10 channels`() {
            // TODO: Phase 2で実装
            // val state = TimelineSyncUiState(channels = emptyList())
            // assertTrue(state.canAddChannel)
        }

        @Test
        @DisplayName("チャンネル数が10の場合はcanAddChannelがFalseであること")
        fun `canAddChannel should be false when 10 channels exist`() {
            // TODO: Phase 2で実装
            // val channels = (1..10).map { i ->
            //     SyncChannel(
            //         channelId = "ch$i",
            //         channelName = "Channel $i",
            //         channelIconUrl = "",
            //         serviceType = VideoServiceType.TWITCH,
            //     )
            // }
            // val state = TimelineSyncUiState(channels = channels)
            //
            // assertFalse(state.canAddChannel)
        }
    }
}
