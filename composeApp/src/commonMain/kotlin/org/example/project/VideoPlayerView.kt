package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoUiState
import org.example.project.feature.video_playback.player.WebViewPlayerController

/**
 * Platform-specific video player component using expect/actual pattern.
 * Each platform provides its optimal implementation:
 * - Android: YouTube Android Player API + AndroidView
 * - iOS: WKWebView + iframe + UIKitView
 *
 * @param videoId The YouTube video ID to display
 * @param modifier Compose modifier for styling and layout
 * @param onError Callback for handling errors during video loading/playback
 * @param onPlayerControllerReady Callback invoked when the player controller is initialized and ready
 */
@Composable
expect fun VideoPlayerView(
    videoId: String,
    uiState: VideoUiState,
    onIntent: (VideoIntent) -> Unit,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {},
    onPlayerControllerReady: (WebViewPlayerController) -> Unit = {},
)
