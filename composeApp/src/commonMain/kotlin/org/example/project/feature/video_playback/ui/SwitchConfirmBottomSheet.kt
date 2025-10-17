package org.example.project.feature.video_playback.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.time.ExperimentalTime
import org.example.project.VideoPlayerView
import org.example.project.domain.model.StreamInfo
import org.example.project.domain.usecase.VideoSyncUseCase
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoUiState
import org.example.project.feature.video_playback.player.WebViewPlayerController

/**
 * Bottom Sheet for displaying Sub Stream playback with WebView only
 *
 * This component:
 * 1. Calculates sync position based on main stream's current time
 * 2. Displays WebView player at the synced position
 * 3. No additional UI - just the video player
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun SwitchConfirmBottomSheet(
    streamToSwitch: StreamInfo,
    mainStreamCurrentTime: Float,
    mainStream: StreamInfo?,
    videoSyncUseCase: VideoSyncUseCase,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var syncPosition by remember { mutableStateOf<Float?>(null) }
    var playerController by remember { mutableStateOf<WebViewPlayerController?>(null) }

    // Calculate sync position when bottom sheet opens
    LaunchedEffect(streamToSwitch, mainStreamCurrentTime) {
        if (mainStream != null) {
            try {
                // 1. Get main stream's absolute time
                val mainSyncResult = videoSyncUseCase.syncVideoToAbsoluteTime(
                    mainStream.streamId,
                    mainStreamCurrentTime,
                    mainStream.serviceType,
                )

                mainSyncResult.fold(
                    onSuccess = { mainSyncInfo ->
                        val mainAbsoluteTime = mainSyncInfo.absoluteTime

                        // 2. Calculate target position for the stream to switch
                        val subSyncResult = videoSyncUseCase.syncVideoToAbsoluteTime(
                            streamToSwitch.streamId,
                            0f,
                            streamToSwitch.serviceType,
                        )

                        subSyncResult.fold(
                            onSuccess = { subSyncInfo ->
                                val elapsedSeconds =
                                    (mainAbsoluteTime - subSyncInfo.streamStartTime).inWholeSeconds.toFloat()

                                syncPosition = if (elapsedSeconds < 0) 0f else elapsedSeconds
                            },
                            onFailure = {
                                // On error, start from beginning
                                syncPosition = 0f
                            }
                        )
                    },
                    onFailure = {
                        // On error, start from beginning
                        syncPosition = 0f
                    }
                )
            } catch (e: Exception) {
                // On error, start from beginning
                syncPosition = 0f
            }
        }
    }

    // Seek to sync position when both position and controller are ready
    LaunchedEffect(syncPosition, playerController) {
        if (syncPosition != null && playerController != null) {
            // Small delay to ensure player is fully initialized
            kotlinx.coroutines.delay(1000)
            playerController?.seekTo(syncPosition!!)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        // Display WebView only
        VideoPlayerView(
            videoId = streamToSwitch.streamId,
            uiState = VideoUiState(
                videoId = streamToSwitch.streamId,
                serviceType = streamToSwitch.serviceType,
                mainStream = streamToSwitch,
            ),
            onIntent = { /* No intent handling needed */ },
            onPlayerControllerReady = { controller ->
                playerController = controller
            },
        )
    }
}
