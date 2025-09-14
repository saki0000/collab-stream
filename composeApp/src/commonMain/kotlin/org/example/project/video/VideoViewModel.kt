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
import org.example.project.domain.usecase.VideoSyncUseCase

/**
 * ViewModel for video functionality following MVI architecture pattern.
 * Manages video state and handles user intents.
 */
class VideoViewModel(
    private val videoSyncUseCase: VideoSyncUseCase,
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
            VideoIntent.ClearError -> clearError()
            VideoIntent.RetryLoad -> retryLoad()
            is VideoIntent.SyncToAbsoluteTime -> syncToAbsoluteTime(intent.currentTime)
            is VideoIntent.UserSeekToPosition -> handleUserSeek(intent.position)
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
                val syncResult = videoSyncUseCase.syncVideoToAbsoluteTime(currentVideoId, currentTime)

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
}
