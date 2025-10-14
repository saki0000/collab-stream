package org.example.project.feature.video_playback.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.VideoPlayerView
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoUiState

/**
 * Screen Composable (Stateless) - Defines overall screen layout and structure
 * Receives UiState and Intent callbacks from Container, delegates to Content composables
 */
@Composable
fun VideoScreen(
    uiState: VideoUiState,
    onIntent: (VideoIntent) -> Unit,
    onVideoError: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    onNavigateToSearch: (initialQuery: String, selectionTarget: String) -> Unit,
    onMainControllerReady: (Any?) -> Unit,
    onSubControllerReady: (Any?) -> Unit,
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
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                // Video Sync Mode - Always show
                Text(
                    text = "Video Sync Mode",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                val hasMainVideo = uiState.mainVideo.videoId.isNotBlank()
                val hasSubVideo = uiState.subVideo.videoId.isNotBlank()

                // Main Video Section (always show)
                VideoPlayerSection(
                    title = "Main Video (主動画)",
                    videoInfo = uiState.mainVideo,
                    onVideoError = onVideoError,
                    onIntent = onIntent,
                    onAddVideo = {
                        onNavigateToSearch("", "MAIN")
                    },
                    isMain = true,
                    onControllerReady = onMainControllerReady,
                )

                // Sync Button (show only when both videos are loaded)
                if (hasMainVideo && hasSubVideo) {
                    Button(
                        onClick = { onIntent(VideoIntent.SyncMainToSub) },
                        enabled = uiState.mainVideo.isPlayerReady && uiState.subVideo.isPlayerReady && !uiState.isSyncing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(if (uiState.isSyncing) "Syncing..." else "Sync Main → Sub")
                    }
                }

                // Sub Video Section (always show)
                VideoPlayerSection(
                    title = "Sub Video (副動画)",
                    videoInfo = uiState.subVideo,
                    onVideoError = onVideoError,
                    onIntent = onIntent,
                    onAddVideo = {
                        onNavigateToSearch("", "SUB")
                    },
                    isMain = false,
                    onControllerReady = onSubControllerReady,
                )
            }
        }
    }
}

/**
 * VideoPlayerSection - Displays a single video player with title
 */
@Composable
private fun VideoPlayerSection(
    title: String,
    videoInfo: org.example.project.feature.video_playback.VideoPlayerInfo,
    onVideoError: (String) -> Unit,
    onIntent: (VideoIntent) -> Unit,
    onAddVideo: () -> Unit,
    isMain: Boolean,
    onControllerReady: (Any?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            // Check if video is selected
            if (videoInfo.videoId.isBlank()) {
                // No video selected - Show Add button
                OutlinedButton(
                    onClick = onAddVideo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        Text(
                            text = "Add ${if (isMain) "Main" else "Sub"} Video",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            } else {
                // Video selected - Show video player
                // Create temporary VideoUiState for this video
                val videoUiState = VideoUiState(
                    videoId = videoInfo.videoId,
                    serviceType = videoInfo.serviceType,
                    playerState = videoInfo.playerState,
                    currentTime = videoInfo.currentTime,
                    isPlayerReady = videoInfo.isPlayerReady,
                    // Copy main/sub video info for proper state management
                    mainVideo = if (isMain) videoInfo else org.example.project.feature.video_playback.VideoPlayerInfo(),
                    subVideo = if (!isMain) videoInfo else org.example.project.feature.video_playback.VideoPlayerInfo(),
                )

                // Actual Video Player
                VideoPlayerView(
                    videoId = videoInfo.videoId,
                    uiState = videoUiState,
                    onIntent = onIntent,
                    onError = onVideoError,
                    isMainPlayer = isMain,
                    onControllerReady = onControllerReady,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
            }
        }
    }
}
