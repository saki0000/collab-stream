package org.example.project.feature.video_search.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.feature.video_search.VideoSearchIntent
import org.example.project.feature.video_search.VideoSearchUiState

/**
 * Screen (Stateless) - Defines overall screen layout and structure
 * Wraps the ModalBottomSheet and delegates to Content composables
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoSearchScreen(
    uiState: VideoSearchUiState,
    onIntent: (VideoSearchIntent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        VideoSearchContent(
            searchQuery = uiState.searchQuery,
            searchResults = uiState.searchResults,
            isSearching = uiState.isSearching,
            searchError = uiState.searchError,
            hasMoreResults = uiState.searchNextPageToken != null,
            onSearchQuery = { query -> onIntent(VideoSearchIntent.SearchVideos(query)) },
            onSelectResult = { result -> onIntent(VideoSearchIntent.SelectSearchResult(result)) },
            onLoadMore = { onIntent(VideoSearchIntent.LoadMoreSearchResults) },
            onClearError = { onIntent(VideoSearchIntent.ClearSearchError) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}
