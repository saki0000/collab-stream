package org.example.project.video.player

/**
 * Common interface for WebView-based YouTube player control.
 * Provides platform-agnostic API for video player operations.
 */
interface WebViewPlayerController {

    /**
     * Set the WebView instance for communication
     */
    fun setWebView(webView: Any?)

    /**
     * Request current playback time from YouTube player
     * @param callback Function to receive the current time in seconds
     */
    fun requestCurrentTime(callback: (Float) -> Unit)

    /**
     * Seek to specific time in the video
     * @param seconds Target time in seconds
     * @param callback Optional callback to receive success/failure result
     */
    fun seekTo(seconds: Float, callback: ((Boolean) -> Unit)? = null)

    /**
     * Get current player state
     * @param callback Function to receive the player state
     */
    fun getPlayerState(callback: (Int) -> Unit)

    /**
     * Pause the video
     */
    fun pauseVideo()

    /**
     * Play the video
     */
    fun playVideo()
}

/**
 * Message types for JavaScript to native communication
 */
object PlayerMessageType {
    const val PLAYER_READY = "playerReady"
    const val STATE_CHANGE = "stateChange"
    const val PLAYER_ERROR = "playerError"
}

/**
 * YouTube player states (matching YouTube iframe API constants)
 */
object YouTubePlayerState {
    const val UNSTARTED = -1
    const val ENDED = 0
    const val PLAYING = 1
    const val PAUSED = 2
    const val BUFFERING = 3
    const val CUED = 5
}
