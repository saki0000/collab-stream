package org.example.project.data.mapper

import org.example.project.domain.model.TwitchVideoDetails
import org.example.project.domain.model.TwitchVideoDetailsImpl
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.YouTubeVideoDetails
import org.example.project.domain.model.YouTubeVideoDetailsImpl

/**
 * サーバーAPIレスポンスの単純data classからドメインのsealed classへの変換。
 *
 * サーバーは TwitchVideoDetails / YouTubeVideoDetails（単純data class）を返すが、
 * クライアントドメイン層では VideoDetails（sealed class）を使用するため変換が必要。
 */

/**
 * YouTubeVideoDetails（data class） → YouTubeVideoDetailsImpl（VideoDetails sealed class）
 */
fun YouTubeVideoDetails.toVideoDetails(): VideoDetails =
    YouTubeVideoDetailsImpl(
        id = id,
        snippet = snippet,
        liveStreamingDetails = liveStreamingDetails,
    )

/**
 * TwitchVideoDetails（data class） → TwitchVideoDetailsImpl（VideoDetails sealed class）
 */
fun TwitchVideoDetails.toVideoDetails(): VideoDetails =
    TwitchVideoDetailsImpl(
        id = id,
        snippet = snippet,
        streamInfo = streamInfo,
    )
