package org.example.project.feature.video_playback.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.ExperimentalTime
import org.example.project.domain.usecase.VideoSyncUseCase
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoUiState
import org.example.project.feature.video_playback.player.WebViewPlayerController
import org.koin.compose.koinInject

/**
 * Screen Composable (Stateless) - Main Player Screen with 3-section hierarchical layout
 * Following design doc: MainPlayerSection + SyncControlBar + SubStreamsList
 * Receives UiState and Intent callbacks from Container, delegates to Content composables
 */
@OptIn(ExperimentalTime::class)
@Composable
fun VideoScreen(
    uiState: VideoUiState,
    onIntent: (VideoIntent) -> Unit,
    onVideoError: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    onNavigateToSubSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Store player controller reference at screen level
    var playerController by remember { mutableStateOf<WebViewPlayerController?>(null) }

    // Inject VideoSyncUseCase for Bottom Sheet
    val videoSyncUseCase: VideoSyncUseCase = koinInject()

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues),
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(8.dp, 16.dp),
            ) {
                // Section 1: Main Player Section
                item {
                    VideoPlayerContent(
                        uiState = uiState,
                        onVideoError = onVideoError,
                        onRetry = { onIntent(VideoIntent.RetryLoad) },
                        onIntent = onIntent,
                        onPlayerControllerReady = { controller ->
                            playerController = controller
                        },
                    )
                }

                // Section 2: Sync Control Bar
                item {
                    val syncedCount = uiState.subStreams.count { it.isSynced }
                    val totalSubCount = uiState.subStreams.size

                    SyncControlBar(
                        absoluteTime = uiState.mainAbsoluteTime,
                        syncedCount = syncedCount,
                        totalSubCount = totalSubCount,
                        isSyncing = uiState.isSyncing,
                        onSyncAll = { onGetCurrentTime ->
                            // Get current playback position from player controller
                            playerController?.requestCurrentTime { currentPosition ->
                                onGetCurrentTime(currentPosition)
                                // Send intent with current position
                                onIntent(VideoIntent.SyncAllStreams(currentPosition))
                            } ?: run {
                                // Player not ready
                                println("Player controller not ready for sync")
                            }
                        },
                        onAddSub = { onNavigateToSubSearch() },
                    )
                }

                // Section 3: Sub Streams List
                items(uiState.subStreams) { subStream ->
                    SubStreamItem(
                        stream = subStream,
                        mainTime = uiState.currentTime,
                        onSwitchToMain = {
                            onIntent(VideoIntent.ShowSwitchConfirmBottomSheet(subStream))
                        },
                        onRemove = {
                            onIntent(VideoIntent.RemoveSubStream(subStream.streamId))
                        },
                    )
                }
            }
        }
    }

    // Sub Stream Playback Bottom Sheet (WebView only)
    if (uiState.showSwitchConfirmBottomSheet && uiState.streamToSwitch != null) {
        SwitchConfirmBottomSheet(
            streamToSwitch = uiState.streamToSwitch!!,
            mainStreamCurrentTime = uiState.currentTime,
            mainStream = uiState.mainStream,
            videoSyncUseCase = videoSyncUseCase,
            onDismiss = {
                onIntent(VideoIntent.DismissSwitchBottomSheet)
            },
        )
    }
}
