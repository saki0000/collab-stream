package org.example.project.video

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

/**
 * iOS implementation of VideoPlayerView using WKWebView with YouTube iframe.
 * Uses WKWebView with JavaScript enabled and media playback configuration.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayerView(
    videoId: String,
    modifier: Modifier,
    onError: (String) -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }

    if (videoId.isBlank()) {
        onError("Video ID cannot be empty")
        return
    }

    UIKitView(
        factory = {
            val config = WKWebViewConfiguration().apply {
                allowsInlineMediaPlayback = true
                mediaTypesRequiringUserActionForPlayback = 0u // Allow autoplay
            }

            val webView = WKWebView(frame = cValue { CGRectZero }, configuration = config)

            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                    <style>
                        body, html {
                            margin: 0;
                            padding: 0;
                            width: 100%;
                            height: 100%;
                            overflow: hidden;
                            background-color: black;
                        }
                        .video-container {
                            position: relative;
                            width: 100%;
                            height: 100%;
                        }
                        .video-container iframe {
                            position: absolute;
                            top: 0;
                            left: 0;
                            width: 100%;
                            height: 100%;
                            border: none;
                        }
                    </style>
                </head>
                <body>
                    <div class="video-container">
                        <iframe
                            src="https://www.youtube.com/embed/$videoId?enablejsapi=1&playsinline=1&rel=0&modestbranding=1&controls=1"
                            frameborder="0"
                            allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
                            allowfullscreen
                            webkitallowfullscreen
                            mozallowfullscreen>
                        </iframe>
                    </div>
                </body>
                </html>
            """.trimIndent()

            webView.loadHTMLString(html, baseURL = NSURL.URLWithString("https://www.youtube.com"))
            webView
        },
        modifier = modifier.fillMaxSize(),
        update = { webView ->
            // Update the webView if needed when videoId changes
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                    <style>
                        body, html {
                            margin: 0;
                            padding: 0;
                            width: 100%;
                            height: 100%;
                            overflow: hidden;
                            background-color: black;
                        }
                        .video-container {
                            position: relative;
                            width: 100%;
                            height: 100%;
                        }
                        .video-container iframe {
                            position: absolute;
                            top: 0;
                            left: 0;
                            width: 100%;
                            height: 100%;
                            border: none;
                        }
                    </style>
                </head>
                <body>
                    <div class="video-container">
                        <iframe
                            src="https://www.youtube.com/embed/$videoId?enablejsapi=1&playsinline=1&rel=0&modestbranding=1&controls=1"
                            frameborder="0"
                            allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
                            allowfullscreen
                            webkitallowfullscreen
                            mozallowfullscreen>
                        </iframe>
                    </div>
                </body>
                </html>
            """.trimIndent()

            webView.loadHTMLString(html, baseURL = NSURL.URLWithString("https://www.youtube.com"))
        },
    )

    if (isLoading) {
        CircularProgressIndicator()
    }
}
