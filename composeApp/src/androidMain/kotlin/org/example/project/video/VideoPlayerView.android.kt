package org.example.project.video

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBarListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.example.project.video.ui.SyncControlsSection

@Composable
actual fun VideoPlayerView(
    videoId: String,
    uiState: VideoUiState,
    onIntent: (VideoIntent) -> Unit,
    modifier: Modifier,
    onError: (String) -> Unit,
) {
    var player by remember { mutableStateOf<YouTubePlayer?>(null) }
    var currentTime by remember { mutableFloatStateOf(0F) }

    val tracker = remember { YouTubePlayerTracker() }
    if (videoId.isBlank()) {
        onError("Video ID cannot be empty")
        return
    }

    Column {
        AndroidView(
            modifier = modifier,
            factory = { context: Context ->
                YouTubePlayerView(context).apply {
                    // Store reference for external access
                    tag = "youtube_player_view"

                    // MUST disable automatic initialization before manual initialization
                    enableAutomaticInitialization = false

                    // Create IFramePlayerOptions to completely disable default controls
                    val iFramePlayerOptions = com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions.Builder()
                        .controls(0) // Disable all default controls
                        .rel(0) // Don't show related videos
                        .ivLoadPolicy(3) // Don't load video thumbnail
                        .ccLoadPolicy(0) // Don't show captions button
                        .build()

                    // Initialize manually with custom options to hide default UI
                    initialize(
                        object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                player = youTubePlayer
                                youTubePlayer.addListener(tracker)

                                // Load the video
                                youTubePlayer.loadVideo(videoId, 0f)
                            }

                            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                                currentTime = tracker.currentSecond
                            }

                            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
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
                        },
                        true,
                        iFramePlayerOptions,
                    ) // Pass the options to disable controls
                }
            },
            update = { youTubePlayerView: YouTubePlayerView ->
                // Update video if videoId changes
                player?.loadVideo(videoId, 0f)
            },
        )

        // Custom seek bar (this will be the ONLY seek bar now)
        player?.let { ytPlayer ->
            YouTubePlayerSeekBarComponent(
                player = ytPlayer,
                onUserSeek = { seekTime ->
                    currentTime = seekTime
                },
            )
        }

        // Sync controls section
        Spacer(modifier = Modifier.height(16.dp))
        SyncControlsSection(
            uiState = uiState,
            onSync = {
                player?.pause()
                onIntent(VideoIntent.SyncToAbsoluteTime(currentTime))
            },
        )
    }
}

@Composable
fun YouTubePlayerSeekBarComponent(
    player: YouTubePlayer,
    onUserSeek: (Float) -> Unit,
) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context: Context ->
            YouTubePlayerSeekBar(context).apply {
                // Connect the seek bar to the YouTube player for time updates
                player.addListener(this)

                // Create custom listener for user seek operations
                val seekBarListener = object : YouTubePlayerSeekBarListener {
                    override fun seekTo(time: Float) {
                        // This method is called ONLY when user manually seeks
                        player.seekTo(time)
                        onUserSeek(time)
                    }
                }

                // Try to set the listener using reflection as fallback
                try {
                    val method = this.javaClass.getMethod(
                        "setYoutubePlayerSeekBarListener",
                        YouTubePlayerSeekBarListener::class.java,
                    )
                    method.invoke(this, seekBarListener)
                } catch (_: Exception) {
                    // Fallback: manually track seek bar changes
                    onUserSeek(0f) // Placeholder - in real scenario we'd need alternative approach
                }
            }
        },
    )
}
