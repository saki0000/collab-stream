package org.example.project.feature.video_playback.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlin.time.ExperimentalTime
import org.example.project.domain.usecase.VideoSyncUseCase
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoUiState
import org.example.project.feature.video_playback.player.WebViewPlayerController
import org.koin.compose.koinInject

/**
 * Screen Composable (Stateless) - Main Player Screen with improved layout
 * Layout: VideoPlayer + MainStreamInfo + AddSub + SubStreamsList + SyncFloatingBar(bottom)
 * Receives UiState and Intent callbacks from Container, delegates to Content composables
 */
@OptIn(ExperimentalTime::class)
@Composable
fun VideoScreen(
    uiState: VideoUiState,
    onIntent: (VideoIntent) -> Unit,
    onVideoError: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    onNavigateToSubSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Store player controller reference at screen level
    var playerController by remember { mutableStateOf<WebViewPlayerController?>(null) }

    // Inject VideoSyncUseCase for Bottom Sheet
    val videoSyncUseCase: VideoSyncUseCase = koinInject()

    // LazyListState for tracking scroll position
    val listState = rememberLazyListState()

    // Store video player height
    var videoPlayerHeight by remember { mutableStateOf(0f) }

    // Calculate scale and alpha based on scroll offset
    // Video player is at index 0
    val videoScrollProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                // Calculate progress based on scroll offset
                val scrollOffset = listState.firstVisibleItemScrollOffset.toFloat()
                // Dynamic threshold: 80% of video height (to reach 20% scale)
                val threshold = if (videoPlayerHeight > 0f) {
                    videoPlayerHeight * 0.9f
                } else {
                    800f // Fallback value until height is measured
                }
                (scrollOffset / threshold).coerceIn(0f, 1f)
            } else if (listState.firstVisibleItemIndex > 0) {
                // Video is completely scrolled out
                1f
            } else {
                0f
            }
        }
    }

    // Calculate scale: 1.0 -> 0.2 (minimum 20%)
    val videoScale = (1f - videoScrollProgress).coerceAtLeast(0.2f)
    // Calculate alpha: fade out when scale reaches 20%
    val videoAlpha = if (videoScale <= 0.1f) 0f else 1f - videoScrollProgress

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Main content with LazyColumn (without video player)
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 256.dp, // Space for fixed video player
                    bottom = 96.dp, // Space for floating bar
                ),
            ) {
                // Section 1: Main Stream Info
                item {
                    val mainStream = uiState.mainStream
                    MainStreamInfo(
                        channelName = mainStream?.channelName ?: "",
                        title = mainStream?.title ?: "",
                    )
                }

                // Section 3: Add Sub Button
                item {
                    OutlinedButton(
                        onClick = { onNavigateToSubSearch() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Sub Stream")
                    }
                }

                // Section 4: Sub Streams List
                items(uiState.subStreams) { subStream ->
                    SubStreamItem(
                        stream = subStream,
                        mainTime = uiState.currentTime,
                        onPlayInModal = {
                            onIntent(VideoIntent.ShowSwitchConfirmBottomSheet(subStream))
                        },
                        onSwitchToMain = {
                            onIntent(VideoIntent.SwitchMainSub(subStream.streamId))
                        },
                        onRemove = {
                            onIntent(VideoIntent.RemoveSubStream(subStream.streamId))
                        },
                    )
                }
            }

            // Fixed Video Player at top with scroll-based animation
            VideoPlayerContent(
                uiState = uiState,
                onVideoError = onVideoError,
                onRetry = { onIntent(VideoIntent.RetryLoad) },
                onIntent = onIntent,
                onPlayerControllerReady = { controller ->
                    playerController = controller
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .onSizeChanged { size ->
                        // Capture the actual video player height
                        videoPlayerHeight = size.height.toFloat()
                    }
                    .graphicsLayer {
                        scaleX = videoScale
                        scaleY = videoScale
                        alpha = videoAlpha
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    },
            )

            // Floating Sync Control Bar overlaying the list
            val syncedCount = uiState.subStreams.count { it.isSynced }
            val totalSubCount = uiState.subStreams.size

            SyncFloatingBar(
                absoluteTime = uiState.mainAbsoluteTime,
                syncedCount = syncedCount,
                totalSubCount = totalSubCount,
                isSyncing = uiState.isSyncing,
                onSyncAll = { onGetCurrentTime ->
                    // Get current playback position from player controller
                    playerController?.requestCurrentTime { currentPosition ->
                        onGetCurrentTime(currentPosition)
                        // Send intent with current position
                        onIntent(VideoIntent.SyncAllStreams(currentPosition))
                    } ?: run {
                        // Player not ready
                        println("Player controller not ready for sync")
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            )
        }
    }

    // Sub Stream Playback Bottom Sheet (WebView only)
    if (uiState.showSwitchConfirmBottomSheet && uiState.streamToSwitch != null) {
        SwitchConfirmBottomSheet(
            streamToSwitch = uiState.streamToSwitch,
            mainStreamCurrentTime = uiState.currentTime,
            mainStream = uiState.mainStream,
            videoSyncUseCase = videoSyncUseCase,
            onDismiss = {
                onIntent(VideoIntent.DismissSwitchBottomSheet)
            },
        )
    }
}
