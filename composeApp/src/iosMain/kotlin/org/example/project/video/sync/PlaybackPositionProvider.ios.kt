package org.example.project.video.sync

import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.WebKit.WKWebView

/**
 * iOS implementation of PlaybackPositionProvider using WKWebView JavaScript bridge.
 * Retrieves current playback position from YouTube Player API JavaScript.
 */
@OptIn(ExperimentalForeignApi::class)
class IOSPlaybackPositionProvider(
    private val webViewProvider: () -> WKWebView?,
) : PlaybackPositionProvider {
    /**
     * Retrieves the current playback position from the YouTube player via JavaScript bridge.
     *
     * @return Result containing current playback position in seconds, or failure if unavailable
     */
    override suspend fun getCurrentPlaybackPosition(): Result<Float> = suspendCancellableCoroutine { continuation ->
        try {
            val webView = webViewProvider()
            if (webView == null) {
                continuation.resume(Result.failure(IllegalStateException("WebView not initialized")))
                return@suspendCancellableCoroutine
            }

            // JavaScript to get current playback time from YouTube Player API
            // This corresponds to the improved JavaScript bridge in VideoPlayerViewWithSync
            val javascript = """
                (function() {
                    try {
                        // Try to get current time from the exposed getCurrentTime function
                        if (typeof getCurrentTime === 'function') {
                            return getCurrentTime();
                        }

                        // Fallback: Check if the global player variable exists
                        if (typeof player !== 'undefined' && player && typeof player.getCurrentTime === 'function') {
                            return player.getCurrentTime();
                        }

                        // Fallback: Try to access via YT API
                        if (typeof YT !== 'undefined' && YT.get) {
                            var ytPlayer = YT.get('player');
                            if (ytPlayer && typeof ytPlayer.getCurrentTime === 'function') {
                                return ytPlayer.getCurrentTime();
                            }
                        }

                        return -1; // Player not ready
                    } catch (e) {
                        console.log('Error getting current time:', e);
                        return -2; // Error occurred
                    }
                })();
            """.trimIndent()

            webView.evaluateJavaScript(javascript) { result, error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception("JavaScript execution failed: ${error.localizedDescription}")))
                } else if (result != null) {
                    when (val currentTime = result.toString().toFloatOrNull()) {
                        null -> continuation.resume(Result.failure(IllegalStateException("Invalid time format: $result")))
                        -1f -> continuation.resume(Result.failure(IllegalStateException("YouTube player not ready")))
                        -2f -> continuation.resume(Result.failure(IllegalStateException("JavaScript error occurred")))
                        else -> {
                            if (currentTime >= 0f) {
                                continuation.resume(Result.success(currentTime))
                            } else {
                                continuation.resume(
                                    Result.failure(IllegalStateException("Invalid playback time: $currentTime")),
                                )
                            }
                        }
                    }
                } else {
                    continuation.resume(Result.failure(IllegalStateException("No result returned from JavaScript")))
                }
            }
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual class PlaybackPositionProviderImpl(
    private val webViewProvider: () -> WKWebView?,
) : PlaybackPositionProvider {
    override suspend fun getCurrentPlaybackPosition(): Result<Float> = suspendCancellableCoroutine { continuation ->
        try {
            val webView = webViewProvider()
            if (webView == null) {
                continuation.resume(Result.failure(IllegalStateException("WebView not initialized")))
                return@suspendCancellableCoroutine
            }

            // JavaScript to get current playback time from YouTube Player API
            // This corresponds to the improved JavaScript bridge in VideoPlayerViewWithSync
            val javascript = """
                (function() {
                    try {
                        // Try to get current time from the exposed getCurrentTime function
                        if (typeof getCurrentTime === 'function') {
                            return getCurrentTime();
                        }

                        // Fallback: Check if the global player variable exists
                        if (typeof player !== 'undefined' && player && typeof player.getCurrentTime === 'function') {
                            return player.getCurrentTime();
                        }

                        // Fallback: Try to access via YT API
                        if (typeof YT !== 'undefined' && YT.get) {
                            var ytPlayer = YT.get('player');
                            if (ytPlayer && typeof ytPlayer.getCurrentTime === 'function') {
                                return ytPlayer.getCurrentTime();
                            }
                        }

                        return -1; // Player not ready
                    } catch (e) {
                        console.log('Error getting current time:', e);
                        return -2; // Error occurred
                    }
                })();
            """.trimIndent()

            webView.evaluateJavaScript(javascript) { result, error ->
                if (error != null) {
                    continuation.resume(Result.failure(Exception("JavaScript execution failed: ${error.localizedDescription}")))
                } else if (result != null) {
                    when (val currentTime = result.toString().toFloatOrNull()) {
                        null -> continuation.resume(Result.failure(IllegalStateException("Invalid time format: $result")))
                        -1f -> continuation.resume(Result.failure(IllegalStateException("YouTube player not ready")))
                        -2f -> continuation.resume(Result.failure(IllegalStateException("JavaScript error occurred")))
                        else -> {
                            if (currentTime >= 0f) {
                                continuation.resume(Result.success(currentTime))
                            } else {
                                continuation.resume(
                                    Result.failure(IllegalStateException("Invalid playback time: $currentTime")),
                                )
                            }
                        }
                    }
                } else {
                    continuation.resume(Result.failure(IllegalStateException("No result returned from JavaScript")))
                }
            }
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
}
