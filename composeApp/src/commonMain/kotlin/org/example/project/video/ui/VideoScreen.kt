package org.example.project.video.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.video.VideoIntent
import org.example.project.video.VideoUiState

/**
 * Screen Composable (Stateless) - Defines overall screen layout and structure
 * Receives UiState and Intent callbacks from Container, delegates to Content composables
 */
@Composable
fun VideoScreen(
    uiState: VideoUiState,
    onIntent: (VideoIntent) -> Unit,
    onVideoError: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(VideoIntent.ToggleSearchBottomSheet) },
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Videos",
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Video input section
                VideoInputContent(
                    currentVideoId = uiState.videoId,
                    currentServiceType = uiState.serviceType,
                    onLoadVideo = { videoId, serviceType ->
                        onIntent(VideoIntent.LoadVideoWithService(videoId, serviceType))
                    },
                    onServiceTypeChange = { serviceType ->
                        onIntent(VideoIntent.ChangeServiceType(serviceType))
                    },
                )

                // Video player section
                VideoPlayerContent(
                    uiState = uiState,
                    onVideoError = onVideoError,
                    onRetry = { onIntent(VideoIntent.RetryLoad) },
                    onIntent = onIntent,
                )
            }
        }

        // Search Bottom Sheet
        VideoSearchBottomSheet(
            isVisible = uiState.isSearchBottomSheetVisible,
            searchQuery = uiState.searchQuery,
            searchResults = uiState.searchResults,
            isSearching = uiState.isSearching,
            searchError = uiState.searchError,
            hasMoreResults = uiState.searchNextPageToken != null,
            onDismiss = { onIntent(VideoIntent.ToggleSearchBottomSheet) },
            onSearchQuery = { query -> onIntent(VideoIntent.SearchVideos(query)) },
            onSelectResult = { result -> onIntent(VideoIntent.SelectSearchResult(result)) },
            onLoadMore = { onIntent(VideoIntent.LoadMoreSearchResults) },
            onClearError = { onIntent(VideoIntent.ClearSearchError) },
        )
    }
}
