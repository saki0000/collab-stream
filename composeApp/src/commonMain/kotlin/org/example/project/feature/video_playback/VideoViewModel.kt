package org.example.project.feature.video_playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
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
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.usecase.VideoSyncUseCase

/**
 * ViewModel for video playback functionality following MVI architecture pattern.
 * Manages video playback state and handles user intents.
 *
 * Note: Search functionality has been moved to VideoSearchViewModel
 * for better separation of concerns and adherence to ADR.
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
            is VideoIntent.LoadVideoWithService -> loadVideoWithService(intent.videoId, intent.serviceType)
            is VideoIntent.ChangeServiceType -> changeServiceType(intent.serviceType)
            VideoIntent.ClearError -> clearError()
            VideoIntent.RetryLoad -> retryLoad()
            is VideoIntent.SyncToAbsoluteTime -> syncToAbsoluteTime(intent.currentTime)
            is VideoIntent.UserSeekToPosition -> handleUserSeek(intent.position)
            VideoIntent.ClearSyncError -> clearSyncError()
            // Multi-video sync intents
            is VideoIntent.LoadMainVideo -> loadMainVideo(intent.videoId, intent.serviceType)
            is VideoIntent.LoadSubVideo -> loadSubVideo(intent.videoId, intent.serviceType)
            VideoIntent.SyncMainToSub -> syncMainToSub()
            is VideoIntent.SyncMainToSubWithTime -> syncMainToSubWithTime(intent.mainCurrentTime)
            is VideoIntent.UpdateMainPlayerTime -> updateMainPlayerTime(intent.currentTime)
            is VideoIntent.UpdateSubPlayerTime -> updateSubPlayerTime(intent.currentTime)
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
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.Companion.currentSystemDefault())
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
        val localDateTime = absoluteTime.toLocalDateTime(TimeZone.Companion.currentSystemDefault())
        return "${localDateTime.date} ${localDateTime.hour}:${localDateTime.minute.toString().padStart(
            2,
            '0',
        )}:${localDateTime.second.toString().padStart(2, '0')}"
    }

    // Multi-video sync methods

    /**
     * Loads main video (primary video for sync)
     */
    private fun loadMainVideo(videoId: String, serviceType: VideoServiceType) {
        if (videoId.isBlank()) {
            handleVideoError("Main video ID cannot be empty")
            return
        }

        _uiState.value = _uiState.value.copy(
            mainVideo = VideoPlayerInfo(
                videoId = videoId,
                serviceType = serviceType,
                playerState = _uiState.value.mainVideo.playerState,
                currentTime = 0f,
                isPlayerReady = true, // Temporarily set to true for sync button enablement
            ),
            // Also update legacy fields for backward compatibility
            videoId = videoId,
            serviceType = serviceType,
            isLoading = true,
            errorMessage = null,
        )

        viewModelScope.launch {
            val serviceName = when (serviceType) {
                VideoServiceType.YOUTUBE -> "YouTube"
                VideoServiceType.TWITCH -> "Twitch"
            }
            _sideEffect.emit(VideoSideEffect.ShowSuccess("Main video ($serviceName) loading started"))
        }
    }

    /**
     * Loads sub video (secondary video to be synced)
     */
    private fun loadSubVideo(videoId: String, serviceType: VideoServiceType) {
        if (videoId.isBlank()) {
            handleVideoError("Sub video ID cannot be empty")
            return
        }

        _uiState.value = _uiState.value.copy(
            subVideo = VideoPlayerInfo(
                videoId = videoId,
                serviceType = serviceType,
                playerState = _uiState.value.subVideo.playerState,
                currentTime = 0f,
                isPlayerReady = true, // Temporarily set to true for sync button enablement
            ),
        )

        viewModelScope.launch {
            val serviceName = when (serviceType) {
                VideoServiceType.YOUTUBE -> "YouTube"
                VideoServiceType.TWITCH -> "Twitch"
            }
            _sideEffect.emit(VideoSideEffect.ShowSuccess("Sub video ($serviceName) loading started"))
        }
    }

    /**
     * Updates main player's current time
     */
    private fun updateMainPlayerTime(currentTime: Float) {
        _uiState.value = _uiState.value.copy(
            mainVideo = _uiState.value.mainVideo.copy(currentTime = currentTime),
            // Also update legacy field
            currentTime = currentTime,
        )
    }

    /**
     * Updates sub player's current time
     */
    private fun updateSubPlayerTime(currentTime: Float) {
        _uiState.value = _uiState.value.copy(
            subVideo = _uiState.value.subVideo.copy(currentTime = currentTime),
        )
    }

    /**
     * Synchronizes main video's playback position to sub video
     * This method now requests the current time from Container layer via side effect
     */
    @OptIn(ExperimentalTime::class)
    private fun syncMainToSub() {
        val mainVideo = _uiState.value.mainVideo
        val subVideo = _uiState.value.subVideo

        // Validation checks
        if (mainVideo.videoId.isBlank()) {
            handleSyncError("No main video loaded for synchronization")
            return
        }

        if (subVideo.videoId.isBlank()) {
            handleSyncError("No sub video loaded for synchronization")
            return
        }

        if (!mainVideo.isPlayerReady || !subVideo.isPlayerReady) {
            handleSyncError("Both players must be ready before synchronization")
            return
        }

        // Request main player's current time from Container layer
        viewModelScope.launch {
            _sideEffect.emit(VideoSideEffect.RequestMainPlayerTime)
        }
    }

    /**
     * Synchronizes main video's playback position to sub video with explicit time
     * This version receives the current time from the container layer
     */
    @OptIn(ExperimentalTime::class)
    private fun syncMainToSubWithTime(mainCurrentTime: Float) {
        val mainVideo = _uiState.value.mainVideo
        val subVideo = _uiState.value.subVideo

        // Validation checks
        if (mainVideo.videoId.isBlank()) {
            handleSyncError("No main video loaded for synchronization")
            return
        }

        if (subVideo.videoId.isBlank()) {
            handleSyncError("No sub video loaded for synchronization")
            return
        }

        if (!mainVideo.isPlayerReady || !subVideo.isPlayerReady) {
            handleSyncError("Both players must be ready before synchronization")
            return
        }

        _uiState.value = _uiState.value.copy(
            isSyncing = true,
            syncError = null,
        )

        viewModelScope.launch {
            try {
                // 1. Get main video's absolute time using the provided current time
                val mainSyncResult = videoSyncUseCase.syncVideoToAbsoluteTime(
                    mainVideo.videoId,
                    mainCurrentTime,
                    mainVideo.serviceType,
                )

                mainSyncResult.fold(
                    onSuccess = { mainSyncInfo ->
                        // 2. Get sub video's start time
                        val subSyncResult = videoSyncUseCase.syncVideoToAbsoluteTime(
                            subVideo.videoId,
                            0f, // Get stream start time
                            subVideo.serviceType,
                        )

                        subSyncResult.fold(
                            onSuccess = { subSyncInfo ->
                                // 3. Calculate target position for sub video
                                val timeDiff = mainSyncInfo.absoluteTime - subSyncInfo.streamStartTime
                                val subTargetTime = timeDiff.inWholeSeconds.toFloat()

                                if (subTargetTime < 0) {
                                    handleSyncError("Cannot sync: main video time is before sub video start time")
                                    return@launch
                                }

                                _uiState.value = _uiState.value.copy(
                                    isSyncing = false,
                                    syncError = null,
                                )

                                // 4. Emit side effect to seek sub video
                                _sideEffect.emit(VideoSideEffect.SeekSubVideo(subTargetTime))

                                // 5. Show sync success message
                                val formattedTime = formatAbsoluteTime(mainSyncInfo.absoluteTime)
                                _sideEffect.emit(
                                    VideoSideEffect.ShowSyncResult("Synced to: $formattedTime"),
                                )
                            },
                            onFailure = { error ->
                                handleSyncError("Failed to get sub video time: ${error.message}")
                            },
                        )
                    },
                    onFailure = { error ->
                        handleSyncError("Failed to get main video time: ${error.message}")
                    },
                )
            } catch (e: Exception) {
                handleSyncError("Unexpected sync error: ${e.message}")
            }
        }
    }
}
