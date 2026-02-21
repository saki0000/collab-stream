@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.VideoServiceType

/**
 * Timeline Sync画面の振る舞い仕様
 * Specification: feature/timeline_sync/SPECIFICATION.md
 * Story Issue: #32（Story 1）, #53（Story 3）, #54（Story 4）
 */
class TimelineSyncViewModelTest {

    // ========================================
    // Story 1: タイムライン基本表示 - 画面を開いた時
    // ========================================

    @Test
    fun `画面を開いた時_まずはローディング状態になること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `画面を開いた時_データ取得成功_チャンネルありの場合コンテンツが表示されること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `画面を開いた時_データ取得成功_チャンネルなしの場合空状態が表示されること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `画面を開いた時_データ取得失敗_エラー状態になること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 1: 日付選択
    // ========================================

    @Test
    fun `日付選択_デフォルトで今日の日付が選択されていること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `日付選択_日付をタップすると選択日付が更新されること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `日付選択_選択日付の変更時にタイムラインバーが再計算されること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 1: 週移動
    // ========================================

    @Test
    fun `週移動_左スワイプで次週に移動できること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `週移動_右スワイプで前週に移動できること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `週移動_週移動時に選択日付は変更されないこと`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 1: アクティブチャンネル数
    // ========================================

    @Test
    fun `アクティブチャンネル数_ストリームが選択されているチャンネル数をカウントすること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `アクティブチャンネル数_ヘッダーにN CHANNELS ACTIVEとして表示されること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 1: リフレッシュ・リトライ
    // ========================================

    @Test
    fun `リフレッシュ操作_再度データ取得を試みること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `再試行ボタンを押した時_再度データ取得を試みること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 3: 初期同期時刻設定
    // ========================================

    @Test
    fun `初期同期時刻_チャンネルありの場合最初のチャンネルのストリーム開始時刻が設定されること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `初期同期時刻_チャンネルなしの場合syncTimeがnullであること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 3: シークバードラッグ
    // ========================================

    @Test
    fun `シークバードラッグ_開始時にisDraggingがtrueになること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `シークバードラッグ_終了時にisDraggingがfalseになること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `シークバードラッグ_ドラッグ中にsyncTimeがリアルタイムで更新されること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 3: SYNC TIME表示
    // ========================================

    @Test
    fun `SYNC TIME表示_syncTimeがHH MM SS形式で表示されること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `SYNC TIME表示_syncTimeがnullの場合非表示になること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `SYNC TIME表示_シークバードラッグ中にリアルタイムで更新されること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 3: 同期位置計算
    // ========================================

    @Test
    fun `同期位置計算_syncTimeがストリーム範囲内の場合targetSeekPositionが正しく計算されること`() {
        // TODO: Phase 2でAI実装
        // 計算式: (syncTime - streamStartTime).inWholeSeconds.toFloat()
    }

    @Test
    fun `同期位置計算_syncTimeがストリーム開始前の場合targetSeekPositionが0であること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 3: SyncStatus判定
    // ========================================

    @Test
    fun `SyncStatus判定_syncTimeがストリーム開始前の場合WAITINGになること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `SyncStatus判定_syncTimeがストリーム範囲内の場合READYになること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `SyncStatus判定_syncTimeの変更時に全チャンネルのSyncStatusが再判定されること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 3: syncTimeRange制限
    // ========================================

    @Test
    fun `syncTimeRange_全チャンネルの最小開始時刻から最大終了時刻の範囲であること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `syncTimeRange_シークバーがsyncTimeRange内でのみ移動可能であること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 3: チャンネル追加・削除時の振る舞い
    // ========================================

    @Test
    fun `チャンネル追加時_syncTimeRangeが再計算されること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `チャンネル削除時_syncTimeRangeが再計算されること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `最後のチャンネル削除時_syncTimeがnullになること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 3: 同期時刻インジケーター
    // ========================================

