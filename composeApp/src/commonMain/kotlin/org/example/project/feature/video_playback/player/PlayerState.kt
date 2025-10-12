package org.example.project.feature.video_playback.player

/**
 * Represents the current state of the video player across all platforms.
 * This state is managed by PlayerStateManager and observed by components that need
 * to react to player initialization and playback changes.
 */
sealed class PlayerState {
    /**
     * Player is not yet initialized or has been destroyed.
     */
    object NotInitialized : PlayerState()

    /**
     * Player is currently being initialized.
     */
    object Initializing : PlayerState()

    /**
     * Player is ready and can provide playback position.
     * @param currentTimeProvider Function that returns current playback time in seconds
     */
    data class Ready(
        val currentTimeProvider: () -> Float = { 0f },
    ) : PlayerState()

    /**
     * Player encountered an error during initialization or playback.
     * @param error The error that occurred
     */
    data class Error(
        val error: Throwable,
    ) : PlayerState()
}
