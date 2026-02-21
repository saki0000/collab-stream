@file:OptIn(kotlin.time.ExperimentalTime::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package org.example.project.feature.timeline_sync.sync_history.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.SavedChannelInfo
import org.example.project.domain.model.SyncHistory
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.HistorySortOrder
import org.example.project.feature.timeline_sync.sync_history.SyncHistoryListIntent
import org.example.project.feature.timeline_sync.sync_history.SyncHistoryListUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 同期履歴一覧画面のScreen層（Stateless）。
 *
 * 画面全体のレイアウト（Scaffold）を定義し、状態に応じてContent/空状態/ローディングを表示する。
 * TopAppBarに戻るボタンとソートアイコンを配置する。
 * 削除・名前変更のダイアログもここで管理する。
 *
 * 4層構造: Container -> Screen -> Content -> Component
 *
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 * Story: US-3 (履歴一覧表示)
 *
 * @param uiState 画面の現在状態
 * @param now 現在時刻（Container層から渡す。Clock使用禁止）
 * @param onIntent Intentを処理するコールバック
 * @param onNavigateBack 戻るボタンのコールバック
 * @param snackbarHostState Snackbar表示用の状態
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun SyncHistoryListScreen(
    uiState: SyncHistoryListUiState,
    now: Instant,
    onIntent: (SyncHistoryListIntent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("同期履歴") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "前の画面に戻る",
                        )
                    }
                },
                actions = {
                    // ソートアイコンボタン
                    Box {
                        IconButton(
                            onClick = { onIntent(SyncHistoryListIntent.OpenSortMenu) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "履歴のソート順を変更する",
                            )
                        }

                        // ソートドロップダウンメニュー
                        DropdownMenu(
                            expanded = uiState.isSortMenuVisible,
                            onDismissRequest = { onIntent(SyncHistoryListIntent.CloseSortMenu) },
                        ) {
                            DropdownMenuItem(
                                text = { Text("最終使用日時") },
                                onClick = {
                                    onIntent(
                                        SyncHistoryListIntent.ChangeSortOrder(HistorySortOrder.LAST_USED),
                                    )
                                },
                                trailingIcon = if (uiState.sortOrder == HistorySortOrder.LAST_USED) {
                                    { Text("✓", color = MaterialTheme.colorScheme.primary) }
                                } else {
                                    null
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("作成日時") },
                                onClick = {
                                    onIntent(
                                        SyncHistoryListIntent.ChangeSortOrder(HistorySortOrder.CREATED),
                                    )
                                },
                                trailingIcon = if (uiState.sortOrder == HistorySortOrder.CREATED) {
                                    { Text("✓", color = MaterialTheme.colorScheme.primary) }
                                } else {
                                    null
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("使用回数") },
                                onClick = {
                                    onIntent(
                                        SyncHistoryListIntent.ChangeSortOrder(HistorySortOrder.MOST_USED),
                                    )
                                },
                                trailingIcon = if (uiState.sortOrder == HistorySortOrder.MOST_USED) {
                                    { Text("✓", color = MaterialTheme.colorScheme.primary) }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                uiState.isLoading -> {
                    // ローディング状態
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.isEmpty -> {
                    // 空状態
                    EmptyHistoryState(
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    // 履歴リスト表示
                    SyncHistoryListContent(
                        histories = uiState.histories,
                        now = now,
                        onIntent = onIntent,
                    )
                }
            }
        }
    }

    // 削除確認ダイアログ
    if (uiState.isDeleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { onIntent(SyncHistoryListIntent.DismissDeleteDialog) },
            title = { Text("履歴を削除") },
            text = { Text("この履歴を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = { onIntent(SyncHistoryListIntent.ConfirmDelete) },
                ) {
                    Text(
                        text = "削除",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onIntent(SyncHistoryListIntent.DismissDeleteDialog) },
                ) {
                    Text("キャンセル")
                }
            },
        )
    }

    // 名前変更ダイアログ
    if (uiState.isRenameDialogVisible) {
        AlertDialog(
            onDismissRequest = { onIntent(SyncHistoryListIntent.DismissRenameDialog) },
            title = { Text("名前を変更") },
            text = {
                Column {
                    Text(
                        text = "新しい名前を入力してください。空欄にすると自動生成名に戻ります。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.sm),
                    )
                    OutlinedTextField(
                        value = uiState.renameInput,
                        onValueChange = {
                            onIntent(SyncHistoryListIntent.UpdateRenameInput(it))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("履歴名（空欄で自動生成）") },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onIntent(SyncHistoryListIntent.ConfirmRename) },
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onIntent(SyncHistoryListIntent.DismissRenameDialog) },
                ) {
                    Text("キャンセル")
                }
            },
        )
    }
}

/**
 * 履歴が0件の場合に表示する空状態。
 */
@Composable
private fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "保存した同期履歴がありません",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = "TimelineSyncで同期を保存してみましょう",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun SyncHistoryListScreenLoadingPreview() {
    AppTheme {
        SyncHistoryListScreen(
            uiState = SyncHistoryListUiState(isLoading = true),
            now = Instant.parse("2024-01-15T12:00:00Z"),
            onIntent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun SyncHistoryListScreenEmptyPreview() {
    AppTheme {
        SyncHistoryListScreen(
            uiState = SyncHistoryListUiState(
                isLoading = false,
                histories = emptyList(),
            ),
            now = Instant.parse("2024-01-15T12:00:00Z"),
            onIntent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun SyncHistoryListScreenContentPreview() {
    val now = Instant.parse("2024-01-15T12:00:00Z")
    val histories = listOf(
        SyncHistory(
            id = "1",
            name = "Apex大会グループ",
            channels = listOf(
                SavedChannelInfo("ch1", "チャンネルA", "", VideoServiceType.TWITCH),
                SavedChannelInfo("ch2", "チャンネルB", "", VideoServiceType.YOUTUBE),
            ),
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            lastUsedAt = Instant.parse("2024-01-12T10:00:00Z"),
            usageCount = 5,
        ),
        SyncHistory(
            id = "2",
            name = null,
            channels = listOf(
                SavedChannelInfo("ch3", "チャンネルC", "", VideoServiceType.TWITCH),
                SavedChannelInfo("ch4", "チャンネルD", "", VideoServiceType.TWITCH),
            ),
            createdAt = Instant.parse("2024-01-05T00:00:00Z"),
            lastUsedAt = Instant.parse("2024-01-08T15:00:00Z"),
            usageCount = 2,
        ),
    )
    AppTheme {
        SyncHistoryListScreen(
            uiState = SyncHistoryListUiState(
                isLoading = false,
                histories = histories,
            ),
            now = now,
            onIntent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun SyncHistoryListScreenDeleteDialogPreview() {
    val now = Instant.parse("2024-01-15T12:00:00Z")
    val histories = listOf(
        SyncHistory(
            id = "1",
            name = "Apex大会グループ",
            channels = listOf(
                SavedChannelInfo("ch1", "チャンネルA", "", VideoServiceType.TWITCH),
                SavedChannelInfo("ch2", "チャンネルB", "", VideoServiceType.YOUTUBE),
            ),
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            lastUsedAt = Instant.parse("2024-01-12T10:00:00Z"),
            usageCount = 5,
        ),
    )
    AppTheme {
        SyncHistoryListScreen(
            uiState = SyncHistoryListUiState(
                isLoading = false,
                histories = histories,
                deletingHistoryId = "1",
            ),
            now = now,
            onIntent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun SyncHistoryListScreenRenameDialogPreview() {
    val now = Instant.parse("2024-01-15T12:00:00Z")
    val histories = listOf(
        SyncHistory(
            id = "1",
            name = "Apex大会グループ",
            channels = listOf(
                SavedChannelInfo("ch1", "チャンネルA", "", VideoServiceType.TWITCH),
                SavedChannelInfo("ch2", "チャンネルB", "", VideoServiceType.YOUTUBE),
            ),
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            lastUsedAt = Instant.parse("2024-01-12T10:00:00Z"),
            usageCount = 5,
        ),
    )
    AppTheme {
        SyncHistoryListScreen(
            uiState = SyncHistoryListUiState(
                isLoading = false,
                histories = histories,
                renamingHistoryId = "1",
                renameInput = "Apex大会グループ",
            ),
            now = now,
            onIntent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun SyncHistoryListScreenSortMenuPreview() {
    val now = Instant.parse("2024-01-15T12:00:00Z")
    val histories = listOf(
        SyncHistory(
            id = "1",
            name = "Apex大会グループ",
            channels = listOf(
                SavedChannelInfo("ch1", "チャンネルA", "", VideoServiceType.TWITCH),
                SavedChannelInfo("ch2", "チャンネルB", "", VideoServiceType.YOUTUBE),
            ),
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            lastUsedAt = Instant.parse("2024-01-12T10:00:00Z"),
            usageCount = 5,
        ),
    )
    AppTheme {
        SyncHistoryListScreen(
            uiState = SyncHistoryListUiState(
                isLoading = false,
                histories = histories,
                isSortMenuVisible = true,
                sortOrder = HistorySortOrder.LAST_USED,
            ),
            now = now,
            onIntent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
