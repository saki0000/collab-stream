package org.example.project.feature.streamer_search.ui

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.streamer_search.StreamerSearchSideEffect
import org.example.project.feature.streamer_search.StreamerSearchViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Container for Streamer Search
 */
@Composable
fun StreamerSearchContainer(
    onDismiss: () -> Unit,
    onStreamerSelected: (SearchResult, VideoServiceType) -> Unit,
    onStreamRemoved: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    existingSubStreamIds: List<String> = emptyList(),
    mainStreamId: String? = null,
    viewModel: StreamerSearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val localDensity = LocalDensity.current

    // Initialize existing sub stream selection and main stream ID
    LaunchedEffect(existingSubStreamIds, mainStreamId) {
        viewModel.initializeExistingSubStreams(existingSubStreamIds, mainStreamId)
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is StreamerSearchSideEffect.StreamerSelected -> {
                    onStreamerSelected(sideEffect.searchResult, sideEffect.serviceType)
                    // NavGraph handles navigation for both MAIN and SUB modes
                    // - MAIN: navController.navigate() removes BottomSheet from backstack
                    // - SUB: navController.popBackStack() closes BottomSheet
                }
                is StreamerSearchSideEffect.StreamerRemoved -> {
                    onStreamRemoved(sideEffect.videoId)
                }
            }
        }
    }

    StreamerSearchScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onDismiss = {
            // Save current selected stream IDs before dismissing
            // This will be used by VideoContainer to detect removals
            if (uiState.searchMode == "SUB") {
                // Get the savedStateHandle from the previous entry to update the selected IDs
                // This allows VideoContainer to know which streams are still selected
                val currentSelectedIds = uiState.selectedResults.map { it.videoId }
                // We'll use a different key to track current selection for removal detection
                // The VideoContainer will compare this with existing_sub_stream_ids
            }
            onDismiss()
        },
        modifier = modifier.statusBarsPadding(),
    )
}
