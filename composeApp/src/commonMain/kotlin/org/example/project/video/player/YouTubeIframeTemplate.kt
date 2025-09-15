package org.example.project.video.player

/**
 * Shared YouTube iframe HTML template for unified implementation across platforms.
 * Provides consistent YouTube player experience with JavaScript bridge communication.
 */
object YouTubeIframeTemplate {

    fun generateHtml(videoId: String): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                html, body {
                    width: 100%;
                    height: 100%;
                    background: #000;
                    overflow: hidden;
                }
                #player {
                    width: 100%;
                    height: 100%;
                }
            </style>
        </head>
        <body>
            <div id="player"></div>

            <script>
                var player;
                var currentTime = 0.0;

                // Load YouTube iframe API
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                // YouTube API ready callback
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        height: '100%',
                        width: '100%',
                        videoId: '$videoId',
                        playerVars: {
                            autoplay: 0,
                            controls: 1,
                            rel: 0,
                            playsinline: 1,
                            origin: window.location.origin
                        },
                        events: {
                            'onReady': onPlayerReady,
                            'onStateChange': onPlayerStateChange
                        }
                    });
                }

                function onPlayerReady(event) {
                    console.log('YouTube player ready');
                    // Start tracking current time
                    setInterval(updateCurrentTime, 100);
                }

                function onPlayerStateChange(event) {
                    console.log('Player state changed:', event.data);
                    // Notify native code about state changes
                    try {
                        if (typeof webkit !== 'undefined' && webkit.messageHandlers && webkit.messageHandlers.nativeApp) {
                            webkit.messageHandlers.nativeApp.postMessage(event.data);
                        }
                    } catch(e) {
                        console.log('Error posting message to native:', e);
                    }
                }

                function updateCurrentTime() {
                    if (player && typeof player.getCurrentTime === 'function') {
                        currentTime = player.getCurrentTime();
                    }
                }

                // JavaScript functions called from native code
                function getCurrentTime() {
                    if (player && typeof player.getCurrentTime === 'function') {
                        currentTime = player.getCurrentTime();
                        return currentTime;
                    }
                    return 0.0;
                }

                function seekTo(seconds) {
                    if (player && typeof player.seekTo === 'function') {
                        player.seekTo(seconds, true);
                        return true;
                    }
                    return false;
                }

                function getPlayerState() {
                    if (player && typeof player.getPlayerState === 'function') {
                        return player.getPlayerState();
                    }
                    return -1;
                }

                function pauseVideo() {
                    if (player && typeof player.pauseVideo === 'function') {
                        player.pauseVideo();
                    }
                }

                function playVideo() {
                    if (player && typeof player.playVideo === 'function') {
                        player.playVideo();
                    }
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }
}