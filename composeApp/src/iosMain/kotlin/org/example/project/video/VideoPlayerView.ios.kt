package org.example.project.video

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import org.example.project.video.player.IOSWebViewPlayerController
import org.example.project.video.player.YouTubeIframeTemplate
import org.example.project.video.ui.SyncControlsSection
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

/**
 * Message handler for JavaScript to native communication in iOS WebView.
 * Implements WKScriptMessageHandlerProtocol to receive messages from JavaScript.
 */
class YouTubeMessageHandler(
    private val onMessageReceived: (message: WKScriptMessage) -> Unit,
) : NSObject(), WKScriptMessageHandlerProtocol {

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        onMessageReceived(didReceiveScriptMessage)
    }
}

/**
 * iOS implementation of VideoPlayerView using WKWebView with YouTube iframe.
 * Uses WKWebView with JavaScript enabled and media playback configuration.
 */
@OptIn(ExperimentalForeignApi::class)
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
    
    val controller = remember { IOSWebViewPlayerController() }

    Column(modifier = modifier) {
        var webView by remember { mutableStateOf<WKWebView?>(null) }

        // Set the WebView instance in the controller when available
        remember(webView, controller) {
            controller.setWebView(webView)
        }

        UIKitView(
            factory = {
                // Create message handler for JavaScript to native communication
                val messageHandler = YouTubeMessageHandler { message ->
                    val state = (message.body as? NSNumber)?.intValue()
                    state?.let {
                        println("iOS WebView state changed: $it")
                    }
                }

                // Configure WebView with message handler
                val userContentController = WKUserContentController()
                userContentController.addScriptMessageHandler(messageHandler, "nativeApp")

                val config = WKWebViewConfiguration().apply {
                    this.userContentController = userContentController
                    allowsInlineMediaPlayback = true
                    mediaTypesRequiringUserActionForPlayback = 0u
                }

                val webViewInstance = WKWebView(frame = cValue { CGRectZero }, configuration = config)
                webViewInstance.scrollView.setScrollEnabled(false)

                // Use shared template for HTML generation
                val html = YouTubeIframeTemplate.generateHtml(videoId)
                webViewInstance.loadHTMLString(html, baseURL = NSURL.URLWithString("https://www.youtube.com"))

                // Store WebView instance in state
                webView = webViewInstance

                webViewInstance
            },
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
            update = { webViewInstance ->
                // Use shared template for HTML generation on updates
                val html = YouTubeIframeTemplate.generateHtml(videoId)
                webViewInstance.loadHTMLString(html, baseURL = NSURL.URLWithString("https://www.youtube.com"))
            },
            onRelease = {},
            properties = UIKitInteropProperties(isInteractive = true, isNativeAccessibilityEnabled = true),
        )

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
