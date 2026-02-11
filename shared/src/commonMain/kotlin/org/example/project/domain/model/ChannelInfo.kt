package org.example.project.domain.model

/**
 * チャンネル検索結果のドメインモデル。
 * 検索候補の表示やチャンネル追加に使用する。
 */
data class ChannelInfo(
    val id: String,
    val displayName: String,
    val thumbnailUrl: String? = null,
    val broadcasterLanguage: String? = null,
    val gameId: String? = null,
    val gameName: String? = null,
    val serviceType: VideoServiceType = VideoServiceType.TWITCH,
)
