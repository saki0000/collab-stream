package org.example.project.feature.video_playback.ui

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
import org.example.project.VideoPlayerView
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoUiState

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
    // Use mainStream if available, otherwise fall back to legacy videoId
    val mainStream = uiState.mainStream
    val displayVideoId = mainStream?.streamId ?: uiState.videoId
    val displayServiceType = mainStream?.serviceType?.name ?: uiState.serviceType.name
    val displayChannelName = mainStream?.channelName ?: ""
    val displayTitle = mainStream?.title ?: ""

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
                videoId = displayVideoId,
                serviceType = displayServiceType,
                syncDateTime = uiState.syncDateTime,
                channelName = displayChannelName,
                title = displayTitle,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Video player area
            Box(
                modifier = Modifier,
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
    channelName: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (videoId.isNotEmpty()) {
            // Channel name (if available from mainStream)
            if (channelName.isNotEmpty()) {
                Text(
                    text = channelName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Title (if available from mainStream)
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Video ID: $videoId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = serviceType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
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
