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
        youtubeApiKey = System.getenv("YOUTUBE_API_KEY")
        if (youtubeApiKey.isNullOrBlank()) {
            application.log.warn("YOUTUBE_API_KEY is not set. YouTube API features will be unavailable.")
        } else {
            application.log.info("YouTube API Key loaded successfully.")
        }

        twitchClientId = System.getenv("TWITCH_CLIENT_ID")
        if (twitchClientId.isNullOrBlank()) {
            application.log.warn("TWITCH_CLIENT_ID is not set. Twitch API features will be unavailable.")
        } else {
            application.log.info("Twitch Client ID loaded successfully.")
        }

        twitchClientSecret = System.getenv("TWITCH_CLIENT_SECRET")
        if (twitchClientSecret.isNullOrBlank()) {
            application.log.warn("TWITCH_CLIENT_SECRET is not set. Twitch API features will be unavailable.")
        } else {
            application.log.info("Twitch Client Secret loaded successfully.")
        }
    }
}
