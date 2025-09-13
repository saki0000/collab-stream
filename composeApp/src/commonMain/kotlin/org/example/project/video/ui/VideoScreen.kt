package org.example.project.video.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.video.VideoIntent
import org.example.project.video.VideoUiState

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
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Video input section
                VideoInputContent(
                    currentVideoId = uiState.videoId,
                    onLoadVideo = { videoId ->
                        onIntent(VideoIntent.LoadVideo(videoId))
                    },
                    modifier = Modifier.weight(0.3f)
                )

                // Video player section
                VideoPlayerContent(
                    uiState = uiState,
                    onVideoError = onVideoError,
                    onRetry = { onIntent(VideoIntent.RetryLoad) },
                    modifier = Modifier.weight(0.7f)
                )
            }
        }
    }
}