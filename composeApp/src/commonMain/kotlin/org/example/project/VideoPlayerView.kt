package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoUiState

/**
 * Platform-specific video player component using expect/actual pattern.
 * Each platform provides its optimal implementation:
 * - Android: YouTube Android Player API + AndroidView
 * - iOS: WKWebView + iframe + UIKitView
 *
 * @param videoId The YouTube video ID to display
 * @param modifier Compose modifier for styling and layout
 * @param onError Callback for handling errors during video loading/playback
 * @param isMainPlayer True if this is the main player, false if sub player
 * @param onControllerReady Callback when the player controller is ready
 */
@Composable
expect fun VideoPlayerView(
    videoId: String,
    uiState: VideoUiState,
    onIntent: (VideoIntent) -> Unit,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {},
    isMainPlayer: Boolean = true,
    onControllerReady: (Any?) -> Unit = {},
)
