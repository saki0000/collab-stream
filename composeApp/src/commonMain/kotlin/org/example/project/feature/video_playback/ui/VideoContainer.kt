package org.example.project.feature.video_playback.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
 */
@Composable
fun VideoContainer(
    onNavigateToSearch: (initialQuery: String) -> Unit,
    videoSelectionResult: VideoSelectionResult?,
    modifier: Modifier = Modifier,
    viewModel: VideoViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    // Process video selection result when received from navigation layer
    LaunchedEffect(videoSelectionResult) {
        videoSelectionResult?.let { result ->
            // Load the selected video
            viewModel.handleIntent(
                VideoIntent.LoadVideoWithService(result.videoId, result.serviceType),
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
            }
        }
    }

    VideoScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onVideoError = viewModel::handleVideoError,
        snackbarHostState = snackBarHostState,
        onNavigateToSearch = onNavigateToSearch,
        modifier = modifier,
    )
}
