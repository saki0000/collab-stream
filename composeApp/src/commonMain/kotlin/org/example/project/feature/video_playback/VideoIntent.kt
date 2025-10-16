package org.example.project.feature.video_playback

import org.example.project.domain.model.StreamInfo
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.video_playback.player.WebViewPlayerController

/**
 * Sealed interface defining all possible user intents for video playback functionality.
 * Following MVI architecture pattern for state management.
 *
 * Supports both legacy single video and new multi-stream functionality.
 */
sealed interface VideoIntent {
    // Legacy single video intents
    data class LoadVideo(val videoId: String) : VideoIntent
    data class LoadVideoWithService(val videoId: String, val serviceType: VideoServiceType) : VideoIntent
    data class ChangeServiceType(val serviceType: VideoServiceType) : VideoIntent
    data object ClearError : VideoIntent
    data object RetryLoad : VideoIntent

    // Multi-stream intents (new)
    data class LoadMainStream(val streamInfo: StreamInfo) : VideoIntent
    data class AddSubStream(val streamInfo: StreamInfo) : VideoIntent
    data class RemoveSubStream(val streamId: String) : VideoIntent
    data class SwitchMainSub(val subStreamId: String) : VideoIntent
    data object SyncAllStreams : VideoIntent

    // Sync intents
    data class SyncToAbsoluteTime(val currentTime: Float) : VideoIntent
    data class UserSeekToPosition(val position: Float) : VideoIntent
    data object ClearSyncError : VideoIntent

    // Player controller intent
    data class SetPlayerController(val controller: WebViewPlayerController) : VideoIntent
}

/**
 * Sealed interface defining side effects for one-time events.
 * Used for navigation, snackbars, and other one-time actions.
 *
 * Note: Search side effects have been moved to VideoSearchSideEffect.
 */
sealed interface VideoSideEffect {
    /**
     * Show an error message to the user
     */
    data class ShowError(val message: String) : VideoSideEffect

    /**
     * Show a success message when video loads successfully
     */
    data class ShowSuccess(val message: String) : VideoSideEffect

    /**
     * Show sync result to the user
     */
    data class ShowSyncResult(val absoluteTime: String) : VideoSideEffect

    /**
     * Show sync error message
     */
    data class ShowSyncError(val message: String) : VideoSideEffect
}
