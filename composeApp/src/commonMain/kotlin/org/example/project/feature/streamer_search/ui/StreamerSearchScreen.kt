package org.example.project.feature.streamer_search.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.feature.streamer_search.StreamerSearchIntent
import org.example.project.feature.streamer_search.StreamerSearchUiState

/**
 * Streamer Search Screen (BottomSheet)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamerSearchScreen(
    uiState: StreamerSearchUiState,
    onIntent: (StreamerSearchIntent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            StreamerSearchContent(
                searchMode = uiState.searchMode,
                inputText = uiState.inputText,
                searchQuery = uiState.searchQuery,
                searchResults = uiState.searchResults,
                isSearching = uiState.isSearching,
                searchError = uiState.searchError,
                hasMoreResults = uiState.searchNextPageToken != null,
                selectedDate = uiState.selectedDate,
                selectedService = uiState.selectedService,
                channelSuggestions = uiState.channelSuggestions,
                isSearchingChannels = uiState.isSearchingChannels,
                selectedResults = uiState.selectedResults,
                onInputTextChange = { text -> onIntent(StreamerSearchIntent.UpdateInputText(text)) },
                onExecuteSearch = { onIntent(StreamerSearchIntent.ExecuteSearch) },
                onSelectResult = { result ->
                    if (uiState.searchMode == "SUB") {
                        onIntent(StreamerSearchIntent.ToggleResultSelection(result))
                    } else {
                        onIntent(StreamerSearchIntent.SelectSearchResult(result))
                    }
                },
                onLoadMore = { onIntent(StreamerSearchIntent.LoadMoreSearchResults) },
                onClearError = { onIntent(StreamerSearchIntent.ClearSearchError) },
                onSelectService = { service -> onIntent(StreamerSearchIntent.SelectService(service)) },
                onSearchChannels = { query -> onIntent(StreamerSearchIntent.SearchChannels(query)) },
                onSelectChannel = { channel -> onIntent(StreamerSearchIntent.SelectChannel(channel)) },
                onDismiss = onDismiss,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            )
        }
    }
}
