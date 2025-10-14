package org.example.project.feature.video_playback.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoSideEffect
import org.example.project.feature.video_playback.VideoViewModel
import org.example.project.feature.video_search.VideoSelectionResult
import org.koin.compose.viewmodel.koinViewModel

/**
 * Container Composable (Stateful) - Connects to ViewModel and manages state
 * This is the only stateful composable in the hierarchy following the 4-tier pattern:
 * Container -> Screen -> Content -> Component
 *
 * Receives video selection results from navigation layer (passed as parameter).
 * Navigation handling is managed by the NavGraph layer for proper separation of concerns.
 *
 * @param mainVideoResult Main video selection result from navigation
 * @param subVideoResult Sub video selection result from navigation
 */
@Composable
fun VideoContainer(
    onNavigateToSearch: (initialQuery: String, selectionTarget: String) -> Unit,
    mainVideoResult: VideoSelectionResult? = null,
    subVideoResult: VideoSelectionResult? = null,
    modifier: Modifier = Modifier,
    viewModel: VideoViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    // Store references to player controllers
    var mainPlayerController by remember { mutableStateOf<Any?>(null) }
    var subPlayerController by remember { mutableStateOf<Any?>(null) }

    // Process main video selection result when received from navigation layer
    LaunchedEffect(mainVideoResult) {
        mainVideoResult?.let { result ->
            // Load the main video
            viewModel.handleIntent(
                VideoIntent.LoadMainVideo(result.videoId, result.serviceType),
            )
        }
    }

    // Process sub video selection result when received from navigation layer
    LaunchedEffect(subVideoResult) {
        subVideoResult?.let { result ->
            // Load the sub video
            viewModel.handleIntent(
                VideoIntent.LoadSubVideo(result.videoId, result.serviceType),
            )
        }
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is VideoSideEffect.ShowError -> {
                    snackBarHostState.showSnackbar(
                        message = sideEffect.message,
                        actionLabel = "Dismiss",
                        duration = SnackbarDuration.Short,
                    )
                }

                is VideoSideEffect.ShowSuccess -> {
                    snackBarHostState.showSnackbar(
                        message = sideEffect.message,
                        actionLabel = "OK",
                        duration = SnackbarDuration.Short,
                    )
                }

                is VideoSideEffect.ShowSyncResult -> {
                    snackBarHostState.showSnackbar(
                        message = "Synchronized to: ${sideEffect.absoluteTime}",
                        actionLabel = "OK",
                        duration = SnackbarDuration.Long,
                    )
                }

                is VideoSideEffect.ShowSyncError -> {
                    snackBarHostState.showSnackbar(
                        message = "Sync Error: ${sideEffect.message}",
                        actionLabel = "Dismiss",
                        duration = SnackbarDuration.Long,
                    )
                }

                is VideoSideEffect.SeekSubVideo -> {
                    // Handle sub video seek using the controller reference
                    subPlayerController?.let { controller ->
                        try {
                            // Cast to WebViewPlayerController and call seekTo
                            when (controller) {
                                is org.example.project.feature.video_playback.player.WebViewPlayerController -> {
                                    controller.seekTo(sideEffect.seconds) { success ->
                                        if (success) {
                                            println("Sub video seeked to ${sideEffect.seconds}s successfully")
                                        } else {
                                            println("Failed to seek sub video")
                                        }
                                    }
                                }
                                else -> {
                                    println("Unknown controller type: ${controller::class.simpleName}")
                                }
                            }
                        } catch (e: Exception) {
                            println("Error seeking sub video: ${e.message}")
                        }
                    } ?: run {
                        println("Sub player controller not ready yet")
                    }
                }

                is VideoSideEffect.RequestMainPlayerTime -> {
                    // Handle request for main player's current time
                    mainPlayerController?.let { controller ->
                        try {
                            when (controller) {
                                is org.example.project.feature.video_playback.player.WebViewPlayerController -> {
                                    controller.requestCurrentTime { currentTime ->
                                        // Send the time back to ViewModel via intent
                                        viewModel.handleIntent(VideoIntent.SyncMainToSubWithTime(currentTime))
                                    }
                                }
                                else -> {
                                    println("Cannot get current time: unknown controller type")
                                }
                            }
                        } catch (e: Exception) {
                            println("Error requesting current time: ${e.message}")
                        }
                    } ?: run {
                        println("Main player controller not ready yet")
                    }
                }
            }
        }
    }

    VideoScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onVideoError = viewModel::handleVideoError,
        snackbarHostState = snackBarHostState,
        onNavigateToSearch = onNavigateToSearch,
        onMainControllerReady = { mainPlayerController = it },
        onSubControllerReady = { subPlayerController = it },
        modifier = modifier,
    )
}
