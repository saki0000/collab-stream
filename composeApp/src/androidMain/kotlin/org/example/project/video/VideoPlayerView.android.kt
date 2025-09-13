package org.example.project.video

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URLEncoder

/**
 * Android implementation of VideoPlayerView using WebView with YouTube iframe.
 * Uses Android WebView with hardware acceleration and JavaScript enabled.
 */
@Composable
actual fun VideoPlayerView(
    videoId: String,
    modifier: Modifier,
    onError: (String) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    if (videoId.isBlank()) {
        onError("Video ID cannot be empty")
        return
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    mediaPlaybackRequiresUserGesture = false
                    setSupportZoom(true)
                    builtInZoomControls = false
                    displayZoomControls = false

                    // Enable hardware acceleration for better performance
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    cacheMode = WebSettings.LOAD_DEFAULT
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        if (newProgress == 100) {
                            isLoading = false
                        }
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        isLoading = false
                        onError("Failed to load video: $description")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isLoading = false
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { webView ->
            val encodedVideoId = URLEncoder.encode(videoId, "UTF-8")
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body, html {
                            margin: 0;
                            padding: 0;
                            width: 100%;
                            height: 100%;
                            overflow: hidden;
                        }
                        .video-container {
                            position: relative;
                            width: 100%;
                            height: 100%;
                            padding-bottom: 56.25%; /* 16:9 aspect ratio */
                        }
                        .video-container iframe {
                            position: absolute;
                            top: 0;
                            left: 0;
                            width: 100%;
                            height: 100%;
                        }
                    </style>
                </head>
                <body>
                    <div class="video-container">
                        <iframe
                            src="https://www.youtube.com/embed/$encodedVideoId?enablejsapi=1&playsinline=1&rel=0&modestbranding=1"
                            frameborder="0"
                            allow="autoplay; encrypted-media; picture-in-picture"
                            allowfullscreen>
                        </iframe>
                    </div>
                </body>
                </html>
            """.trimIndent()

            webView.loadDataWithBaseURL(
                "https://www.youtube.com",
                html,
                "text/html",
                "UTF-8",
                null
            )
        }
    )

    if (isLoading) {
        CircularProgressIndicator()
    }
}