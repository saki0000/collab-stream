package org.example.project.video.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.video.VideoIntent
import org.example.project.video.VideoSideEffect
import org.example.project.video.VideoViewModel

/**
 * Container Composable (Stateful) - Connects to ViewModel and manages state
 * This is the only stateful composable in the hierarchy following the 4-tier pattern:
 * Container -> Screen -> Content -> Component
 */
@Composable
fun VideoContainer(
    modifier: Modifier = Modifier,
    viewModel: VideoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is VideoSideEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        actionLabel = "Dismiss"
                    )
                }
                is VideoSideEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        actionLabel = "OK"
                    )
                }
            }
        }
    }

    VideoScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onVideoError = viewModel::handleVideoError,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}