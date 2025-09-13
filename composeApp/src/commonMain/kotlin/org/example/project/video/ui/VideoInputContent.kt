package org.example.project.video.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

/**
 * Content Composable (Stateless) - Represents the video input section
 * Groups related Components together with section-specific layout
 */
@Composable
fun VideoInputContent(
    currentVideoId: String,
    onLoadVideo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputVideoId by remember { mutableStateOf(currentVideoId) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "YouTube Video Player Demo",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter a YouTube video ID or try sample videos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Video ID input
            VideoIdInputComponent(
                value = inputVideoId,
                onValueChange = { inputVideoId = it },
                onSubmit = { onLoadVideo(inputVideoId) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sample video buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SampleVideoButton(
                    text = "Sample 1",
                    videoId = "dQw4w9WgXcQ", // Rick Roll - famous test video
                    onClick = { videoId ->
                        inputVideoId = videoId
                        onLoadVideo(videoId)
                    },
                    modifier = Modifier.weight(1f)
                )

                SampleVideoButton(
                    text = "Sample 2",
                    videoId = "9bZkp7q19f0", // PSY - GANGNAM STYLE - another popular test video
                    onClick = { videoId ->
                        inputVideoId = videoId
                        onLoadVideo(videoId)
                    },
                    modifier = Modifier.weight(1f)
                )

                SampleVideoButton(
                    text = "Sample 3",
                    videoId = "kJQP7kiw5Fk", // Luis Fonsi - Despacito - yet another popular test video
                    onClick = { videoId ->
                        inputVideoId = videoId
                        onLoadVideo(videoId)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}