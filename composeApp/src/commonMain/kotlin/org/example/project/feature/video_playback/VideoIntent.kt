package org.example.project.feature.video_playback

import org.example.project.domain.model.VideoServiceType

/**
 * Sealed interface defining all possible user intents for video playback functionality.
 * Following MVI architecture pattern for state management.
 *
 * Note: Search intents have been moved to VideoSearchIntent.
 */
sealed interface VideoIntent {
    /**
     * Intent to load a video with the specified video ID (legacy)
     */
    data class LoadVideo(val videoId: String) : VideoIntent

    /**
     * Intent to load a video with the specified video ID and service type
     */
    data class LoadVideoWithService(val videoId: String, val serviceType: VideoServiceType) : VideoIntent

    /**
     * Intent to change the service type
     */
    data class ChangeServiceType(val serviceType: VideoServiceType) : VideoIntent

    /**
     * Intent to clear any error state
     */
    data object ClearError : VideoIntent

    /**
     * Intent to retry loading the current video
     */
    data object RetryLoad : VideoIntent

    /**
     * Intent to synchronize video playback position to absolute time
     */
    data class SyncToAbsoluteTime(val currentTime: Float) : VideoIntent

    /**
     * Intent to handle user-initiated seek to specific position
     */
    data class UserSeekToPosition(val position: Float) : VideoIntent

    /**
     * Intent to clear sync error state
     */
    data object ClearSyncError : VideoIntent
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
