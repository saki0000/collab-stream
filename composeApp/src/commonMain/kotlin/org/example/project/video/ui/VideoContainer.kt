package org.example.project.video.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.video.VideoSideEffect
import org.example.project.video.VideoViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Container Composable (Stateful) - Connects to ViewModel and manages state
 * This is the only stateful composable in the hierarchy following the 4-tier pattern:
 * Container -> Screen -> Content -> Component
 */
@Composable
fun VideoContainer(
    modifier: Modifier = Modifier,
    viewModel: VideoViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

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

                is VideoSideEffect.ShowSearchError -> {
                    //TODO()
                     }
                is VideoSideEffect.ShowSearchSuccess -> {
                    //TODO()
                     }
            }
        }
    }

    VideoScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onVideoError = viewModel::handleVideoError,
        snackbarHostState = snackBarHostState,
        modifier = modifier,
    )
}
