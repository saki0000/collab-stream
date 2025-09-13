package org.example.project.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun VideoPlayerView(
    videoId: String,
    modifier: Modifier,
    onError: (String) -> Unit,
) {
    // 不要
}
