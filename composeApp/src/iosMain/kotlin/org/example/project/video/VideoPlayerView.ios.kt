package org.example.project.video

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
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
        onRelease = {},
        properties = UIKitInteropProperties(isInteractive = true, isNativeAccessibilityEnabled = true),
    )
}

/**
 * Extended version of VideoPlayerView that provides access to WKWebView instance
 * for synchronization purposes via JavaScript bridge.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun VideoPlayerViewWithSync(
    videoId: String,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {},
    onWebViewReady: (WKWebView?) -> Unit = {},
) {
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
                        <script src="https://www.youtube.com/iframe_api"></script>
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
                            #player {
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
                            <div id="player"></div>
                        </div>
                        <script>
                            var player;
                            
                            function onYouTubeIframeAPIReady() {
                                player = new YT.Player('player', {
                                    height: '100%',
                                    width: '100%',
                                    videoId: '$videoId',
                                    playerVars: {
                                        'playsinline': 1,
                                        'rel': 0,
                                        'modestbranding': 1,
                                        'controls': 1,
                                        'enablejsapi': 1
                                    },
                                    events: {
                                        'onReady': onPlayerReady,
                                        'onStateChange': onPlayerStateChange,
                                        'onError': onPlayerError
                                    }
                                });
                            }
                            
                            function onPlayerReady(event) {
                                console.log('YouTube Player ready');
                            }
                            
                            function onPlayerStateChange(event) {
                                console.log('YouTube Player state changed:', event.data);
                            }
                            
                            function onPlayerError(event) {
                                console.log('YouTube Player error:', event.data);
                            }
                            
                            // Function to get current time - exposed for native bridge
                            function getCurrentTime() {
                                if (player && player.getCurrentTime) {
                                    return player.getCurrentTime();
                                }
                                return -1;
                            }
                            
                            // Function to seek to specific time
                            function seekTo(seconds) {
                                if (player && player.seekTo) {
                                    player.seekTo(seconds, true);
                                }
                            }
                        </script>
                    </body>
                    </html>
            """.trimIndent()

            webView.loadHTMLString(html, baseURL = NSURL.URLWithString("https://www.youtube.com"))

            // Notify that WebView is ready
            onWebViewReady(webView)

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
                        <script src="https://www.youtube.com/iframe_api"></script>
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
                            #player {
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
                            <div id="player"></div>
                        </div>
                        <script>
                            var player;
                            
                            function onYouTubeIframeAPIReady() {
                                player = new YT.Player('player', {
                                    height: '100%',
                                    width: '100%',
                                    videoId: '$videoId',
                                    playerVars: {
                                        'playsinline': 1,
                                        'rel': 0,
                                        'modestbranding': 1,
                                        'controls': 1,
                                        'enablejsapi': 1
                                    },
                                    events: {
                                        'onReady': onPlayerReady,
                                        'onStateChange': onPlayerStateChange,
                                        'onError': onPlayerError
                                    }
                                });
                            }
                            
                            function onPlayerReady(event) {
                                console.log('YouTube Player ready');
                            }
                            
                            function onPlayerStateChange(event) {
                                console.log('YouTube Player state changed:', event.data);
                            }
                            
                            function onPlayerError(event) {
                                console.log('YouTube Player error:', event.data);
                            }
                            
                            // Function to get current time - exposed for native bridge
                            function getCurrentTime() {
                                if (player && player.getCurrentTime) {
                                    return player.getCurrentTime();
                                }
                                return -1;
                            }
                            
                            // Function to seek to specific time
                            function seekTo(seconds) {
                                if (player && player.seekTo) {
                                    player.seekTo(seconds, true);
                                }
                            }
                        </script>
                    </body>
                    </html>
            """.trimIndent()

            webView.loadHTMLString(html, baseURL = NSURL.URLWithString("https://www.youtube.com"))
        },
        onRelease = {},
        properties = UIKitInteropProperties(isInteractive = true, isNativeAccessibilityEnabled = true),
    )
}
