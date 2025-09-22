package org.example.project.video.player

/**
 * Shared Twitch iframe HTML template for unified implementation across platforms.
 * Provides consistent Twitch player experience with JavaScript bridge communication.
 */
object TwitchIframeTemplate {
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
                    player = new Twitch.Player('player', {
                        width: '100%',
                        height: '100%',
                        video: '$formattedVideoId',
                        parent: ['$parentHost'],
                        autoplay: false,
                        muted: false
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
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "stateChange", data: "paused"}));
                        }
                    });

                    player.addEventListener(Twitch.Player.ENDED, function() {
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "stateChange", data: "ended"}));
                        }
                    });

                    // Error handling
                    player.addEventListener(Twitch.Player.OFFLINE, function() {
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "playerError", data: "Video is offline"}));
                        }
                    });
                }

                // Initialize player when DOM is ready
                document.addEventListener('DOMContentLoaded', initializeTwitchPlayer);

                // --- Functions callable from Kotlin/Swift ---
                function getCurrentTime() {
                    try {
                        if (player && typeof player.getCurrentTime === 'function') {
                            var currentTime = player.getCurrentTime();
                            if (window.AndroidInterface) {
                                window.AndroidInterface.onMessage(JSON.stringify({type: "debug", data: "getCurrentTime called, returning: " + currentTime}));
                            }
                            return currentTime;
                        } else {
                            if (window.AndroidInterface) {
                                window.AndroidInterface.onMessage(JSON.stringify({type: "debug", data: "Player not ready or getCurrentTime not available"}));
                            }
                            return 0.0;
                        }
                    } catch (error) {
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
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onMessage(JSON.stringify({type: "debug", data: "Error in seekTo: " + error.message}));
                        }
                        return false;
                    }
                }

                function getPlayerState() {
                    try {
                        if (player && typeof player.isPaused === 'function') {
                            return player.isPaused() ? "paused" : "playing";
                        }
                        return "unknown";
                    } catch (error) {
                        return "error";
                    }
                }

                function pauseVideo() {
                    try {
                        if (player && typeof player.pause === 'function') {
                            player.pause();
                        }
                    } catch (error) {
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
}
