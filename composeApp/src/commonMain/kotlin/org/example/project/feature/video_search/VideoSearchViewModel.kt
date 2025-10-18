package org.example.project.feature.video_search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import org.example.project.domain.model.SearchOrder
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
            is VideoSearchIntent.UpdateInputText -> updateInputText(intent.text)
            VideoSearchIntent.ExecuteSearch -> executeSearch()
            is VideoSearchIntent.SearchVideos -> searchVideos(intent.query)
            VideoSearchIntent.LoadMoreSearchResults -> loadMoreSearchResults()
            is VideoSearchIntent.SelectSearchResult -> selectSearchResult(intent.searchResult)
            VideoSearchIntent.ClearSearchError -> clearSearchError()
            VideoSearchIntent.Dismiss -> dismiss()
            VideoSearchIntent.ClearSearchResults -> clearSearchResults()
            is VideoSearchIntent.ChangeSelectedDate -> changeSelectedDate(intent.date)
            is VideoSearchIntent.ChangeSearchMode -> changeSearchMode(intent.mode)
            is VideoSearchIntent.SelectService -> selectService(intent.service)
            is VideoSearchIntent.ToggleResultSelection -> toggleResultSelection(intent.result)
            VideoSearchIntent.ClearSelectedResults -> clearSelectedResults()
        }
    }

    private fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    private fun executeSearch() {
        val inputText = _uiState.value.inputText
        searchVideos(inputText)
    }

    @OptIn(ExperimentalTime::class)
    private fun searchVideos(query: String) {
        if (query.isBlank()) {
            handleSearchError("Search query cannot be empty")
            return
        }

        val currentState = _uiState.value

        // Calculate start and end of the selected date
        val startOfDay = currentState.selectedDate
            .atTime(0, 0, 0)
            .toInstant(TimeZone.currentSystemDefault())
        val endOfDay = currentState.selectedDate
            .atTime(23, 59, 59)
            .toInstant(TimeZone.currentSystemDefault())

        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearching = true,
            searchError = null,
        )

        viewModelScope.launch {
            try {
                // For Twitch, use channelId parameter; for YouTube, use query
                val channelId = if (currentState.selectedService == VideoServiceType.TWITCH) {
                    query
                } else {
                    null
                }

                val result = videoSearchUseCase.searchVideos(
                    query = query,
                    preferArchived = true,
                    publishedAfter = startOfDay,
                    publishedBefore = endOfDay,
                    order = SearchOrder.VIEW_COUNT,
                    targetServices = setOf(currentState.selectedService),
                    channelId = channelId,
                )

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

    @OptIn(ExperimentalTime::class)
    private fun loadMoreSearchResults() {
        val currentState = _uiState.value
        val nextPageToken = currentState.searchNextPageToken

        if (nextPageToken.isNullOrBlank() || currentState.searchQuery.isBlank()) {
            return
        }

        val startOfDay = currentState.selectedDate
            .atTime(0, 0, 0)
            .toInstant(TimeZone.currentSystemDefault())
        val endOfDay = currentState.selectedDate
            .atTime(23, 59, 59)
            .toInstant(TimeZone.currentSystemDefault())

        _uiState.value = currentState.copy(isSearching = true)

        viewModelScope.launch {
            try {
                // For Twitch, use channelId parameter; for YouTube, use query
                val channelId = if (currentState.selectedService == VideoServiceType.TWITCH) {
                    currentState.searchQuery
                } else {
                    null
                }

                val result = videoSearchUseCase.loadMoreResults(
                    query = currentState.searchQuery,
                    nextPageToken = nextPageToken,
                    preferArchived = true,
                    publishedAfter = startOfDay,
                    publishedBefore = endOfDay,
                    order = SearchOrder.VIEW_COUNT,
                    targetServices = setOf(currentState.selectedService),
                    channelId = channelId,
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
                    serviceType = searchResult.serviceType,
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
            inputText = "",
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

    private fun changeSelectedDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    private fun changeSearchMode(mode: SearchMode) {
        _uiState.value = _uiState.value.copy(searchMode = mode)
    }

    private fun selectService(service: VideoServiceType) {
        // When switching to Twitch, force search mode to CHANNEL_NAME
        val newSearchMode = when (service) {
            VideoServiceType.YOUTUBE -> _uiState.value.searchMode // Keep current mode for YouTube
            VideoServiceType.TWITCH -> SearchMode.CHANNEL_NAME // Force CHANNEL_NAME for Twitch
        }
        _uiState.value = _uiState.value.copy(
            selectedService = service,
            searchMode = newSearchMode,
        )
    }

    /**
     * Toggles the selection state of a search result (for multi-selection in sub search mode)
     */
    private fun toggleResultSelection(result: SearchResult) {
        val currentState = _uiState.value

        if (!currentState.isSubSearchMode) {
            // If not in sub search mode, behave like normal selection
            selectSearchResult(result)
            return
        }

        val selectedResults = currentState.selectedResults
        val isCurrentlySelected = selectedResults.any { it.videoId == result.videoId }

        val updatedSelection = if (isCurrentlySelected) {
            // Remove from selection
            selectedResults.filterNot { it.videoId == result.videoId }
        } else {
            // Add to selection
            selectedResults + result
        }

        _uiState.value = currentState.copy(selectedResults = updatedSelection)

        // In sub search mode, emit side effect immediately when adding
        if (!isCurrentlySelected) {
            viewModelScope.launch {
                _sideEffect.emit(
                    VideoSearchSideEffect.VideoSelected(
                        videoId = result.videoId,
                        serviceType = result.serviceType,
                    ),
                )
            }
        }
    }

    /**
     * Clears all selected results
     */
    private fun clearSelectedResults() {
        _uiState.value = _uiState.value.copy(selectedResults = emptyList())
    }

    /**
     * Sets the search mode (main or sub)
     */
    fun setSubSearchMode(isSubMode: Boolean) {
        _uiState.value = _uiState.value.copy(
            isSubSearchMode = isSubMode,
            selectedResults = emptyList(), // Clear selection when mode changes
        )
    }
}
