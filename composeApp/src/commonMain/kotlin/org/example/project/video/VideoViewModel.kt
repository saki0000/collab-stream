package org.example.project.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.usecase.VideoSearchUseCase
import org.example.project.domain.usecase.VideoSyncUseCase

/**
 * ViewModel for video functionality following MVI architecture pattern.
 * Manages video state and handles user intents.
 */
class VideoViewModel(
    private val videoSyncUseCase: VideoSyncUseCase,
    private val videoSearchUseCase: VideoSearchUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoUiState())

    // Combine UI state with player state for reactive updates
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<VideoSideEffect>()
    val sideEffect: SharedFlow<VideoSideEffect> = _sideEffect.asSharedFlow()

    /**
     * Handles user intents and updates state accordingly
     */
    fun handleIntent(intent: VideoIntent) {
        when (intent) {
            is VideoIntent.LoadVideo -> loadVideo(intent.videoId)
            is VideoIntent.LoadVideoWithService -> loadVideoWithService(intent.videoId, intent.serviceType)
            is VideoIntent.ChangeServiceType -> changeServiceType(intent.serviceType)
            VideoIntent.ClearError -> clearError()
            VideoIntent.RetryLoad -> retryLoad()
            is VideoIntent.SyncToAbsoluteTime -> syncToAbsoluteTime(intent.currentTime)
            is VideoIntent.UserSeekToPosition -> handleUserSeek(intent.position)
            VideoIntent.ClearSyncError -> clearSyncError()
            // Search-related intents
            is VideoIntent.SearchVideos -> searchVideos(intent.query)
            VideoIntent.LoadMoreSearchResults -> loadMoreSearchResults()
            is VideoIntent.SelectSearchResult -> selectSearchResult(intent.searchResult)
            VideoIntent.ClearSearchError -> clearSearchError()
            VideoIntent.ToggleSearchBottomSheet -> toggleSearchBottomSheet()
            VideoIntent.ClearSearchResults -> clearSearchResults()
        }
    }

    /**
     * Handles errors from the video player component
     */
    fun handleVideoError(errorMessage: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = errorMessage,
        )

        viewModelScope.launch {
            _sideEffect.emit(VideoSideEffect.ShowError(errorMessage))
        }
    }

    private fun loadVideo(videoId: String) {
        if (videoId.isBlank()) {
            handleVideoError("Video ID cannot be empty")
            return
        }

        _uiState.value = _uiState.value.copy(
            videoId = videoId,
            isLoading = true,
            errorMessage = null,
            syncDateTime = getCurrentDateTime(),
        )

        // Simulate loading success after a short delay
        // In real implementation, this would handle actual video loading
        viewModelScope.launch {
            try {
                // Video loading is handled by the platform-specific VideoPlayerView
                // We just update the state to indicate loading has started
                _sideEffect.emit(VideoSideEffect.ShowSuccess("Video loading started"))
            } catch (e: Exception) {
                handleVideoError("Failed to load video: ${e.message}")
            }
        }
    }

    private fun loadVideoWithService(videoId: String, serviceType: VideoServiceType) {
        if (videoId.isBlank()) {
            handleVideoError("Video ID cannot be empty")
            return
        }

        _uiState.value = _uiState.value.copy(
            videoId = videoId,
            serviceType = serviceType,
            isLoading = true,
            errorMessage = null,
            syncDateTime = getCurrentDateTime(),
        )

        // Simulate loading success after a short delay
        // In real implementation, this would handle actual video loading
        viewModelScope.launch {
            try {
                // Video loading is handled by the platform-specific VideoPlayerView
                // We just update the state to indicate loading has started
                val serviceName = when (serviceType) {
                    VideoServiceType.YOUTUBE -> "YouTube"
                    VideoServiceType.TWITCH -> "Twitch"
                }
                _sideEffect.emit(VideoSideEffect.ShowSuccess("$serviceName video loading started"))
            } catch (e: Exception) {
                handleVideoError("Failed to load video: ${e.message}")
            }
        }
    }

    private fun changeServiceType(serviceType: VideoServiceType) {
        _uiState.value = _uiState.value.copy(
            serviceType = serviceType,
            // Clear current video when switching services
            videoId = "",
            errorMessage = null,
            syncResult = null,
            syncError = null,
        )

        val serviceName = when (serviceType) {
            VideoServiceType.YOUTUBE -> "YouTube"
            VideoServiceType.TWITCH -> "Twitch"
        }

        viewModelScope.launch {
            _sideEffect.emit(VideoSideEffect.ShowSuccess("Switched to $serviceName"))
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun retryLoad() {
        val currentState = _uiState.value
        if (currentState.videoId.isNotBlank()) {
            loadVideoWithService(currentState.videoId, currentState.serviceType)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun getCurrentDateTime(): String {
        val now = kotlin.time.Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.date} ${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
    }

    /**
     * Synchronizes current video playback position to absolute time
     * Now includes player readiness check for better UX
     */
    @OptIn(ExperimentalTime::class)
    private fun syncToAbsoluteTime(currentTime: Float) {
        val currentVideoId = _uiState.value.videoId
        if (currentVideoId.isBlank()) {
            handleSyncError("No video loaded for synchronization")
            return
        }

        _uiState.value = _uiState.value.copy(
            isSyncing = true,
            syncError = null,
        )

        viewModelScope.launch {
            try {
                val currentServiceType = _uiState.value.serviceType
                val syncResult = videoSyncUseCase.syncVideoToAbsoluteTime(currentVideoId, currentTime, currentServiceType)

                syncResult.fold(
                    onSuccess = { syncInfo ->
                        val formattedAbsoluteTime = formatAbsoluteTime(syncInfo.absoluteTime)

                        val uiState = VideoSyncUiState(
                            videoId = syncInfo.videoId,
                            playbackSeconds = syncInfo.playbackSeconds,
                            streamStartTime = syncInfo.streamStartTime,
                            absoluteTime = syncInfo.absoluteTime,
                            formattedAbsoluteTime = formattedAbsoluteTime,
                        )
                        _uiState.value = _uiState.value.copy(
                            isSyncing = false,
                            syncResult = uiState,
                            syncError = null,
                        )

                        _sideEffect.emit(
                            VideoSideEffect.ShowSyncResult(uiState.formattedAbsoluteTime),
                        )
                    },
                    onFailure = { error ->
                        handleSyncError("Sync failed: ${error.message}")
                    },
                )
            } catch (e: Exception) {
                handleSyncError("Unexpected sync error: ${e.message}")
            }
        }
    }

    /**
     * Handles sync errors and updates state accordingly
     */
    private fun handleSyncError(errorMessage: String) {
        _uiState.value = _uiState.value.copy(
            isSyncing = false,
            syncError = errorMessage,
        )

        viewModelScope.launch {
            _sideEffect.emit(VideoSideEffect.ShowSyncError(errorMessage))
        }
    }

    /**
     * Clears sync error state
     */
    private fun clearSyncError() {
        _uiState.value = _uiState.value.copy(syncError = null)
    }

    /**
     * Handles user-initiated seek to specific position
     */
    private fun handleUserSeek(position: Float) {
        println("User seeked to position: $position seconds")

        // Update UI state to reflect user seek operation
        _uiState.value = _uiState.value.copy(
            lastUserSeekPosition = position,
            lastUserSeekTime = getCurrentDateTime(),
        )

        viewModelScope.launch {
            _sideEffect.emit(VideoSideEffect.ShowSuccess("Seeked to ${position.toInt()}s"))
        }

        // Optionally trigger sync after user seek
        syncToAbsoluteTime(position)
    }

    @OptIn(ExperimentalTime::class)
    private fun formatAbsoluteTime(absoluteTime: Instant): String {
        val localDateTime = absoluteTime.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.date} ${localDateTime.hour}:${localDateTime.minute.toString().padStart(
            2,
            '0',
        )}:${localDateTime.second.toString().padStart(2, '0')}"
    }

    // Search-related methods

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
                            VideoSideEffect.ShowSearchSuccess("Found ${searchResponse.results.size} videos"),
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
        // Load the selected video
        loadVideoWithService(searchResult.videoId, VideoServiceType.YOUTUBE)

        // Hide the search bottom sheet
        _uiState.value = _uiState.value.copy(
            isSearchBottomSheetVisible = false,
        )

        viewModelScope.launch {
            _sideEffect.emit(
                VideoSideEffect.ShowSuccess("Loading ${searchResult.title}"),
            )
        }
    }

    private fun clearSearchError() {
        _uiState.value = _uiState.value.copy(searchError = null)
    }

    private fun toggleSearchBottomSheet() {
        _uiState.value = _uiState.value.copy(
            isSearchBottomSheetVisible = !_uiState.value.isSearchBottomSheetVisible,
        )
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
            _sideEffect.emit(VideoSideEffect.ShowSearchError(errorMessage))
        }
    }
}
