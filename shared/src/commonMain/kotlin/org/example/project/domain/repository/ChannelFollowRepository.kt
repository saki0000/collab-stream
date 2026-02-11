package org.example.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.FollowedChannel
import org.example.project.domain.model.VideoServiceType

/**
 * チャンネルフォローのローカル永続化を担当するRepository。
 *
 * Room KMPによる実装を想定し、チャンネルの
 * フォロー追加、解除、一覧取得、状態確認を提供する。
 *
 * Epic: チャンネルフォロー & アーカイブHome
 * Shared across: US-1 (データ層), US-2 (フォローUI), US-3 (Home画面)
 */
interface ChannelFollowRepository {

    /**
     * チャンネルをフォローする。
     *
     * 既にフォロー済みの場合は何もしない（冪等）。
     *
     * @param channelId チャンネルID
     * @param channelName チャンネル表示名
     * @param channelIconUrl チャンネルアイコンURL
     * @param serviceType 動画サービスの種別
     * @return フォロー操作のResult
     */
    suspend fun follow(
        channelId: String,
        channelName: String,
        channelIconUrl: String,
        serviceType: VideoServiceType,
    ): Result<FollowedChannel>

    /**
     * チャンネルのフォローを解除する。
     *
     * フォローしていない場合は何もしない（冪等）。
     *
     * @param channelId チャンネルID
     * @param serviceType 動画サービスの種別
     * @return アンフォロー操作のResult
     */
    suspend fun unfollow(
        channelId: String,
        serviceType: VideoServiceType,
    ): Result<Unit>

    /**
     * チャンネルがフォロー済みかどうかを確認する。
     *
     * @param channelId チャンネルID
     * @param serviceType 動画サービスの種別
     * @return フォロー済みならtrue
     */
    suspend fun isFollowing(
        channelId: String,
        serviceType: VideoServiceType,
    ): Boolean

    /**
     * フォロー済みチャンネル一覧を取得する。
     *
     * @return フォロー済みチャンネルリストのResult（followedAt降順）
     */
    suspend fun getAllFollowedChannels(): Result<List<FollowedChannel>>

    /**
     * フォロー済みチャンネル一覧の変更をFlowとして監視する。
     *
     * UIでのリアルタイム更新に使用。
     * フォロー追加・解除時に新しいリストが発行される。
     *
     * @return フォロー済みチャンネルリストのFlow（followedAt降順）
     */
    fun observeFollowedChannels(): Flow<List<FollowedChannel>>
}
