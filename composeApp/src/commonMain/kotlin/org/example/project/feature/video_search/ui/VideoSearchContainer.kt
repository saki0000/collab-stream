package org.example.project.feature.video_search.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.example.project.feature.video_search.VideoSearchSideEffect
import org.example.project.feature.video_search.VideoSearchViewModel
import org.example.project.feature.video_search.VideoSelectionResult
import org.koin.compose.viewmodel.koinViewModel

/**
 * Container (Stateful) - Connects to ViewModel and manages state
 * This is the only stateful composable in the hierarchy following the 4-tier pattern:
 * Container -> Screen -> Content -> Component
 *
 * Handles side effects with LaunchedEffect and Intent pattern for navigation.
 */
@Composable
fun VideoSearchContainer(
    onDismiss: () -> Unit,
    onVideoSelected: (VideoSelectionResult) -> Unit,
    isSubSearchMode: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: VideoSearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Set sub search mode when container is created
    LaunchedEffect(isSubSearchMode) {
        viewModel.setSubSearchMode(isSubSearchMode)
    }

    // Handle side effects with LaunchedEffect and Intent pattern
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is VideoSearchSideEffect.NavigateBack -> onDismiss()
                is VideoSearchSideEffect.VideoSelected -> {
                    onVideoSelected(
                        VideoSelectionResult(
                            videoId = effect.videoId,
                            serviceType = effect.serviceType,
                        ),
                    )
                    // Don't dismiss in sub search mode, allow multiple selections
                    if (!isSubSearchMode) {
                        onDismiss()
                    }
                }
                is VideoSearchSideEffect.ShowSearchError -> {
                    // Error is already shown in UI state, no additional action needed
                }
                is VideoSearchSideEffect.ShowSearchSuccess -> {
                    // Success message could be shown via Snackbar if needed
                }
            }
        }
    }

    VideoSearchScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}
