package org.example.project.domain.model

import kotlinx.serialization.Serializable

/**
 * 履歴に保存されるチャンネル情報の軽量版。
 *
 * SyncChannelから永続化に必要な情報のみを抽出したモデル。
 * ストリーム情報や同期状態は含まず、チャンネルの識別情報のみを保持する。
 *
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 * Shared across: US-1 (永続化基盤), US-2 (履歴保存), US-3 (履歴一覧), US-4 (再同期)
 */
@Serializable
data class SavedChannelInfo(
    /**
     * チャンネルID（プラットフォーム固有）。
     */
    val channelId: String,

    /**
     * チャンネル名（表示用、キャッシュ）。
     */
    val channelName: String,

    /**
     * チャンネルアイコンURL（表示用、キャッシュ）。
     */
    val channelIconUrl: String,

    /**
     * 動画サービスタイプ（YOUTUBE or TWITCH）。
     */
    val serviceType: VideoServiceType,
)

/**
 * SyncChannelからSavedChannelInfoへ変換する拡張関数。
 *
 * @return 永続化用の軽量チャンネル情報
 */
fun SyncChannel.toSavedChannelInfo(): SavedChannelInfo = SavedChannelInfo(
    channelId = channelId,
    channelName = channelName,
    channelIconUrl = channelIconUrl,
    serviceType = serviceType,
)
