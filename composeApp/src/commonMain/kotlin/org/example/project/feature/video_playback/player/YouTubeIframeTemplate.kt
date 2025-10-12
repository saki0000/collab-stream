package org.example.project.feature.video_playback.player

/**
 * Shared YouTube iframe HTML template for unified implementation across platforms.
 * Provides consistent YouTube player experience with JavaScript bridge communication.
 * Note: YouTube doesn't require parent domain configuration like Twitch does.
 */
object YouTubeIframeTemplate {
    fun generateHtml(videoId: String): String {
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

            <script>
                // 2. IFrame Player APIのコードを非同期で読み込む
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                // 3. APIコードがダウンロードされた後にiframeとプレーヤーを作成する
                var player;
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        height: '100%',
                        width: '100%',
                        videoId: '$videoId',
                        playerVars: {
                            'playsinline': 1, // iOSでインライン再生を有効化
                            'autoplay': 1,    // 自動再生
                            'controls': 1,    // プレーヤーコントロールを非表示
                            'fs': 1,          // 全画面表示ボタンを非表示
                            'modestbranding': 1 // YouTubeロゴを控えめに
                        },
                        events: {
                            'onReady': onPlayerReady,
                            'onStateChange': onPlayerStateChange,
                            'onError': onPlayerError
                        }
                    });
                }

                // 4. APIがプレーヤーの準備ができたときにこの関数を呼び出す
                function onPlayerReady(event) {
                    // JavaScript Interfaceを経由してAndroid側のメソッドを呼び出す
                    if (window.AndroidInterface) {
                        window.AndroidInterface.onMessage(JSON.stringify({type: "playerReady", data: "ready"}));
                    }
                    // event.target.playVideo(); // 準備完了後に再生する場合
                }

                // 5. プレーヤーの状態が変化したときにAPIがこの関数を呼び出す
                function onPlayerStateChange(event) {
                    if (window.AndroidInterface) {
                        // event.dataに現在の状態コードが入っている
                        window.AndroidInterface.onMessage(JSON.stringify({type: "stateChange", data: event.data.toString()}));
                    }
                }

                function onPlayerError(event) {
                    if (window.AndroidInterface) {
                        window.AndroidInterface.onMessage(JSON.stringify({type: "playerError", data: event.data.toString()}));
                    }
                }
                
                // --- Kotlin側から呼び出すための関数 ---
                function getCurrentTime() {
                    try {
                        if (player && typeof player.getCurrentTime === 'function') {
                            var currentTime = player.getCurrentTime();
                            // Debug: AndroidInterfaceにメッセージを送信
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
