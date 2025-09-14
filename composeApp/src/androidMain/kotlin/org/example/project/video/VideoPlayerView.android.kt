package org.example.project.video

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
actual fun VideoPlayerView(
    videoId: String,
    modifier: Modifier,
    onError: (String) -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var player by remember { mutableStateOf<YouTubePlayer?>(null) }

    if (videoId.isBlank()) {
        onError("Video ID cannot be empty")
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { context: android.content.Context ->
            YouTubePlayerView(context).apply {
                // Store reference for external access
                tag = "youtube_player_view"

                // Add YouTube Player listener
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        player = youTubePlayer
                        isLoading = false
                        // Load the video
                        youTubePlayer.loadVideo(videoId, 0f)
                    }

                    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                        when (state) {
                            PlayerConstants.PlayerState.BUFFERING -> {
                                isLoading = true
                            }
                            PlayerConstants.PlayerState.PLAYING,
                            PlayerConstants.PlayerState.PAUSED,
                            PlayerConstants.PlayerState.VIDEO_CUED,
                            -> {
                                isLoading = false
                            }
                            PlayerConstants.PlayerState.ENDED -> {
                                isLoading = false
                            }
                            PlayerConstants.PlayerState.UNSTARTED -> {
                                // Video not started yet
                            }
                            else -> {
                                isLoading = false
                            }
                        }
                    }

                    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                        isLoading = false
                        val errorMsg = when (error) {
                            PlayerConstants.PlayerError.INVALID_PARAMETER_IN_REQUEST -> "Invalid video ID: $videoId"
                            PlayerConstants.PlayerError.HTML_5_PLAYER -> "HTML5 player error"
                            PlayerConstants.PlayerError.VIDEO_NOT_FOUND -> "Video not found: $videoId"
                            PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER ->
                                "Video cannot be played in embedded player"
                            else -> "YouTube player error: $error"
                        }
                        onError(errorMsg)
                    }
                })
            }
        },
        update = { youTubePlayerView: YouTubePlayerView ->
            // Update video if videoId changes
            player?.loadVideo(videoId, 0f)
        },
    )

    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Extended version of VideoPlayerView that provides access to YouTubePlayer instance
 * for synchronization purposes.
 */
@Composable
fun VideoPlayerViewWithSync(
    videoId: String,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {},
    onPlayerReady: (YouTubePlayer?) -> Unit = {},
) {
    var isLoading by remember { mutableStateOf(true) }
    var player by remember { mutableStateOf<YouTubePlayer?>(null) }

    if (videoId.isBlank()) {
        onError("Video ID cannot be empty")
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { context: android.content.Context ->
            YouTubePlayerView(context).apply {
                // Store reference for external access
                tag = "youtube_player_view"

                // Add YouTube Player listener
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        player = youTubePlayer
                        isLoading = false
                        // Load the video
                        youTubePlayer.loadVideo(videoId, 0f)
                        // Notify that player is ready
                        onPlayerReady(youTubePlayer)
                    }

                    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                        when (state) {
                            PlayerConstants.PlayerState.BUFFERING -> {
                                isLoading = true
                            }
                            PlayerConstants.PlayerState.PLAYING,
                            PlayerConstants.PlayerState.PAUSED,
                            PlayerConstants.PlayerState.VIDEO_CUED,
                            -> {
                                isLoading = false
                            }
                            PlayerConstants.PlayerState.ENDED -> {
                                isLoading = false
                            }
                            PlayerConstants.PlayerState.UNSTARTED -> {
                                // Video not started yet
                            }
                            else -> {
                                isLoading = false
                            }
                        }
                    }

                    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                        isLoading = false
                        val errorMsg = when (error) {
                            PlayerConstants.PlayerError.INVALID_PARAMETER_IN_REQUEST -> "Invalid video ID: $videoId"
                            PlayerConstants.PlayerError.HTML_5_PLAYER -> "HTML5 player error"
                            PlayerConstants.PlayerError.VIDEO_NOT_FOUND -> "Video not found: $videoId"
                            PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER ->
                                "Video cannot be played in embedded player"
                            else -> "YouTube player error: $error"
                        }
                        onError(errorMsg)
                    }
                })
            }
        },
        update = { youTubePlayerView: YouTubePlayerView ->
            // Update video if videoId changes
            player?.loadVideo(videoId, 0f)
        },
    )

    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