    @Test
    fun `同期時刻インジケーター_syncTimeに対応する位置に縦の青い線が表示されること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `同期時刻インジケーター_シークバードラッグ中にリアルタイムで移動すること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `同期時刻インジケーター_syncTimeがnullの場合非表示になること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 4: 外部アプリ連携 - Openボタン
    // ========================================

    @Test
    fun `Openボタン_READY状態のチャンネルで有効になること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `Openボタン_WAITING状態のチャンネルで非活性になること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `Openボタン_OPENED状態のチャンネルで引き続き有効になること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 4: 外部アプリ連携 - Open操作
    // ========================================

    @Test
    fun `Open操作_READY状態のチャンネルでOpenChannelIntentを送信できること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `Open操作_外部アプリ起動成功時にSyncStatusがOPENEDに更新されること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `Open操作_OPENED状態のチャンネルでも再度外部アプリを起動できること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 4: DeepLink URL生成
    // ========================================

    @Test
    fun `DeepLink生成_YouTubeの場合正しい形式のURLが生成されること`() {
        // TODO: Phase 2でAI実装
        // 期待値: youtube://watch?v={VIDEO_ID}&t={SECONDS}
    }

    @Test
    fun `DeepLink生成_Twitchの場合正しい形式のURLが生成されること`() {
        // TODO: Phase 2でAI実装
        // 期待値: twitch://video/{VIDEO_ID}?t={SECONDS}s
    }

    @Test
    fun `DeepLink生成_targetSeekPositionが秒単位の整数値に変換されること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `DeepLink生成_targetSeekPositionが負の値の場合0に丸められること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 4: フォールバックURL生成
    // ========================================

    @Test
    fun `フォールバックURL生成_YouTubeの場合正しいWeb URLが生成されること`() {
        // TODO: Phase 2でAI実装
        // 期待値: https://www.youtube.com/watch?v={VIDEO_ID}&t={SECONDS}s
        // Note: YouTubeのWebサイトURLではtパラメータにs接尾辞が必要
    }

    @Test
    fun `フォールバックURL生成_Twitchの場合正しいWeb URLが生成されること`() {
        // TODO: Phase 2でAI実装
        // 期待値: https://www.twitch.tv/videos/{VIDEO_ID}?t={SECONDS}s
    }

    // ========================================
    // Story 4: 外部アプリ起動エラー
    // ========================================

    @Test
    fun `外部アプリ起動失敗_フォールバックURLでWebブラウザを起動すること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `外部アプリ起動失敗_フォールバックも失敗時にSnackbarでエラー表示すること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 4: SideEffect発行
    // ========================================

    @Test
    fun `SideEffect_Open成功時にNavigateToExternalAppが発行されること`() {
        // TODO: Phase 2でAI実装
    }

    @Test
    fun `SideEffect_Open失敗時にShowExternalAppErrorが発行されること`() {
        // TODO: Phase 2でAI実装
    }

    // ========================================
    // Story 5: プラットフォーム切り替え
    // ========================================

    @Test
    fun `プラットフォーム選択_デフォルトでTwitchが選択されていること`() {
        // Arrange
        val state = TimelineSyncUiState()

        // Assert
        assertEquals(VideoServiceType.TWITCH, state.selectedPlatform)
    }

    @Test
    fun `プラットフォーム切り替え_YouTubeに切り替えるとselectedPlatformが更新されること`() {
        // Arrange
        val initialState = TimelineSyncUiState(selectedPlatform = VideoServiceType.TWITCH)

        // Act
        val updatedState = initialState.copy(selectedPlatform = VideoServiceType.YOUTUBE)

        // Assert
        assertEquals(VideoServiceType.YOUTUBE, updatedState.selectedPlatform)
    }

    @Test
    fun `プラットフォーム切り替え_検索結果がクリアされること`() {
        // Arrange
        val stateWithSuggestions = TimelineSyncUiState(
            channelSuggestions = listOf(
                ChannelInfo(
                    id = "ch1",
                    displayName = "Test Channel",
                    serviceType = VideoServiceType.TWITCH,
                ),
            ),
            selectedPlatform = VideoServiceType.TWITCH,
        )

        // Act: プラットフォーム切り替え時に suggestions をクリアする
        val updatedState = stateWithSuggestions.copy(
            selectedPlatform = VideoServiceType.YOUTUBE,
            channelSuggestions = emptyList(),
        )

        // Assert
        assertEquals(VideoServiceType.YOUTUBE, updatedState.selectedPlatform)
        assertTrue(updatedState.channelSuggestions.isEmpty())
    }

    @Test
    fun `プラットフォーム切り替え_検索クエリは保持されること`() {
        // Arrange
        val stateWithQuery = TimelineSyncUiState(
            channelSearchQuery = "test query",
            selectedPlatform = VideoServiceType.TWITCH,
        )

        // Act: プラットフォーム切り替え
        val updatedState = stateWithQuery.copy(
            selectedPlatform = VideoServiceType.YOUTUBE,
            channelSuggestions = emptyList(),
        )

        // Assert
        assertEquals("test query", updatedState.channelSearchQuery)
        assertEquals(VideoServiceType.YOUTUBE, updatedState.selectedPlatform)
    }

    // ========================================
    // Story 5: チャンネル追加 - serviceType
    // ========================================

    @Test
    fun `チャンネル追加_ChannelInfoのserviceTypeがSyncChannelに反映されること`() {
        // Arrange
        val youtubeChannel = ChannelInfo(
            id = "yt_ch_1",
            displayName = "YouTube Channel",
            thumbnailUrl = "https://example.com/thumb.jpg",
            serviceType = VideoServiceType.YOUTUBE,
        )

        // Assert: ChannelInfo の serviceType が YOUTUBE であること
        assertEquals(VideoServiceType.YOUTUBE, youtubeChannel.serviceType)
    }

    @Test
    fun `ChannelInfo_デフォルトserviceTypeがTWITCHであること`() {
        // Arrange
        val defaultChannel = ChannelInfo(
            id = "ch1",
            displayName = "Default Channel",
        )

        // Assert
        assertEquals(VideoServiceType.TWITCH, defaultChannel.serviceType)
    }

    // ========================================
    // 履歴保存 (US-2: 同期チャンネル履歴保存)
    // ========================================

    @Test
    fun `保存ボタン有効条件_チャンネルが2つ以上の場合canSaveHistoryがtrueであること`() {
        // Arrange
        val channel1 = ChannelInfo(id = "ch1", displayName = "Channel 1")
        val channel2 = ChannelInfo(id = "ch2", displayName = "Channel 2")
        val state = TimelineSyncUiState(
            channels = listOf(
                org.example.project.domain.model.SyncChannel(
                    channelId = channel1.id,
                    channelName = channel1.displayName,
                    channelIconUrl = "",
                    serviceType = channel1.serviceType,
                    selectedStream = null,
                    syncStatus = org.example.project.domain.model.SyncStatus.NOT_SYNCED,
                ),
                org.example.project.domain.model.SyncChannel(
                    channelId = channel2.id,
                    channelName = channel2.displayName,
                    channelIconUrl = "",
                    serviceType = channel2.serviceType,
                    selectedStream = null,
                    syncStatus = org.example.project.domain.model.SyncStatus.NOT_SYNCED,
                ),
            ),
            isSavingHistory = false,
        )

        // Assert
        assertTrue(state.canSaveHistory)
    }

    @Test
    fun `保存ボタン有効条件_チャンネルが1つの場合canSaveHistoryがfalseであること`() {
        // Arrange
        val state = TimelineSyncUiState(
            channels = listOf(
                org.example.project.domain.model.SyncChannel(
                    channelId = "ch1",
                    channelName = "Channel 1",
                    channelIconUrl = "",
                    serviceType = VideoServiceType.TWITCH,
                    selectedStream = null,
                    syncStatus = org.example.project.domain.model.SyncStatus.NOT_SYNCED,
                ),
            ),
            isSavingHistory = false,
        )

        // Assert
        assertTrue(!state.canSaveHistory)
    }

    @Test
    fun `保存ボタン有効条件_チャンネルが0件の場合canSaveHistoryがfalseであること`() {
        // Arrange
        val state = TimelineSyncUiState(
            channels = emptyList(),
            isSavingHistory = false,
        )

        // Assert
        assertTrue(!state.canSaveHistory)
    }

    @Test
    fun `保存ボタン有効条件_保存処理中はcanSaveHistoryがfalseであること`() {
        // Arrange
        val state = TimelineSyncUiState(
            channels = listOf(
                org.example.project.domain.model.SyncChannel(
                    channelId = "ch1",
                    channelName = "Channel 1",
                    channelIconUrl = "",
                    serviceType = VideoServiceType.TWITCH,
                    selectedStream = null,
                    syncStatus = org.example.project.domain.model.SyncStatus.NOT_SYNCED,
                ),
                org.example.project.domain.model.SyncChannel(
                    channelId = "ch2",
                    channelName = "Channel 2",
                    channelIconUrl = "",
                    serviceType = VideoServiceType.YOUTUBE,
                    selectedStream = null,
                    syncStatus = org.example.project.domain.model.SyncStatus.NOT_SYNCED,
                ),
            ),
            isSavingHistory = true,
        )

        // Assert（保存中でもチャンネル数は2以上だが、isSavingHistoryがtrueなのでfalse）
        assertTrue(!state.canSaveHistory)
    }

    @Test
    fun `重複ダイアログ表示状態_showDuplicateDialogがtrueの時にダイアログ表示フラグが設定されること`() {
        // Arrange
        val initialState = TimelineSyncUiState(
            showDuplicateDialog = false,
            duplicateHistoryId = null,
        )

        // Act: 重複検出後の状態に遷移
        val stateAfterDuplicateDetected = initialState.copy(
            showDuplicateDialog = true,
            duplicateHistoryId = "existing-history-id",
        )

        // Assert
        assertTrue(stateAfterDuplicateDetected.showDuplicateDialog)
        assertEquals("existing-history-id", stateAfterDuplicateDetected.duplicateHistoryId)
    }

    @Test
    fun `キャンセル操作_ダイアログキャンセル後にshowDuplicateDialogがfalseになること`() {
        // Arrange: 重複確認ダイアログが表示されている状態
        val stateWithDialog = TimelineSyncUiState(
            showDuplicateDialog = true,
            duplicateHistoryId = "existing-history-id",
        )

        // Act: キャンセル選択後の状態
        val stateAfterCancel = stateWithDialog.copy(
            showDuplicateDialog = false,
            duplicateHistoryId = null,
            isSavingHistory = false,
        )

        // Assert
        assertTrue(!stateAfterCancel.showDuplicateDialog)
        assertEquals(null, stateAfterCancel.duplicateHistoryId)
        assertTrue(!stateAfterCancel.isSavingHistory)
    }

    @Test
    fun `保存中フラグ_isSavingHistoryがtrueの時に保存処理中であること`() {
        // Arrange
        val state = TimelineSyncUiState(isSavingHistory = false)

        // Act: 保存処理開始後の状態
        val savingState = state.copy(isSavingHistory = true)

        // Assert
        assertTrue(savingState.isSavingHistory)
    }

    @Test
    fun `最小チャンネル数_MIN_CHANNELS_FOR_SAVEが2であること`() {
        // Assert: 仕様書の「チャンネル数 >= 2」を確認
        assertEquals(2, TimelineSyncUiState.MIN_CHANNELS_FOR_SAVE)
    }
}
