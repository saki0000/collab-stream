package org.example.project.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific video player component using expect/actual pattern.
 * Each platform provides its optimal implementation:
 * - Android: YouTube Android Player API + AndroidView
 * - iOS: WKWebView + iframe + UIKitView
 *
 * @param videoId The YouTube video ID to display
 * @param modifier Compose modifier for styling and layout
 * @param onError Callback for handling errors during video loading/playback
 */
@Composable
expect fun VideoPlayerView(
    videoId: String,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {}
)