package org.example.project.feature.video_search.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
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
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        sheetMaxWidth = Dp.Unspecified,
    ) {
        VideoSearchContent(
            inputText = uiState.inputText,
            searchQuery = uiState.searchQuery,
            searchResults = uiState.searchResults,
            isSearching = uiState.isSearching,
            searchError = uiState.searchError,
            hasMoreResults = uiState.searchNextPageToken != null,
            selectedDate = uiState.selectedDate,
            searchMode = uiState.searchMode,
            selectedService = uiState.selectedService,
            isSubSearchMode = uiState.isSubSearchMode,
            selectedResults = uiState.selectedResults,
            onInputTextChange = { text -> onIntent(VideoSearchIntent.UpdateInputText(text)) },
            onExecuteSearch = { onIntent(VideoSearchIntent.ExecuteSearch) },
            onSelectResult = { result ->
                if (uiState.isSubSearchMode) {
                    onIntent(VideoSearchIntent.ToggleResultSelection(result))
                } else {
                    onIntent(VideoSearchIntent.SelectSearchResult(result))
                }
            },
            onLoadMore = { onIntent(VideoSearchIntent.LoadMoreSearchResults) },
            onClearError = { onIntent(VideoSearchIntent.ClearSearchError) },
            onDateChange = { date -> onIntent(VideoSearchIntent.ChangeSelectedDate(date)) },
            onSearchModeChange = { mode -> onIntent(VideoSearchIntent.ChangeSearchMode(mode)) },
            onSelectService = { service -> onIntent(VideoSearchIntent.SelectService(service)) },
            onDismiss = onDismiss,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        )
    }
}
