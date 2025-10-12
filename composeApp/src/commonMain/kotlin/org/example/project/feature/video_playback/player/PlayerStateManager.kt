package org.example.project.feature.video_playback.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the state of the video player across all platforms.
 * Provides a reactive way to track player initialization and playback position.
 * This replaces the complex dependency injection pattern with a simpler state management approach.
 */
class PlayerStateManager {
    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.NotInitialized)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    /**
     * Updates the player state when player becomes ready.
     * Called by platform-specific VideoPlayerView implementations when onReady is triggered.
     *
     * @param currentTimeProvider Function that returns current playback time in seconds
     */
    fun setPlayerReady(currentTimeProvider: () -> Float) {
        _playerState.value = PlayerState.Ready(currentTimeProvider)
    }

    /**
     * Updates the player state to initializing.
     * Called when player initialization starts.
     */
    fun setPlayerInitializing() {
        _playerState.value = PlayerState.Initializing
    }

    /**
     * Updates the player state when an error occurs.
     *
     * @param error The error that occurred
     */
    fun setPlayerError(error: Throwable) {
        _playerState.value = PlayerState.Error(error)
    }

    /**
     * Resets player state to not initialized.
     * Called when player is destroyed or video changes.
     */
    fun resetPlayer() {
        _playerState.value = PlayerState.NotInitialized
    }

    /**
     * Gets the current playback position if player is ready.
     * This method provides a clean interface for getting playback position
     * without the complex provider pattern.
     *
     * @return Result containing current playback position in seconds, or failure if player not ready
     */
    suspend fun getCurrentPlaybackPosition(): Result<Float> {
        return when (val state = _playerState.value) {
            is PlayerState.Ready -> {
                try {
                    val currentTime = state.currentTimeProvider()
                    Result.success(currentTime)
                } catch (e: Exception) {
                    Result.failure(Exception("Failed to get playback position: ${e.message}", e))
                }
            }
            PlayerState.NotInitialized -> {
                Result.failure(IllegalStateException("YouTube player not initialized"))
            }
            PlayerState.Initializing -> {
                Result.failure(IllegalStateException("YouTube player is initializing"))
            }
            is PlayerState.Error -> {
                Result.failure(state.error)
            }
        }
    }

    /**
     * Checks if the player is ready for sync operations.
     *
     * @return true if player is ready, false otherwise
     */
    fun isPlayerReady(): Boolean {
        return _playerState.value is PlayerState.Ready
    }
}