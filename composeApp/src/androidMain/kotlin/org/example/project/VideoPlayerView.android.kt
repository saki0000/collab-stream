package org.example.project

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoUiState
import org.example.project.feature.video_playback.player.TwitchIframeTemplate
import org.example.project.feature.video_playback.player.YouTubeIframeTemplate
import org.example.project.video.player.AndroidWebViewPlayerController

/**
 * Android implementation of VideoPlayerView using WebView with iframe embedding.
 * Supports both YouTube and Twitch services with JavaScript enabled for interactive control.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun VideoPlayerView(
    videoId: String,
    uiState: VideoUiState,
    onIntent: (VideoIntent) -> Unit,
    modifier: Modifier,
    onError: (String) -> Unit,
) {
    if (videoId.isBlank()) {
        onError("Video ID cannot be empty")
        return
    }
    val controller = remember { AndroidWebViewPlayerController() }
    var webView by remember { mutableStateOf<WebView?>(null) }
    Column(modifier = Modifier) {
        // Set the WebView instance in the controller when available
        LaunchedEffect(webView, controller) {
            controller.setWebView(webView)
        }
        VideoPlayer(
            videoId = videoId,
            serviceType = uiState.serviceType,
            onChangeWebView = { webView = it },
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayer(
    videoId: String,
    serviceType: VideoServiceType,
    onChangeWebView: (WebView) -> Unit = {},
) {
    val (baseUrl, htmlContent) = when (serviceType) {
        VideoServiceType.YOUTUBE -> {
            "https://www.youtube.com" to YouTubeIframeTemplate.generateHtml(videoId)
        }
        VideoServiceType.TWITCH -> {
            val parentHost = "android.example.project"
            "https://$parentHost" to TwitchIframeTemplate.generateHtml(videoId, parentHost)
        }
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )

                settings.javaScriptEnabled = true
                webChromeClient = WebChromeClient()

                loadDataWithBaseURL(
                    baseUrl,
                    htmlContent,
                    "text/html",
                    "utf-8",
                    null,
                )

                onChangeWebView(this)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),

        // Update block is called on recomposition
        // For example, when videoId or serviceType changes, update the video
        update = { wv ->
            val (updatedBaseUrl, updatedHtmlContent) = when (serviceType) {
                VideoServiceType.YOUTUBE -> {
                    "https://www.youtube.com" to YouTubeIframeTemplate.generateHtml(videoId)
                }
                VideoServiceType.TWITCH -> {
                    val parentHost = "android.example.project"
                    "https://$parentHost" to TwitchIframeTemplate.generateHtml(videoId, parentHost)
                }
            }
            wv.loadDataWithBaseURL(
                updatedBaseUrl,
                updatedHtmlContent,
                "text/html",
                "utf-8",
                null,
            )
        },
    )
}
