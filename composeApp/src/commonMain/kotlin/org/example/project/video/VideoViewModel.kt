package org.example.project.video

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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.video.sync.VideoSyncController

/**
 * ViewModel for video functionality following MVI architecture pattern.
 * Manages video state and handles user intents.
 */
class VideoViewModel(
    private val videoSyncController: VideoSyncController? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<VideoSideEffect>()
    val sideEffect: SharedFlow<VideoSideEffect> = _sideEffect.asSharedFlow()

    /**
     * Handles user intents and updates state accordingly
     */
    fun handleIntent(intent: VideoIntent) {
        when (intent) {
            is VideoIntent.LoadVideo -> loadVideo(intent.videoId)
            VideoIntent.ClearError -> clearError()
            VideoIntent.RetryLoad -> retryLoad()
            VideoIntent.SyncToAbsoluteTime -> syncToAbsoluteTime()
            VideoIntent.ClearSyncError -> clearSyncError()
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

    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun retryLoad() {
        val currentVideoId = _uiState.value.videoId
        if (currentVideoId.isNotBlank()) {
            loadVideo(currentVideoId)
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
     */
    private fun syncToAbsoluteTime() {
        val currentVideoId = _uiState.value.videoId
        if (currentVideoId.isBlank()) {
            handleSyncError("No video loaded for synchronization")
            return
        }

        if (videoSyncController == null) {
            handleSyncError("Video sync functionality is not available")
            return
        }

        _uiState.value = _uiState.value.copy(
            isSyncing = true,
            syncError = null
        )

        viewModelScope.launch {
            try {
                val syncResult = videoSyncController.handleSyncButtonClick(currentVideoId)

                syncResult.fold(
                    onSuccess = { syncUiState ->
                        _uiState.value = _uiState.value.copy(
                            isSyncing = false,
                            syncResult = syncUiState,
                            syncError = null
                        )

                        _sideEffect.emit(
                            VideoSideEffect.ShowSyncResult(syncUiState.formattedAbsoluteTime)
                        )
                    },
                    onFailure = { error ->
                        handleSyncError("Sync failed: ${error.message}")
                    }
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
            syncError = errorMessage
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
}
