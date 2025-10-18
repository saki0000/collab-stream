package org.example.project.feature.streamer_search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SearchOrder
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.usecase.ChannelSearchUseCase
import org.example.project.domain.usecase.VideoSearchUseCase
import org.example.project.feature.streamer_search.SearchMode

/**
 * ViewModel for streamer search (Main or Sub)
 */
class StreamerSearchViewModel(
    private val videoSearchUseCase: VideoSearchUseCase,
    private val channelSearchUseCase: ChannelSearchUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val searchMode: String = savedStateHandle.get<String>("searchMode") ?: "MAIN"
    private var channelSearchJob: Job? = null

    private val _uiState = MutableStateFlow(
        StreamerSearchUiState(
            searchMode = searchMode,
            channelSearchMode = SearchMode.CHANNEL_NAME, // Default to channel name search
        ),
    )
    val uiState: StateFlow<StreamerSearchUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<StreamerSearchSideEffect>()
    val sideEffect: SharedFlow<StreamerSearchSideEffect> = _sideEffect.asSharedFlow()

    fun handleIntent(intent: StreamerSearchIntent) {
        when (intent) {
            is StreamerSearchIntent.UpdateInputText -> updateInputText(intent.text)
            StreamerSearchIntent.ExecuteSearch -> executeSearch()
            is StreamerSearchIntent.SelectSearchResult -> selectSearchResult(intent.result)
            StreamerSearchIntent.LoadMoreSearchResults -> loadMoreSearchResults()
            StreamerSearchIntent.ClearSearchError -> clearSearchError()
            is StreamerSearchIntent.ChangeSelectedDate -> changeSelectedDate(intent.date)
            is StreamerSearchIntent.ChangeSearchMode -> changeSearchMode(intent.mode)
            is StreamerSearchIntent.SelectService -> selectService(intent.service)
            is StreamerSearchIntent.SearchChannels -> searchChannelsDebounced(intent.query)
            is StreamerSearchIntent.SelectChannel -> selectChannel(intent.channel)
            is StreamerSearchIntent.ToggleResultSelection -> toggleResultSelection(intent.result)
            StreamerSearchIntent.ClearSelectedResults -> clearSelectedResults()
        }
    }

    private fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    private fun executeSearch() {
        val inputText = _uiState.value.inputText
        searchStreamers(inputText)
    }

    @OptIn(ExperimentalTime::class)
    private fun searchStreamers(query: String) {
        if (query.isBlank()) {
            handleSearchError("Search query cannot be empty")
            return
        }

        val currentState = _uiState.value

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

    private fun selectSearchResult(result: SearchResult) {
        viewModelScope.launch {
            _sideEffect.emit(
                StreamerSearchSideEffect.StreamerSelected(
                    searchResult = result,
                    serviceType = _uiState.value.selectedService,
                ),
            )
        }
    }

    private fun clearSearchError() {
        _uiState.value = _uiState.value.copy(searchError = null)
    }

    private fun handleSearchError(errorMessage: String) {
        _uiState.value = _uiState.value.copy(
            isSearching = false,
            searchError = errorMessage,
        )
    }

    private fun changeSelectedDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    private fun changeSearchMode(mode: SearchMode) {
        _uiState.value = _uiState.value.copy(channelSearchMode = mode)
    }

    private fun selectService(service: VideoServiceType) {
        val newSearchMode = when (service) {
            VideoServiceType.YOUTUBE -> _uiState.value.channelSearchMode
            VideoServiceType.TWITCH -> SearchMode.CHANNEL_NAME
        }
        _uiState.value = _uiState.value.copy(
            selectedService = service,
            channelSearchMode = newSearchMode,
            channelSuggestions = emptyList(),
            selectedChannel = null,
        )
    }

    /**
     * Search for channels with debouncing (500ms delay).
     * Used for real-time channel suggestions as the user types.
     */
    private fun searchChannelsDebounced(query: String) {
        // Cancel previous search job
        channelSearchJob?.cancel()

        // Clear suggestions if query is empty
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                channelSuggestions = emptyList(),
                isSearchingChannels = false,
            )
            return
        }

        // Only search for Twitch channels
        if (_uiState.value.selectedService != VideoServiceType.TWITCH) {
            return
        }

        // Launch new debounced search
        channelSearchJob = viewModelScope.launch {
            delay(500) // Debounce delay
            searchChannels(query)
        }
    }

    /**
     * Execute channel search immediately.
     */
    private fun searchChannels(query: String) {
        _uiState.value = _uiState.value.copy(
            isSearchingChannels = true,
        )

        viewModelScope.launch {
            try {
                val result = channelSearchUseCase.searchTwitchChannels(
                    query = query,
                    maxResults = 5,
                )

                result.fold(
                    onSuccess = { channels ->
                        _uiState.value = _uiState.value.copy(
                            channelSuggestions = channels,
                            isSearchingChannels = false,
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            channelSuggestions = emptyList(),
                            isSearchingChannels = false,
                        )
                    },
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    channelSuggestions = emptyList(),
                    isSearchingChannels = false,
                )
            }
        }
    }

    /**
     * Select a channel from the suggestions and immediately search for videos.
     */
    private fun selectChannel(channel: ChannelInfo) {
        _uiState.value = _uiState.value.copy(
            selectedChannel = channel,
            inputText = channel.displayName,
            channelSuggestions = emptyList(), // Close dropdown
        )

        // Immediately search for videos for this channel
        searchStreamers(channel.displayName)
    }

    /**
     * Toggles the selection state of a search result (for multi-selection in SUB mode)
     */
    private fun toggleResultSelection(result: SearchResult) {
        val currentState = _uiState.value

        if (currentState.searchMode != "SUB") {
            // If not in SUB mode, behave like normal selection
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

        // In SUB mode, emit side effect immediately when adding
        if (!isCurrentlySelected) {
            viewModelScope.launch {
                _sideEffect.emit(
                    StreamerSearchSideEffect.StreamerSelected(
                        searchResult = result,
                        serviceType = currentState.selectedService,
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
}

/**
 * Side effects for streamer search
 */
sealed interface StreamerSearchSideEffect {
    data class StreamerSelected(
        val searchResult: SearchResult,
        val serviceType: VideoServiceType,
    ) : StreamerSearchSideEffect
}
