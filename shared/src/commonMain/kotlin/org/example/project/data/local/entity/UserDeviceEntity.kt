package org.example.project.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ユーザーデバイス情報のデータベースエンティティ。
 *
 * 単一行テーブル（id = 1固定）でデバイスIDを保持する。
 * デバイスIDはUUID v4形式の文字列で、アプリ初回起動時に生成される。
 *
 * Story Issue: US-2（匿名認証 - デバイスID）
 * Epic: サブスクリプション基盤
 */
@Entity(
    tableName = "user_device",
)
data class UserDeviceEntity(
    /**
     * 固定PK（常に1）。
     *
     * 単一行テーブルとして運用するため、IDは常に1固定。
     */
    @PrimaryKey
    val id: Int = 1,

    /**
     * デバイスID（UUID v4形式）。
     *
     * アプリ初回起動時に生成され、永続化される。
     * 例: "550e8400-e29b-41d4-a716-446655440000"
     */
    val deviceId: String,
)
