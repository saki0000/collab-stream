package org.example.project.video.player

import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Android implementation of WebViewPlayerController using Android WebView.
 * Provides JavaScript bridge communication for YouTube iframe control.
 */
class AndroidWebViewPlayerController : WebViewPlayerController {
    private var webView: WebView? = null

    override fun setWebView(webView: Any?) {
        this.webView = webView as? WebView
        // Add JavaScript interface for communication
        this.webView?.addJavascriptInterface(
            AndroidJavaScriptInterface(),
            "AndroidInterface",
        )
    }

    override fun requestCurrentTime(callback: (Float) -> Unit) {
        webView?.evaluateJavascript("getCurrentTime();") { result ->
            try {
                val time = result?.toFloatOrNull() ?: 0f
                callback(time)
            } catch (e: Exception) {
                println("Error getting current time: ${e.message}")
                callback(0f)
            }
        }
    }

    override fun seekTo(seconds: Float, callback: ((Boolean) -> Unit)?) {
        webView?.evaluateJavascript("seekTo($seconds);") { _ ->
            // WebView evaluateJavascript doesn't provide error feedback easily
            // Assume success unless we implement more complex error handling
            callback?.invoke(true)
        }
    }

    override fun getPlayerState(callback: (Int) -> Unit) {
        webView?.evaluateJavascript("getPlayerState();") { result ->
            try {
                val state = result?.toIntOrNull() ?: -1
                callback(state)
            } catch (e: Exception) {
                println("Error getting player state: ${e.message}")
                callback(-1)
            }
        }
    }

    override fun pauseVideo() {
        webView?.evaluateJavascript("pauseVideo();", null)
    }

    override fun playVideo() {
        webView?.evaluateJavascript("playVideo();", null)
    }

    /**
     * JavaScript interface for receiving messages from iframe
     */
    inner class AndroidJavaScriptInterface {
        @JavascriptInterface
        fun onMessage(message: String) {
            try {
                // Create a more lenient JSON instance
                val json = Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    isLenient = true
                }
                val messageData = json.decodeFromString<PlayerMessage>(message)
                handlePlayerMessage(messageData)
            } catch (e: Exception) {
                println("Error parsing message from JavaScript: $message, ${e.message}")
                // Try to handle the message manually
                handleMessageManually(message)
            }
        }

        private fun handleMessageManually(message: String) {
            try {
                // Manual parsing for simple JSON messages
                if (message.contains("\"type\":\"playerReady\"")) {
                    println("Android WebView: YouTube Player ready (manual parse)")
                } else if (message.contains("\"type\":\"stateChange\"")) {
                    println("Android WebView: Player state changed (manual parse)")
                } else if (message.contains("\"type\":\"playerError\"")) {
                    println("Android WebView: Player error (manual parse)")
                }
            } catch (e: Exception) {
                println("Manual message parsing also failed: ${e.message}")
            }
        }
    }

    private fun handlePlayerMessage(message: PlayerMessage) {
        when (message.type) {
            PlayerMessageType.PLAYER_READY -> {
                println("Android WebView: YouTube Player ready")
            }
            PlayerMessageType.STATE_CHANGE -> {
                println("Android WebView: Player state changed to ${message.data}")
            }
            PlayerMessageType.PLAYER_ERROR -> {
                println("Android WebView: Player error: ${message.data}")
            }
        }
    }
}

@Serializable
private data class PlayerMessage(
    val type: String,
    val data: String,
)
