@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.SelectedStreamInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.timeline_sync.TimelineSyncIntent
import org.example.project.feature.timeline_sync.TimelineSyncUiState
import org.example.project.feature.timeline_sync.channel_add.ChannelAddBottomSheet
import org.example.project.feature.timeline_sync.ui.components.TimelineSyncHeader
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Screen Composable (Stateless) - Main Timeline Sync Screen.
 *
 * Layout: Header + WeekCalendar + ChannelAvatars + SyncTime + TimelineCards + BottomNav
 * Receives UiState and Intent callbacks from Container, delegates to Content composables.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display), US-2 (Channel Add/Remove)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineSyncScreen(
    uiState: TimelineSyncUiState,
    onIntent: (TimelineSyncIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TimelineSyncHeader(
                activeChannelCount = uiState.activeChannelCount,
                canSaveHistory = uiState.canSaveHistory,
                isSavingHistory = uiState.isSavingHistory,
                onSaveHistoryClick = { onIntent(TimelineSyncIntent.SaveHistory) },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                uiState.errorMessage != null -> {
                    ErrorContent(
                        errorMessage = uiState.errorMessage,
                        onRetry = { onIntent(TimelineSyncIntent.Retry) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                uiState.isEmpty -> {
                    EmptyContent(
                        onAddChannel = { onIntent(TimelineSyncIntent.OpenChannelAddModal) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    TimelineContent(
                        uiState = uiState,
                        onIntent = onIntent,
                        currentTime = Clock.System.now(),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Story 2: Channel Add Bottom Sheet, Story 5: Multi-Platform Search
            // Channel Follow (US-2): フォローボタンとフォロー状態の受け渡し
            ChannelAddBottomSheet(
                isVisible = uiState.isChannelAddModalVisible,
                searchQuery = uiState.channelSearchQuery,
                channelSuggestions = uiState.channelSuggestions,
                addedChannels = uiState.channels,
                isSearching = uiState.isSearchingChannels,
                errorMessage = uiState.channelAddError,
                selectedPlatform = uiState.selectedPlatform,
                followedChannelIds = uiState.followedChannelIds,
                onPlatformSelect = { platform ->
                    onIntent(TimelineSyncIntent.SelectPlatform(platform))
                },
                onSearchQueryChange = { query ->
                    onIntent(TimelineSyncIntent.UpdateChannelSearchQuery(query))
                },
                onChannelSelect = { channel ->
                    onIntent(TimelineSyncIntent.AddChannel(channel))
                },
                onChannelRemove = { channelId ->
                    onIntent(TimelineSyncIntent.RemoveChannel(channelId))
                },
                onToggleFollow = { channel ->
                    onIntent(TimelineSyncIntent.ToggleFollow(channel))
                },
                onDismiss = {
                    onIntent(TimelineSyncIntent.CloseChannelAddModal)
                },
            )
        }
    }

    // 重複確認ダイアログ
    if (uiState.showDuplicateDialog) {
        DuplicateHistoryDialog(
            onConfirm = { onIntent(TimelineSyncIntent.ConfirmOverwriteHistory) },
            onDismiss = { onIntent(TimelineSyncIntent.CancelOverwriteHistory) },
        )
    }
}

/**
 * 重複チャンネル組み合わせが既に保存されている場合に表示する確認ダイアログ。
 *
 * 「上書き」または「キャンセル」を選択できる。
 * Epic: 同期チャンネル履歴保存 (US-2: 履歴保存機能)
 */
@Composable
private fun DuplicateHistoryDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(text = "既に保存済みです")
        },
        text = {
            Text(text = "この組み合わせは既に保存されています。上書きしますか？")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "上書き")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "キャンセル")
            }
        },
    )
}

/**
 * Loading content indicator.
 */
@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = "読み込み中...",
            modifier = Modifier.padding(top = Spacing.lg),
        )
    }
}

// ============================================
// Previews
// ============================================

/**
 * Preview with content state (channels loaded).
 */
@Preview
@Composable
private fun TimelineSyncScreenPreview() {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val timeZone = TimeZone.currentSystemDefault()

    val mockChannels = listOf(
        SyncChannel(
            channelId = "yt_channel_1",
            channelName = "Gaming Channel",
            channelIconUrl = "",
            serviceType = VideoServiceType.YOUTUBE,
            selectedStream = SelectedStreamInfo(
                id = "yt_video_001",
                title = "Morning Gaming Stream",
                thumbnailUrl = "",
                startTime = today.atStartOfDayIn(timeZone) + kotlin.time.Duration.parseIsoString("PT10H"),
                endTime = today.atStartOfDayIn(timeZone) + kotlin.time.Duration.parseIsoString("PT13H"),
                duration = kotlin.time.Duration.parseIsoString("PT3H"),
            ),
            syncStatus = SyncStatus.READY,
        ),
        SyncChannel(
            channelId = "tw_channel_1",
            channelName = "Esports Pro",
            channelIconUrl = "",
            serviceType = VideoServiceType.TWITCH,
            selectedStream = SelectedStreamInfo(
                id = "tw_video_001",
                title = "Tournament Finals",
                thumbnailUrl = "",
                startTime = today.atStartOfDayIn(timeZone) + kotlin.time.Duration.parseIsoString("PT14H"),
                endTime = null,
                duration = null,
            ),
            syncStatus = SyncStatus.READY,
        ),
    )

    AppTheme {
        Surface {
            TimelineSyncScreen(
                uiState = TimelineSyncUiState(
                    isLoading = false,
                    channels = mockChannels,
                    syncTime = today.atStartOfDayIn(timeZone) + kotlin.time.Duration.parseIsoString("PT11H30M"),
                ),
                onIntent = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
        }
    }
}

/**
 * ローディング状態のプレビュー。
 */
@Preview
@Composable
private fun TimelineSyncScreenLoadingPreview() {
    AppTheme {
        Surface {
            TimelineSyncScreen(
                uiState = TimelineSyncUiState(
                    isLoading = true,
                ),
                onIntent = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
        }
    }
}

/**
 * 空状態のプレビュー。
 */
@Preview
@Composable
private fun TimelineSyncScreenEmptyPreview() {
    AppTheme {
        Surface {
            TimelineSyncScreen(
                uiState = TimelineSyncUiState(
                    isLoading = false,
                    channels = emptyList(),
                ),
                onIntent = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
        }
    }
}

/**
 * エラー状態のプレビュー。
 */
@Preview
@Composable
private fun TimelineSyncScreenErrorPreview() {
    AppTheme {
        Surface {
            TimelineSyncScreen(
                uiState = TimelineSyncUiState(
                    isLoading = false,
                    errorMessage = "ネットワークエラーが発生しました",
                ),
                onIntent = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
        }
    }
}

/**
 * 重複確認ダイアログのプレビュー。
 */
@Preview
@Composable
private fun DuplicateHistoryDialogPreview() {
    AppTheme {
        Surface {
            DuplicateHistoryDialog(
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
