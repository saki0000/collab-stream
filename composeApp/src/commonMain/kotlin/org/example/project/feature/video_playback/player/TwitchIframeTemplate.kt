package org.example.project.feature.video_playback.player

/**
 * Twitch iframe HTML templates for different platform implementations.
 * Provides both JavaScript API and simple iframe solutions.
 */
object TwitchIframeTemplate {
    /**
     * Android implementation using JavaScript API for full control
     */
    fun generateHtml(videoId: String, parentHost: String): String {
        // Ensure video ID has the 'v' prefix required by Twitch
        val formattedVideoId = if (videoId.startsWith("v")) videoId else "v$videoId"

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body, html, #player {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    background-color: black;
                    overflow: hidden;
                }
            </style>
        </head>
        <body>
            <div id="player"></div>

            <script src="https://player.twitch.tv/js/embed/v1.js"></script>
            <script>
                var player;

                // Initialize Twitch player
                function initializeTwitchPlayer() {
                    try {

                        player = new Twitch.Player('player', {
                        width: '100%',
                        height: '100%',
                        video: '$formattedVideoId',
                        parent: ['$parentHost'],
                        autoplay: false,
                        muted: false,
                        playsinline: true,  // Enable inline playback for iOS
                        allowfullscreen: true,
                        layout: "video"
                    });

                    // Player ready event
                    player.addEventListener(Twitch.Player.READY, function() {
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "playerReady", data: "ready"}));
                        }
                    });

                    // Player state change events
                    player.addEventListener(Twitch.Player.PLAY, function() {
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "stateChange", data: "playing"}));
                        }
                    });

                    player.addEventListener(Twitch.Player.PAUSE, function() {
                        // Send message to Android
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "stateChange", data: "paused"}));
                        }
                    });

                    player.addEventListener(Twitch.Player.ENDED, function() {
                        // Send message to Android
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "stateChange", data: "ended"}));
                        }
                    });

                    // Error handling
                    player.addEventListener(Twitch.Player.OFFLINE, function() {
                        // Send message to Android
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "playerError", data: "Video is offline"}));
                        }
                    });

                    } catch (error) {
                        // Send detailed error info for iOS debugging
                        var errorMessage = "Twitch player initialization failed: " + error.message;
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "playerError", data: errorMessage}));
                        }
                    }
                }

                // Initialize player when DOM is ready
                document.addEventListener('DOMContentLoaded', initializeTwitchPlayer);

                // --- Functions callable from Kotlin/Swift ---
                function getCurrentTime() {
                    try {
                        if (player && typeof player.getCurrentTime === 'function') {
                            var currentTime = player.getCurrentTime();
                            // Send debug message to Android
                            if (window.AndroidInterface) {
                                window.AndroidInterface.onMessage(JSON.stringify({type: "debug", data: "getCurrentTime called, returning: " + currentTime}));
                            }
                            return currentTime;
                        } else {
                            // Send debug message to Android
                            if (window.AndroidInterface) {
                                window.AndroidInterface.onMessage(JSON.stringify({type: "debug", data: "Player not ready or getCurrentTime not available"}));
                            }
                            return 0.0;
                        }
                    } catch (error) {
                        // Send debug message to Android
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "debug", data: "Error in getCurrentTime: " + error.message}));
                        }
                        return 0.0;
                    }
                }

                function seekTo(seconds) {
                    try {
                        if (player && typeof player.seek === 'function') {
                            player.seek(seconds);
                            return true;
                        }
                        return false;
                    } catch (error) {
                        // Send debug message to Android
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "debug", data: "Error in seekTo: " + error.message}));
                        }
                        return false;
                    }
                }

                function getPlayerState() {
                    try {
                        if (player && typeof player.isPaused === 'function') {
                            // Convert Twitch player state to YouTube-compatible integer states
                            // YouTube states: -1=unstarted, 0=ended, 1=playing, 2=paused, 3=buffering, 5=cued
                            return player.isPaused() ? 2 : 1;  // 2=paused, 1=playing
                        }
                        return -1;  // unknown/unstarted
                    } catch (error) {
                        return -1;  // error state
                    }
                }

                function pauseVideo() {
                    try {
                        if (player && typeof player.pause === 'function') {
                            player.pause();
                        }
                    } catch (error) {
                        // Send debug message to Android
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "debug", data: "Error in pauseVideo: " + error.message}));
                        }
                    }
                }

                function playVideo() {
                    try {
                        if (player && typeof player.play === 'function') {
                            player.play();
                        }
                    } catch (error) {
                        // Send debug message to Android
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "debug", data: "Error in playVideo: " + error.message}));
                        }
                    }
                }

                function setVolume(volume) {
                    try {
                        if (player && typeof player.setVolume === 'function') {
                            player.setVolume(volume);
                        }
                    } catch (error) {
                        // Send debug message to Android
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "debug", data: "Error in setVolume: " + error.message}));
                        }
                    }
                }

            </script>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * iOS implementation using simple iframe for parent/baseURL compatibility
     * Enhanced with JavaScript functions for on-demand time tracking
     */
    fun generateSimpleIframeHtml(videoId: String, parentDomain: String): String {
        val twitchUrl = "https://player.twitch.tv/?video=$videoId&parent=$parentDomain&autoplay=true&muted=true&allowfullscreen=true"

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body, html, iframe {
                    margin: 0; padding: 0; width: 100%; height: 100%;
                    border: none; background: black; overflow: hidden;
                }
            </style>
        </head>
        <body>
            <iframe id="twitchPlayer" src="$twitchUrl"
                    width="100%"
                    height="100%"
                    frameborder="0"
                    scrolling="no"
                    allowfullscreen="true">
            </iframe>
            <script>
                // Communication setup for iOS WebKit
                function sendMessageToNative(message) {
                    if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.nativeApp) {
                        window.webkit.messageHandlers.nativeApp.postMessage(message);
                    }
                }

                // Player time tracking for sync button usage
                var simulatedTime = 0;
                var playerStartTime = Date.now();
                var isPlaying = false;

                // JavaScript functions callable from iOS (on-demand only)
                function getCurrentTime() {
                    try {
                        if (isPlaying) {
                            // Calculate current time based on elapsed time since play started
                            var elapsed = (Date.now() - playerStartTime) / 1000;
                            return Math.max(0, elapsed);
                        } else {
                            // Return last known position when paused
                            return simulatedTime;
                        }
                    } catch (error) {
                        sendMessageToNative({
                            type: "debug",
                            data: "Error in getCurrentTime: " + error.message
                        });
                        return 0.0;
                    }
                }

                function getPlayerState() {
                    // Return simplified state: 1=playing, 2=paused
                    return isPlaying ? 1 : 2;
                }

                function seekTo(seconds) {
                    try {
                        // Update simulated time
                        simulatedTime = seconds;
                        playerStartTime = Date.now();
                        return true;
                    } catch (error) {
                        return false;
                    }
                }

                function pauseVideo() {
                    isPlaying = false;
                    simulatedTime = (Date.now() - playerStartTime) / 1000;
                }

                function playVideo() {
                    isPlaying = true;
                    playerStartTime = Date.now() - (simulatedTime * 1000);
                }

                // Auto-start time tracking (assumes autoplay)
                setTimeout(function() {
                    isPlaying = true;
                    playerStartTime = Date.now();
                    
                    sendMessageToNative({
                        type: "debug",
                        data: "iOS Twitch time tracking ready for sync button usage"
                    });
                }, 2000);
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * iOS implementation using full JavaScript API (recommended for accurate time tracking)
     */
    fun generateFullApiHtmlForIOS(videoId: String, parentDomain: String): String {
        // Ensure video ID has the 'v' prefix required by Twitch
        val formattedVideoId = if (videoId.startsWith("v")) videoId else "v$videoId"

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body, html, #player {
                    margin: 0; padding: 0; width: 100%; height: 100%;
                    background-color: black; overflow: hidden;
                }
            </style>
        </head>
        <body>
            <div id="player"></div>

            <script src="https://player.twitch.tv/js/embed/v1.js"></script>
            <script>
                var player;

                function sendMessageToNative(message) {
                    if (window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.nativeApp) {
                        window.webkit.messageHandlers.nativeApp.postMessage(message);
                    }
                }

                function initializeTwitchPlayer() {
                    try {
                        player = new Twitch.Player('player', {
                            width: '100%',
                            height: '100%',
                            video: '$formattedVideoId',
                            parent: ['$parentDomain'],
                            autoplay: false,
                            muted: false,
                            playsinline: true,
                            allowfullscreen: true,
                            layout: "video"
                        });

                        // Player events
                        player.addEventListener(Twitch.Player.READY, function() {
                            sendMessageToNative({type: "playerReady", data: "ready"});
                        });

                        player.addEventListener(Twitch.Player.PLAY, function() {
                            sendMessageToNative({type: "stateChange", data: "playing"});
                        });

                        player.addEventListener(Twitch.Player.PAUSE, function() {
                            sendMessageToNative({type: "stateChange", data: "paused"});
                        });

                        player.addEventListener(Twitch.Player.ENDED, function() {
                            sendMessageToNative({type: "stateChange", data: "ended"});
                        });

                        player.addEventListener(Twitch.Player.OFFLINE, function() {
                            sendMessageToNative({type: "playerError", data: "Video is offline"});
                        });

                    } catch (error) {
                        sendMessageToNative({
                            type: "playerError", 
                            data: "Twitch player initialization failed: " + error.message
                        });
                    }
                }

                // JavaScript functions for iOS WebView
                function getCurrentTime() {
                    try {
                        if (player && typeof player.getCurrentTime === 'function') {
                            var currentTime = player.getCurrentTime();
                            sendMessageToNative({
                                type: "debug", 
                                data: "getCurrentTime: " + currentTime
                            });
                            return currentTime;
                        }
                        return 0.0;
                    } catch (error) {
                        sendMessageToNative({
                            type: "debug", 
                            data: "Error in getCurrentTime: " + error.message
                        });
                        return 0.0;
                    }
                }

                function seekTo(seconds) {
                    try {
                        if (player && typeof player.seek === 'function') {
                            player.seek(seconds);
                            return true;
                        }
                        return false;
                    } catch (error) {
                        return false;
                    }
                }

                function getPlayerState() {
                    try {
                        if (player && typeof player.isPaused === 'function') {
                            return player.isPaused() ? 2 : 1;
                        }
                        return -1;
                    } catch (error) {
                        return -1;
                    }
                }

                function pauseVideo() {
                    try {
                        if (player && typeof player.pause === 'function') {
                            player.pause();
                        }
                    } catch (error) {
                        sendMessageToNative({
                            type: "debug", 
                            data: "Error in pauseVideo: " + error.message
                        });
                    }
                }

                function playVideo() {
                    try {
                        if (player && typeof player.play === 'function') {
                            player.play();
                        }
                    } catch (error) {
                        sendMessageToNative({
                            type: "debug", 
                            data: "Error in playVideo: " + error.message
                        });
                    }
                }

                document.addEventListener('DOMContentLoaded', initializeTwitchPlayer);

                sendMessageToNative({
                    type: "debug",
                    data: "iOS Twitch Full API loaded: " + '$videoId'
                });
            </script>
        </body>
        </html>
        """.trimIndent()
    }
}
