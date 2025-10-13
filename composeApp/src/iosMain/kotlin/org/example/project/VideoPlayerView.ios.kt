package org.example.project

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
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.cValue
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoUiState
import org.example.project.feature.video_playback.player.IOSWebViewPlayerController
import org.example.project.feature.video_playback.player.TwitchIframeTemplate
import org.example.project.feature.video_playback.player.YouTubeIframeTemplate
import org.example.project.video.ui.SyncControlsSection
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

/**
 * Message handler for JavaScript to native communication in iOS WebView.
 * Implements WKScriptMessageHandlerProtocol to receive messages from JavaScript.
 * Supports both YouTube and Twitch iframe communication.
 */
class VideoMessageHandler(
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
 * Navigation delegate for debugging WebView loading issues (optional for troubleshooting)
 */
class VideoNavigationDelegate : NSObject(), WKNavigationDelegateProtocol {
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError,
    ) {
        println("iOS WebView provisional navigation failed: ${withError.localizedDescription} (Code: ${withError.code})")
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailNavigation: WKNavigation?,
        withError: NSError,
    ) {
        println("iOS WebView navigation failed: ${withError.localizedDescription} (Code: ${withError.code})")
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFinishNavigation: WKNavigation?,
    ) {
        println("iOS WebView navigation finished successfully")
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didStartProvisionalNavigation: WKNavigation?,
    ) {
        println("iOS WebView started provisional navigation")
    }
}

/**
 * iOS implementation of VideoPlayerView using WKWebView with iframe embedding.
 * Supports both YouTube and Twitch services with JavaScript enabled and media playback configuration.
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
                val messageHandler = VideoMessageHandler { message ->
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
                    allowsAirPlayForMediaPlayback = true
                    allowsPictureInPictureMediaPlayback = true
                    suppressesIncrementalRendering = false
                }

                val webViewInstance = WKWebView(frame = cValue { CGRectZero }, configuration = config)
                webViewInstance.scrollView.setScrollEnabled(false)

                // Add navigation delegate for debugging
                val navigationDelegate = VideoNavigationDelegate()
                webViewInstance.navigationDelegate = navigationDelegate

                // Generate HTML based on service type
                val (baseUrl, html) = when (uiState.serviceType) {
                    VideoServiceType.YOUTUBE -> {
                        "https://www.youtube.com" to YouTubeIframeTemplate.generateHtml(videoId)
                    }
                    VideoServiceType.TWITCH -> {
                        // iOS Twitch: Use shared template with parent/baseURL matching
                        val parentDomain = "org.example.project.CollabStream"
                        val baseURL = "https://$parentDomain"
                        baseURL to TwitchIframeTemplate.generateSimpleIframeHtml(videoId, parentDomain)
                    }
                }
                webViewInstance.loadHTMLString(html, baseURL = baseUrl?.let { NSURL.URLWithString(it) })

                // Store WebView instance in state
                webView = webViewInstance

                webViewInstance
            },
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
            update = { webViewInstance ->
                // Generate HTML based on service type for updates
                val (baseUrl, html) = when (uiState.serviceType) {
                    VideoServiceType.YOUTUBE -> {
                        "https://www.youtube.com" to YouTubeIframeTemplate.generateHtml(videoId)
                    }
                    VideoServiceType.TWITCH -> {
                        // iOS Twitch: Use shared template with parent/baseURL matching
                        val parentDomain = "org.example.project.CollabStream"
                        val baseURL = "https://$parentDomain"
                        baseURL to TwitchIframeTemplate.generateSimpleIframeHtml(videoId, parentDomain)
                    }
                }
                webViewInstance.loadHTMLString(html, baseURL = baseUrl?.let { NSURL.URLWithString(it) })
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
