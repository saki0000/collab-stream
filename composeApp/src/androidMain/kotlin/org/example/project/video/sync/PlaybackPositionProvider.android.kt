package org.example.project.video.sync

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Android implementation of PlaybackPositionProvider using YouTube Android Player API.
 * Retrieves current playback position from YouTubePlayer instance.
 *
 * Note: The Android YouTube Player API doesn't provide direct access to current time.
 * This implementation returns a placeholder value. In a real implementation, you would
 * need to track the current time using onCurrentSecond() listener in the player setup.
 */
class AndroidPlaybackPositionProvider(
    private val youTubePlayerProvider: () -> YouTubePlayer?,
    private val currentTimeProvider: () -> Float = { 0f }, // Provider for current time tracking
) : PlaybackPositionProvider {
    /**
     * Retrieves the current playback position from the YouTube player.
     *
     * @return Result containing current playback position in seconds, or failure if unavailable
     */
    override suspend fun getCurrentPlaybackPosition(): Result<Float> = suspendCancellableCoroutine { continuation ->
        try {
            val player = youTubePlayerProvider()
            if (player == null) {
                continuation.resume(Result.failure(IllegalStateException("YouTube player not initialized")))
                return@suspendCancellableCoroutine
            }

            // Get current time from provider (tracked via onCurrentSecond listener)
            val currentTime = currentTimeProvider()
            continuation.resume(Result.success(currentTime))
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
}

actual class PlaybackPositionProviderImpl(
    private val youTubePlayerProvider: () -> YouTubePlayer?,
    private val currentTimeProvider: () -> Float = { 0f }, // Provider for current time tracking
) : PlaybackPositionProvider {
    override suspend fun getCurrentPlaybackPosition(): Result<Float> = suspendCancellableCoroutine { continuation ->
        try {
            val player = youTubePlayerProvider()
            if (player == null) {
                continuation.resume(Result.failure(IllegalStateException("YouTube player not initialized")))
                return@suspendCancellableCoroutine
            }

            // Get current time from provider (tracked via onCurrentSecond listener)
            // Note: In a real implementation, this should be connected to onCurrentSecond()
            // callback in the VideoPlayerView setup to track the actual current time
            val currentTime = currentTimeProvider()
            continuation.resume(Result.success(currentTime))
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
}
