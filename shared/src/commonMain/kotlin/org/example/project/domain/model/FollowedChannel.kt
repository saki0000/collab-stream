package org.example.project.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * フォロー済みチャンネルを表すEntity。
 *
 * ユーザーがフォローしたチャンネル情報をローカルに永続化し、
 * Home画面でのアーカイブ一覧表示に使用する。
 *
 * Epic: チャンネルフォロー & アーカイブHome
 * Shared across: US-1 (データ層), US-2 (フォローUI), US-3 (Home画面)
 */
@OptIn(ExperimentalTime::class)
data class FollowedChannel(
    /**
     * チャンネルの一意識別子（プラットフォーム固有）。
     */
    val channelId: String,

    /**
     * チャンネルの表示名。
     */
    val channelName: String,

    /**
     * チャンネルアイコン/アバターのURL。
     */
    val channelIconUrl: String,

    /**
     * 動画サービスの種別（YOUTUBE or TWITCH）。
     */
    val serviceType: VideoServiceType,

    /**
     * フォローした日時。
     */
    val followedAt: Instant,
)
