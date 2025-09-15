package org.example.project.video

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
import org.example.project.video.player.AndroidWebViewPlayerController
import org.example.project.video.player.YouTubeIframeTemplate.generateHtml
import org.example.project.video.ui.SyncControlsSection

/**
 * Android implementation of VideoPlayerView using WebView with YouTube iframe.
 * Uses WebView with JavaScript enabled to match iOS implementation design.
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
        YoutubePlayer(videoId, { webView = it })

        Spacer(modifier = Modifier.height(8.dp))
        SyncControlsSection(
            uiState = uiState,
            onSync = {
                controller.requestCurrentTime { currentTime ->
                    onIntent(VideoIntent.SyncToAbsoluteTime(currentTime))
                }
            },
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YoutubePlayer(
    youtubeVideoId: String,
    onChangeWebView: (WebView) -> Unit = {},
) {
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
                    "https://www.youtube.com",
                    generateHtml(youtubeVideoId),
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

        // updateブロックは再コンポーズ時に呼ばれる
        // 例えばvideoIdが変更されたときに動画を更新するなどの処理を記述できる
        update = { wv ->
            // videoIdが変更されたら新しい動画をロードするロジック
            wv.loadDataWithBaseURL(
                "https://www.youtube.com",
                generateHtml(youtubeVideoId),
                "text/html",
                "utf-8",
                null,
            )
        },
    )
}
