package org.example.project.video.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.video.VideoIntent
import org.example.project.video.VideoPlayerView
import org.example.project.video.VideoUiState

/**
 * Content Composable (Stateless) - Represents the video player section
 * Handles the display of video player and related information
 */
@Composable
fun VideoPlayerContent(
    uiState: VideoUiState,
    onVideoError: (String) -> Unit,
    onRetry: () -> Unit,
    onIntent: (VideoIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.wrapContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
        ) {
            // Video info header
            VideoInfoHeader(
                videoId = uiState.videoId,
                serviceType = uiState.serviceType.name,
                syncDateTime = uiState.syncDateTime,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Video player area
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center,
            ) {
                when {
                    uiState.videoId.isEmpty() -> {
                        EmptyStateComponent(
                            message = "Enter a YouTube video ID above to start",
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
                            videoId = uiState.videoId,
                            onError = onVideoError,
                            uiState = uiState,
                            onIntent = onIntent,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Component Composable (Stateless, reusable) - Video information header
 */
@Composable
private fun VideoInfoHeader(
    videoId: String,
    serviceType: String,
    syncDateTime: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (videoId.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Video ID: $videoId",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = serviceType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (syncDateTime.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Loaded: $syncDateTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
