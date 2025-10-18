package org.example.project.feature.video_playback.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.VideoPlayerView
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoUiState
import org.example.project.feature.video_playback.player.WebViewPlayerController

/**
 * Content Composable (Stateless) - Represents the video player section
 * Displays WebView with rounded corners
 */
@Composable
fun VideoPlayerContent(
    uiState: VideoUiState,
    onVideoError: (String) -> Unit,
    onRetry: () -> Unit,
    onIntent: (VideoIntent) -> Unit,
    onPlayerControllerReady: (WebViewPlayerController?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Use mainStream if available, otherwise fall back to legacy videoId
    val mainStream = uiState.mainStream
    val displayVideoId = mainStream?.streamId ?: uiState.videoId

    // Video player area
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when {
            displayVideoId.isEmpty() -> {
                EmptyStateComponent(
                    message = "Search and select a main streamer to start",
                    modifier = Modifier,
                )
            }

            uiState.errorMessage != null -> {
                ErrorStateComponent(
                    errorMessage = uiState.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier,
                )
            }

            else -> {
                VideoPlayerView(
                    videoId = displayVideoId,
                    onError = onVideoError,
                    uiState = uiState,
                    onIntent = onIntent,
                    onPlayerControllerReady = { controller ->
                        onPlayerControllerReady(controller)
                    },
                )
            }
        }
    }
}
