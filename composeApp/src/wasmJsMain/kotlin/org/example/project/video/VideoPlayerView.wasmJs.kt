package org.example.project.video

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.video.ui.SyncControlsSection

@Composable
actual fun VideoPlayerView(
    videoId: String,
    uiState: VideoUiState,
    onIntent: (VideoIntent) -> Unit,
    modifier: Modifier,
    onError: (String) -> Unit,
) {
    if (videoId.isBlank()) {
        onError("Video ID cannot be empty")
        return
    }

    Column(modifier = modifier) {
        // Web implementation would embed iframe directly here
        // For now, showing placeholder with localhost parent host logic
        Text(
            text = "Web Video Player (${uiState.serviceType}): $videoId\n" +
                "Parent host for Twitch would be: localhost",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))
        SyncControlsSection(
            uiState = uiState,
            onSync = {
                // Web implementation would need to extract current time from iframe
                onIntent(VideoIntent.SyncToAbsoluteTime(0.0f))
            },
        )
    }
}
