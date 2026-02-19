@file:OptIn(kotlin.time.ExperimentalTime::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package org.example.project.feature.archive_home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Spacing
import org.example.project.feature.archive_home.ArchiveHomeIntent
import org.example.project.feature.archive_home.ArchiveHomeUiState
import org.example.project.feature.archive_home.ArchiveItem
import org.example.project.feature.timeline_sync.channel_add.ChannelAddBottomSheet
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * アーカイブHome画面のScreen層（Stateless）。
 *
 * 画面全体のレイアウトを定義し、状態に応じて適切なContentを表示する。
 *
 * 4層構造: Container -> Screen -> Content -> Component
 *
 * Epic: Channel Follow & Archive Home (US-3)
 * Story: US-3 (Archive Home Display)
 */
@Composable
fun ArchiveHomeScreen(
    uiState: ArchiveHomeUiState,
    onIntent: (ArchiveHomeIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
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
                    LoadingState()
                }

                uiState.hasNoFollowedChannels -> {
                    // フォロー0件の空状態
                    EmptyFollowState(
                        onSearchChannelClick = { onIntent(ArchiveHomeIntent.OpenChannelAddModal) },
                    )
                }

                uiState.hasNoArchives -> {
                    // アーカイブ0件の空状態
                    EmptyArchiveState(selectedDate = uiState.selectedDate)
                }

                uiState.errorMessage != null -> {
                    // エラー状態
                    ErrorState(
                        message = uiState.errorMessage,
                        onRetry = { onIntent(ArchiveHomeIntent.Retry) },
                    )
                }

                else -> {
                    // コンテンツ表示
                    ArchiveHomeContent(
                        archives = uiState.archives,
                        selectedDate = uiState.selectedDate,
                        displayedWeekStart = uiState.displayedWeekStart,
                        onDateSelected = { onIntent(ArchiveHomeIntent.SelectDate(it)) },
                        onNavigateToPreviousWeek = { onIntent(ArchiveHomeIntent.NavigateToPreviousWeek) },
                        onNavigateToNextWeek = { onIntent(ArchiveHomeIntent.NavigateToNextWeek) },
                    )
                }
            }

            // チャンネル追加モーダル（ChannelAddBottomSheet再利用）
            ChannelAddBottomSheet(
                isVisible = uiState.isChannelAddModalVisible,
                searchQuery = uiState.channelSearchQuery,
                channelSuggestions = uiState.channelSuggestions,
                addedChannels = emptyList(), // US-3では追加済みチャンネルは表示しない
                isSearching = uiState.isSearchingChannels,
                errorMessage = null,
                selectedPlatform = uiState.selectedPlatform,
                followedChannelIds = uiState.followedChannelIds,
                onPlatformSelect = { onIntent(ArchiveHomeIntent.SelectPlatform(it)) },
                onSearchQueryChange = { onIntent(ArchiveHomeIntent.UpdateChannelSearchQuery(it)) },
                onChannelSelect = {}, // US-3ではタイムラインへの追加は不要（空実装）
                onChannelRemove = {}, // US-3では削除不要（空実装）
                onToggleFollow = { onIntent(ArchiveHomeIntent.ToggleFollow(it)) },
                onDismiss = { onIntent(ArchiveHomeIntent.CloseChannelAddModal) },
            )
        }
    }
}

/**
 * ローディング状態の表示。
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

/**
 * フォロー0件の空状態。
 */
@Composable
private fun EmptyFollowState(
    onSearchChannelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Spacing.md),
        )

        Text(
            text = "チャンネルをフォローして\nアーカイブを表示しよう",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Button(onClick = onSearchChannelClick) {
            Text("チャンネルを検索")
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * アーカイブ0件の空状態。
 */
@Composable
private fun EmptyArchiveState(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "この日のアーカイブはありません",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * エラー状態の表示。
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Button(onClick = onRetry) {
            Text("再試行")
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun ArchiveHomeScreenLoadingPreview() {
    val today = LocalDate.parse("2024-01-15")
    AppTheme {
        ArchiveHomeScreen(
            uiState = ArchiveHomeUiState(
                isLoading = true,
                selectedDate = today,
                displayedWeekStart = today,
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun ArchiveHomeScreenEmptyFollowPreview() {
    val today = LocalDate.parse("2024-01-15")
    AppTheme {
        ArchiveHomeScreen(
            uiState = ArchiveHomeUiState(
                isLoading = false,
                followedChannels = emptyList(),
                selectedDate = today,
                displayedWeekStart = today,
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun ArchiveHomeScreenEmptyArchivePreview() {
    val today = LocalDate.parse("2024-01-15")
    AppTheme {
        ArchiveHomeScreen(
            uiState = ArchiveHomeUiState(
                isLoading = false,
                followedChannels = listOf(
                    org.example.project.domain.model.FollowedChannel(
                        channelId = "ch1",
                        channelName = "Channel 1",
                        channelIconUrl = "",
                        serviceType = org.example.project.domain.model.VideoServiceType.TWITCH,
                        followedAt = Instant.parse("2024-01-01T00:00:00Z"),
                    ),
                ),
                archives = emptyList(),
                selectedDate = today,
                displayedWeekStart = today,
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun ArchiveHomeScreenContentPreview() {
    val today = LocalDate.parse("2024-01-15")
    AppTheme {
        ArchiveHomeScreen(
            uiState = ArchiveHomeUiState(
                isLoading = false,
                followedChannels = listOf(
                    org.example.project.domain.model.FollowedChannel(
                        channelId = "ch1",
                        channelName = "Channel 1",
                        channelIconUrl = "",
                        serviceType = org.example.project.domain.model.VideoServiceType.TWITCH,
                        followedAt = Instant.parse("2024-01-01T00:00:00Z"),
                    ),
                ),
                archives = listOf(
                    ArchiveItem(
                        videoId = "v1",
                        title = "配信アーカイブ #1",
                        thumbnailUrl = "",
                        channelId = "ch1",
                        channelName = "Channel 1",
                        channelIconUrl = "",
                        serviceType = org.example.project.domain.model.VideoServiceType.TWITCH,
                        publishedAt = Instant.parse("2024-01-15T10:00:00Z"),
                        durationSeconds = 3600f,
                    ),
                ),
                selectedDate = today,
                displayedWeekStart = today,
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun ArchiveHomeScreenErrorPreview() {
    val today = LocalDate.parse("2024-01-15")
    AppTheme {
        ArchiveHomeScreen(
            uiState = ArchiveHomeUiState(
                isLoading = false,
                errorMessage = "ネットワークエラーが発生しました",
                selectedDate = today,
                displayedWeekStart = today,
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
