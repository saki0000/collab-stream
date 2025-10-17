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

    @OptIn(ExperimentalTime::class)
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
            // Legacy intents
            is VideoIntent.LoadVideo -> loadVideo(intent.videoId)
            is VideoIntent.LoadVideoWithService -> loadVideoWithService(intent.videoId, intent.serviceType)
            is VideoIntent.ChangeServiceType -> changeServiceType(intent.serviceType)
            VideoIntent.ClearError -> clearError()
            VideoIntent.RetryLoad -> retryLoad()

            // Multi-stream intents
            is VideoIntent.LoadMainStream -> loadMainStream(intent.streamInfo)
            is VideoIntent.AddSubStream -> addSubStream(intent.streamInfo)
            is VideoIntent.RemoveSubStream -> removeSubStream(intent.streamId)
            is VideoIntent.SwitchMainSub -> switchMainSub(intent.subStreamId)

            // Bottom Sheet intents
            is VideoIntent.ShowSwitchConfirmBottomSheet -> showSwitchConfirmBottomSheet(intent.streamInfo)
            is VideoIntent.ConfirmSwitchAndPlay -> confirmSwitchAndPlay(intent.syncPosition)
            VideoIntent.DismissSwitchBottomSheet -> dismissSwitchBottomSheet()

            // Sync intents
            is VideoIntent.SyncToAbsoluteTime -> syncToAbsoluteTime(intent.currentTime)
            is VideoIntent.UserSeekToPosition -> handleUserSeek(intent.position)
            VideoIntent.ClearSyncError -> clearSyncError()
            is VideoIntent.SyncAllStreams -> syncAllStreams(intent.currentPosition)
        }
    }

    /**
     * Handles errors from the video player component
     */
    @OptIn(ExperimentalTime::class)
    fun handleVideoError(errorMessage: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = errorMessage,
        )

        viewModelScope.launch {
            _sideEffect.emit(VideoSideEffect.ShowError(errorMessage))
        }
    }

    @OptIn(ExperimentalTime::class)
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

    @OptIn(ExperimentalTime::class)
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

    @OptIn(ExperimentalTime::class)
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

    @OptIn(ExperimentalTime::class)
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
    @OptIn(ExperimentalTime::class)
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
    @OptIn(ExperimentalTime::class)
    private fun clearSyncError() {
        _uiState.value = _uiState.value.copy(syncError = null)
    }

    /**
     * Handles user-initiated seek to specific position
     */
    @OptIn(ExperimentalTime::class)
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

    // Multi-stream management methods

    /**
     * Loads main stream from StreamInfo
     */
    @OptIn(ExperimentalTime::class)
    private fun loadMainStream(streamInfo: org.example.project.domain.model.StreamInfo) {
        _uiState.value = _uiState.value.copy(
            mainStream = streamInfo,
            videoId = streamInfo.streamId,
            serviceType = streamInfo.serviceType,
            isLoading = true,
            errorMessage = null,
            syncDateTime = getCurrentDateTime(),
        )

        viewModelScope.launch {
            try {
                val serviceName = when (streamInfo.serviceType) {
                    org.example.project.domain.model.VideoServiceType.YOUTUBE -> "YouTube"
                    org.example.project.domain.model.VideoServiceType.TWITCH -> "Twitch"
                }
                _sideEffect.emit(VideoSideEffect.ShowSuccess("$serviceName video loading started"))
            } catch (e: Exception) {
                handleVideoError("Failed to load video: ${e.message}")
            }
        }
    }

    /**
     * Adds a sub stream to the list
     */
    @OptIn(ExperimentalTime::class)
    private fun addSubStream(streamInfo: org.example.project.domain.model.StreamInfo) {
        val currentSubs = _uiState.value.subStreams

        // Check if already added
        if (currentSubs.any { it.streamId == streamInfo.streamId }) {
            viewModelScope.launch {
                _sideEffect.emit(VideoSideEffect.ShowError("Stream already added"))
            }
            return
        }

        _uiState.value = _uiState.value.copy(
            subStreams = currentSubs + streamInfo,
        )

        viewModelScope.launch {
            _sideEffect.emit(VideoSideEffect.ShowSuccess("Sub stream added"))
        }
    }

    /**
     * Removes a sub stream from the list
     */
    @OptIn(ExperimentalTime::class)
    private fun removeSubStream(streamId: String) {
        val currentSubs = _uiState.value.subStreams
        val updatedSubs = currentSubs.filterNot { it.streamId == streamId }

        _uiState.value = _uiState.value.copy(
            subStreams = updatedSubs,
        )

        viewModelScope.launch {
            _sideEffect.emit(VideoSideEffect.ShowSuccess("Sub stream removed"))
        }
    }

    /**
     * Switches main and sub streams
     */
    @OptIn(ExperimentalTime::class)
    private fun switchMainSub(subStreamId: String) {
        val currentMain = _uiState.value.mainStream
        val currentSubs = _uiState.value.subStreams

        val subToPromote = currentSubs.find { it.streamId == subStreamId }

        if (subToPromote == null) {
            viewModelScope.launch {
                _sideEffect.emit(VideoSideEffect.ShowError("Sub stream not found"))
            }
            return
        }

        // Swap: Sub becomes Main, old Main becomes Sub
        val updatedSubs = if (currentMain != null) {
            currentSubs.filterNot { it.streamId == subStreamId } + currentMain
        } else {
            currentSubs.filterNot { it.streamId == subStreamId }
        }

        _uiState.value = _uiState.value.copy(
            mainStream = subToPromote,
            videoId = subToPromote.streamId,
            serviceType = subToPromote.serviceType,
            subStreams = updatedSubs,
            syncDateTime = getCurrentDateTime(),
        )

        viewModelScope.launch {
            _sideEffect.emit(VideoSideEffect.ShowSuccess("Switched to ${subToPromote.channelName}"))
        }
    }

    /**
     * Syncs all sub streams to main stream's current time using absolute time calculation.
     * Receives current playback position from UI layer via intent.
     */
    @OptIn(ExperimentalTime::class)
    private fun syncAllStreams(currentPosition: Float) {
        val mainStream = _uiState.value.mainStream
        val subStreams = _uiState.value.subStreams

        if (mainStream == null) {
            viewModelScope.launch {
                _sideEffect.emit(VideoSideEffect.ShowError("No main stream loaded"))
            }
            return
        }

        if (subStreams.isEmpty()) {
            viewModelScope.launch {
                _sideEffect.emit(VideoSideEffect.ShowError("No sub streams to sync"))
            }
            return
        }

        _uiState.value = _uiState.value.copy(isSyncing = true)

        viewModelScope.launch {
            try {
                // 1. Calculate main stream's absolute time using provided playback position
                val mainSyncResult = videoSyncUseCase.syncVideoToAbsoluteTime(
                    mainStream.streamId,
                    currentPosition,
                    mainStream.serviceType,
                )

                performSync(mainSyncResult, subStreams, currentPosition)
            } catch (e: Exception) {
                handleSyncError("Failed to sync streams: ${e.message}")
            }
        }
    }

    /**
     * Performs the actual sync operation after getting playback position
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun performSync(
        mainSyncResult: Result<org.example.project.domain.model.VideoSyncInfo>,
        subStreams: List<org.example.project.domain.model.StreamInfo>,
        mainTime: Float,
    ) {
        mainSyncResult.fold(
            onSuccess = { mainSyncInfo ->
                val mainAbsoluteTime = mainSyncInfo.absoluteTime

                // 2. Calculate target seek positions for each sub stream
                val syncedSubs = subStreams.map { subStream ->
                    val subSyncResult = videoSyncUseCase.syncVideoToAbsoluteTime(
                        subStream.streamId,
                        0f, // We'll calculate the actual position based on absolute time
                        subStream.serviceType,
                    )

                    subSyncResult.fold(
                        onSuccess = { subSyncInfo ->
                            // Calculate how many seconds elapsed from sub stream start to main's absolute time
                            val elapsedSeconds =
                                (mainAbsoluteTime - subSyncInfo.streamStartTime).inWholeSeconds.toFloat()

                            if (elapsedSeconds < 0) {
                                // Sub stream hasn't started yet when main is at current time
                                subStream.copy(
                                    targetSeekPosition = 0f,
                                    syncedAbsoluteTime = mainAbsoluteTime,
                                    isSynced = false, // Can't sync to future
                                )
                            } else {
                                // Normal case: seek to calculated position
                                subStream.copy(
                                    targetSeekPosition = elapsedSeconds,
                                    syncedAbsoluteTime = mainAbsoluteTime,
                                    isSynced = true,
                                )
                            }
                        },
                        onFailure = {
                            // Keep original if sync fails for this stream
                            subStream.copy(isSynced = false)
                        },
                    )
                }

                _uiState.value = _uiState.value.copy(
                    subStreams = syncedSubs,
                    mainAbsoluteTime = mainAbsoluteTime,
                    currentTime = mainTime, // Update currentTime with actual playback position
                    isSyncing = false,
                )

                val syncedCount = syncedSubs.count { it.isSynced }
                _sideEffect.emit(
                    VideoSideEffect.ShowSuccess(
                        "$syncedCount/${syncedSubs.size} streams synced to ${formatAbsoluteTime(mainAbsoluteTime)}",
                    ),
                )
            },
            onFailure = { error ->
                handleSyncError("Failed to calculate main stream time: ${error.message}")
            },
        )
    }

    // Bottom Sheet methods

    /**
     * Shows the switch confirmation bottom sheet with the selected stream
     */
    @OptIn(ExperimentalTime::class)
    private fun showSwitchConfirmBottomSheet(streamInfo: org.example.project.domain.model.StreamInfo) {
        _uiState.value = _uiState.value.copy(
            showSwitchConfirmBottomSheet = true,
            streamToSwitch = streamInfo,
        )
    }

    /**
     * Confirms the switch and plays the stream at the synced position
     */
    @OptIn(ExperimentalTime::class)
    private fun confirmSwitchAndPlay(syncPosition: Float?) {
        val streamToSwitch = _uiState.value.streamToSwitch

        if (streamToSwitch == null) {
            viewModelScope.launch {
                _sideEffect.emit(VideoSideEffect.ShowError("No stream to switch"))
            }
            dismissSwitchBottomSheet()
            return
        }

        val currentMain = _uiState.value.mainStream
        val currentSubs = _uiState.value.subStreams

        // Swap: Sub becomes Main, old Main becomes Sub
        val updatedSubs = if (currentMain != null) {
            currentSubs.filterNot { it.streamId == streamToSwitch.streamId } + currentMain
        } else {
            currentSubs.filterNot { it.streamId == streamToSwitch.streamId }
        }

        // Update the streamToSwitch with the sync position if provided
        val updatedStreamToSwitch = if (syncPosition != null) {
            streamToSwitch.copy(
                targetSeekPosition = syncPosition,
                isSynced = true,
            )
        } else {
            streamToSwitch
        }

        _uiState.value = _uiState.value.copy(
            mainStream = updatedStreamToSwitch,
            videoId = updatedStreamToSwitch.streamId,
            serviceType = updatedStreamToSwitch.serviceType,
            subStreams = updatedSubs,
            syncDateTime = getCurrentDateTime(),
            showSwitchConfirmBottomSheet = false,
            streamToSwitch = null,
        )

        viewModelScope.launch {
            _sideEffect.emit(VideoSideEffect.ShowSuccess("Switched to ${updatedStreamToSwitch.channelName}"))
        }
    }

    /**
     * Dismisses the switch confirmation bottom sheet
     */
    @OptIn(ExperimentalTime::class)
    private fun dismissSwitchBottomSheet() {
        _uiState.value = _uiState.value.copy(
            showSwitchConfirmBottomSheet = false,
            streamToSwitch = null,
        )
    }
}
