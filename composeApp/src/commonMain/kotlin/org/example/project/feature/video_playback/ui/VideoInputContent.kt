package org.example.project.feature.video_playback.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.domain.model.VideoServiceType

/**
 * Content Composable (Stateless) - Represents the video input section
 * Groups related Components together with section-specific layout
 */
@Composable
fun VideoInputContent(
    currentVideoId: String,
    currentServiceType: VideoServiceType,
    onLoadVideo: (String, VideoServiceType) -> Unit,
    onServiceTypeChange: (VideoServiceType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputVideoId by remember { mutableStateOf(currentVideoId) }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Video Player Demo",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select service and enter video ID or URL",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Service type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onServiceTypeChange(VideoServiceType.YOUTUBE) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentServiceType == VideoServiceType.YOUTUBE) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "YouTube",
                        color = if (currentServiceType == VideoServiceType.YOUTUBE) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }

                Button(
                    onClick = { onServiceTypeChange(VideoServiceType.TWITCH) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentServiceType == VideoServiceType.TWITCH) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "Twitch",
                        color = if (currentServiceType == VideoServiceType.TWITCH) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Video ID input
            VideoIdInputComponent(
                value = inputVideoId,
                onValueChange = { inputVideoId = it },
                onSubmit = { onLoadVideo(inputVideoId, currentServiceType) },
                serviceType = currentServiceType,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
