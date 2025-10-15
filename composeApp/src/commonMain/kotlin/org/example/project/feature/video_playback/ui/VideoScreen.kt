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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoUiState
import kotlin.time.ExperimentalTime

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
    onNavigateToSearch: (initialQuery: String) -> Unit,
    onNavigateToSubSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                contentPadding = PaddingValues(16.dp),
            ) {
                // Section 1: Main Player Section
                item {
                    VideoPlayerContent(
                        uiState = uiState,
                        onVideoError = onVideoError,
                        onRetry = { onIntent(VideoIntent.RetryLoad) },
                        onIntent = onIntent,
                    )
                }

                // Section 2: Sync Control Bar
                item {
                    val syncedCount = uiState.subStreams.count { it.isSynced }
                    val totalSubCount = uiState.subStreams.size

                    SyncControlBar(
                        currentTime = uiState.currentTime,
                        absoluteTime = uiState.mainAbsoluteTime,
                        syncedCount = syncedCount,
                        totalSubCount = totalSubCount,
                        isSyncing = uiState.isSyncing,
                        onSyncAll = { onIntent(VideoIntent.SyncAllStreams) },
                        onAddSub = { onNavigateToSubSearch() },
                    )
                }

                // Section 3: Sub Streams List
                items(uiState.subStreams) { subStream ->
                    SubStreamItem(
                        stream = subStream,
                        mainTime = uiState.currentTime,
                        onSwitchToMain = {
                            onIntent(VideoIntent.SwitchMainSub(subStream.streamId))
                        },
                        onRemove = {
                            onIntent(VideoIntent.RemoveSubStream(subStream.streamId))
                        },
                    )
                }
            }
        }
    }
}
