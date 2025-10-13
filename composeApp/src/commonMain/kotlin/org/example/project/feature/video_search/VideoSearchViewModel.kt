package org.example.project.feature.video_search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.usecase.VideoSearchUseCase

/**
 * ViewModel for video search functionality following MVI architecture pattern.
 * Manages search state and handles user intents for the search screen.
 */
class VideoSearchViewModel(
    private val videoSearchUseCase: VideoSearchUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoSearchUiState())
    val uiState: StateFlow<VideoSearchUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<VideoSearchSideEffect>()
    val sideEffect: SharedFlow<VideoSearchSideEffect> = _sideEffect.asSharedFlow()

    init {
        // Get initial query from navigation arguments if provided
        val initialQuery = savedStateHandle.get<String>("initialQuery") ?: ""
        if (initialQuery.isNotBlank()) {
            searchVideos(initialQuery)
        }
    }

    /**
     * Handles user intents and updates state accordingly
     */
    fun handleIntent(intent: VideoSearchIntent) {
        when (intent) {
            is VideoSearchIntent.SearchVideos -> searchVideos(intent.query)
            VideoSearchIntent.LoadMoreSearchResults -> loadMoreSearchResults()
            is VideoSearchIntent.SelectSearchResult -> selectSearchResult(intent.searchResult)
            VideoSearchIntent.ClearSearchError -> clearSearchError()
            VideoSearchIntent.Dismiss -> dismiss()
            VideoSearchIntent.ClearSearchResults -> clearSearchResults()
        }
    }

    private fun searchVideos(query: String) {
        if (query.isBlank()) {
            handleSearchError("Search query cannot be empty")
            return
        }

        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearching = true,
            searchError = null,
        )

        viewModelScope.launch {
            try {
                val result = videoSearchUseCase.searchVideos(query, preferArchived = true)

                result.fold(
                    onSuccess = { searchResponse ->
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            searchResults = searchResponse.results,
                            searchNextPageToken = searchResponse.nextPageToken,
                            searchError = null,
                        )

                        _sideEffect.emit(
                            VideoSearchSideEffect.ShowSearchSuccess("Found ${searchResponse.results.size} videos"),
                        )
                    },
                    onFailure = { error ->
                        handleSearchError("Search failed: ${error.message}")
                    },
                )
            } catch (e: Exception) {
                handleSearchError("Unexpected search error: ${e.message}")
            }
        }
    }

    private fun loadMoreSearchResults() {
        val currentState = _uiState.value
        val nextPageToken = currentState.searchNextPageToken

        if (nextPageToken.isNullOrBlank() || currentState.searchQuery.isBlank()) {
            return
        }

        _uiState.value = currentState.copy(isSearching = true)

        viewModelScope.launch {
            try {
                val result = videoSearchUseCase.loadMoreResults(
                    query = currentState.searchQuery,
                    nextPageToken = nextPageToken,
                    preferArchived = true,
                )

                result.fold(
                    onSuccess = { searchResponse ->
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            searchResults = currentState.searchResults + searchResponse.results,
                            searchNextPageToken = searchResponse.nextPageToken,
                            searchError = null,
                        )
                    },
                    onFailure = { error ->
                        handleSearchError("Failed to load more results: ${error.message}")
                    },
                )
            } catch (e: Exception) {
                handleSearchError("Unexpected error loading more results: ${e.message}")
            }
        }
    }

    private fun selectSearchResult(searchResult: SearchResult) {
        viewModelScope.launch {
            _sideEffect.emit(
                VideoSearchSideEffect.VideoSelected(
                    videoId = searchResult.videoId,
                    serviceType = VideoServiceType.YOUTUBE,
                ),
            )
        }
    }

    private fun clearSearchError() {
        _uiState.value = _uiState.value.copy(searchError = null)
    }

    private fun dismiss() {
        viewModelScope.launch {
            _sideEffect.emit(VideoSearchSideEffect.NavigateBack)
        }
    }

    private fun clearSearchResults() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            searchError = null,
            searchNextPageToken = null,
        )
    }

    private fun handleSearchError(errorMessage: String) {
        _uiState.value = _uiState.value.copy(
            isSearching = false,
            searchError = errorMessage,
        )

        viewModelScope.launch {
            _sideEffect.emit(VideoSearchSideEffect.ShowSearchError(errorMessage))
        }
    }
}
