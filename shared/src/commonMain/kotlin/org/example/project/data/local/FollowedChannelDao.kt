package org.example.project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.project.data.local.entity.FollowedChannelEntity
import org.example.project.domain.model.VideoServiceType

/**
 * フォロー済みチャンネル情報のDAO。
 *
 * Story Issue: US-1（チャンネルフォロー データ層）
 * Specification: feature/channel_follow/SPECIFICATION.md
 */
@Dao
interface FollowedChannelDao {
    /**
     * チャンネルをフォローする。
     *
     * 既存のフォローがあればREPLACEして更新される（冪等性を保証）。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channel: FollowedChannelEntity)

    /**
     * チャンネルのフォローを解除する。
     *
     * 存在しない場合は何もしない（冪等性を保証）。
     */
    @Query("DELETE FROM followed_channel WHERE channelId = :channelId AND serviceType = :serviceType")
    suspend fun delete(channelId: String, serviceType: VideoServiceType)

    /**
     * 指定したチャンネルがフォロー済みかどうかを確認する。
     */
    @Query("SELECT EXISTS(SELECT 1 FROM followed_channel WHERE channelId = :channelId AND serviceType = :serviceType)")
    suspend fun isFollowing(channelId: String, serviceType: VideoServiceType): Boolean

    /**
     * フォロー済みチャンネル一覧を取得する。
     *
     * フォロー日時の降順（新しいフォローが先頭）でソートする。
     */
    @Query("SELECT * FROM followed_channel ORDER BY followedAt DESC")
    suspend fun getAll(): List<FollowedChannelEntity>

    /**
     * フォロー済みチャンネル一覧をFlowで監視する。
     *
     * フォロー日時の降順（新しいフォローが先頭）でソートする。
     */
    @Query("SELECT * FROM followed_channel ORDER BY followedAt DESC")
    fun observeAll(): Flow<List<FollowedChannelEntity>>
}
