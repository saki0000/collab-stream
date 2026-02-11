package org.example.project.config

import io.ktor.server.application.*

/**
 * APIキー設定
 *
 * 環境変数からYouTube API、Twitch APIのキーを読み込む。
 * ADR-005（段階的APIセキュリティ Phase 2）に基づき、
 * サーバーでAPIキーを一元管理する。
 */
object ApiKeyConfig {

    /**
     * YouTube API Key
     */
    var youtubeApiKey: String? = null
        private set

    /**
     * Twitch Client ID
     */
    var twitchClientId: String? = null
        private set

    /**
     * Twitch Client Secret
     */
    var twitchClientSecret: String? = null
        private set

    /**
     * 環境変数からAPIキーを読み込む
     *
     * 未設定の場合は警告ログを出力するが、サーバー起動は継続する。
     * これにより、開発初期段階での柔軟な動作を可能にする。
     */
    fun loadFromEnvironment(application: Application) {
        fun loadKey(keyName: String, description: String): String? {
            val value = System.getenv(keyName)
            if (value.isNullOrBlank()) {
                val serviceName = description.split(" ")[0]
                application.log.warn("$keyName is not set. $serviceName API features will be unavailable.")
            } else {
                application.log.info("$description loaded successfully.")
            }
            return value
        }

        youtubeApiKey = loadKey("YOUTUBE_API_KEY", "YouTube API Key")
        twitchClientId = loadKey("TWITCH_CLIENT_ID", "Twitch Client ID")
        twitchClientSecret = loadKey("TWITCH_CLIENT_SECRET", "Twitch Client Secret")
    }
}
