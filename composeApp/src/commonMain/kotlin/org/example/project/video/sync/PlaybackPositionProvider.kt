package org.example.project.video.sync

/**
 * Platform-specific provider for retrieving current video playback position.
 * This interface defines the contract for accessing playback position from
 * platform-specific video player implementations (YouTube Player on Android/iOS).
 */
interface PlaybackPositionProvider {
    /**
     * Retrieves the current playback position from the platform-specific video player.
     *
     * @return Result containing current playback position in seconds, or failure if unavailable
     */
    suspend fun getCurrentPlaybackPosition(): Result<Float>
}

/**
 * Platform-specific implementation typealias for PlaybackPositionProvider.
 * - Android: AndroidPlaybackPositionProvider (using YouTube Android Player API)
 * - iOS: IOSPlaybackPositionProvider (using WKWebView JavaScript bridge)
 */
expect class PlaybackPositionProviderImpl : PlaybackPositionProvider
