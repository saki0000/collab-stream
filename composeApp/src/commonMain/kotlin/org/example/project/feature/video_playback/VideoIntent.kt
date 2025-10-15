package org.example.project.feature.video_playback

import org.example.project.domain.model.StreamInfo
import org.example.project.domain.model.VideoServiceType

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

    // Multi-video sync intents

    /**
     * Intent to load main video (primary video for sync)
     */
    data class LoadMainVideo(val videoId: String, val serviceType: VideoServiceType) : VideoIntent

    /**
     * Intent to load sub video (secondary video to be synced)
     */
    data class LoadSubVideo(val videoId: String, val serviceType: VideoServiceType) : VideoIntent

    /**
     * Intent to synchronize main video's playback position to sub video
     */
    data object SyncMainToSub : VideoIntent

    /**
     * Intent to synchronize main video's playback position to sub video with explicit time
     */
    data class SyncMainToSubWithTime(val mainCurrentTime: Float) : VideoIntent

    /**
     * Intent to update main player's current time
     */
    data class UpdateMainPlayerTime(val currentTime: Float) : VideoIntent

    /**
     * Intent to update sub player's current time
     */
    data class UpdateSubPlayerTime(val currentTime: Float) : VideoIntent
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

    /**
     * Seek sub video to specific position (for sync operation)
     */
    data class SeekSubVideo(val seconds: Float) : VideoSideEffect

    /**
     * Request main player's current time (Container will handle this and send back SyncMainToSubWithTime)
     */
    data object RequestMainPlayerTime : VideoSideEffect
}
