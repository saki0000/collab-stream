package org.example.project.feature.video_playback.player

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNumber
import platform.WebKit.WKWebView

/**
 * iOS implementation of WebViewPlayerController using WKWebView.
 * Provides JavaScript bridge communication for YouTube iframe control.
 */
@OptIn(ExperimentalForeignApi::class)
class IOSWebViewPlayerController : WebViewPlayerController {
    private var webView: WKWebView? = null

    override fun setWebView(webView: Any?) {
        this.webView = webView as? WKWebView
    }

    override fun requestCurrentTime(callback: (Float) -> Unit) {
        webView?.evaluateJavaScript("getCurrentTime();") { result, error ->
            if (error == null && result != null) {
                val time = (result as? NSNumber)?.floatValue() ?: 0f
                callback(time)
            } else {
                println("Error getting current time: ${error?.localizedDescription}")
                callback(0f)
            }
        }
    }

    override fun seekTo(seconds: Float, callback: ((Boolean) -> Unit)?) {
        webView?.evaluateJavaScript("seekTo($seconds);") { result, error ->
            val success = error == null
            callback?.invoke(success)
            if (!success) {
                println("Error seeking to $seconds: ${error?.localizedDescription}")
            }
        }
    }

    override fun getPlayerState(callback: (Int) -> Unit) {
        webView?.evaluateJavaScript("getPlayerState();") { result, error ->
            if (error == null && result != null) {
                val state = (result as? NSNumber)?.intValue() ?: -1
                callback(state)
            } else {
                println("Error getting player state: ${error?.localizedDescription}")
                callback(-1)
            }
        }
    }

    override fun pauseVideo() {
        webView?.evaluateJavaScript("pauseVideo();", null)
    }

    override fun playVideo() {
        webView?.evaluateJavaScript("playVideo();", null)
    }
}