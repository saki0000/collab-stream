@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import org.example.project.domain.model.SelectedStreamInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.timeline_sync.TimelineSyncIntent
import org.example.project.feature.timeline_sync.TimelineSyncUiState
import org.example.project.feature.timeline_sync.ui.components.TimelineSyncHeader
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Screen Composable (Stateless) - Main Timeline Sync Screen.
 *
 * Layout: Header + WeekCalendar + ChannelAvatars + SyncTime + TimelineCards + BottomNav
 * Receives UiState and Intent callbacks from Container, delegates to Content composables.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
 */
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
        }
    }
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
            modifier = Modifier.padding(top = 16.dp),
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
            syncStatus = SyncStatus.WAITING,
        ),
    )

    MaterialTheme {
        Surface {
            TimelineSyncScreen(
                uiState = TimelineSyncUiState(
                    isLoading = false,
                    channels = mockChannels,
                ),
                onIntent = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
        }
    }
}

/**
 * Preview with loading state.
 */
@Preview
@Composable
private fun TimelineSyncScreenLoadingPreview() {
    MaterialTheme {
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
 * Preview with empty state.
 */
@Preview
@Composable
private fun TimelineSyncScreenEmptyPreview() {
    MaterialTheme {
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
 * Preview with error state.
 */
@Preview
@Composable
private fun TimelineSyncScreenErrorPreview() {
    MaterialTheme {
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
