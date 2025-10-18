package org.example.project.feature.streamer_search.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier,
    viewModel: StreamerSearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

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
            }
        }
    }

    StreamerSearchScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}
