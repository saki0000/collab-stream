package org.example.project.data.local

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import org.example.project.data.local.entity.FollowedChannelEntity
import org.example.project.data.local.entity.SavedChannelEntity
import org.example.project.data.local.entity.SyncHistoryEntity

/**
 * アプリケーションのメインデータベース。
 *
 * Room KMPを使用してAndroid/iOSで共通のデータベース定義を提供する。
 *
 * Story Issue: #36, US-1
 * Epic: EPIC-003（同期チャンネル履歴保存）
 */
@Database(
    entities = [
        SyncHistoryEntity::class,
        SavedChannelEntity::class,
        FollowedChannelEntity::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * 同期履歴のDAOを取得する。
     */
    abstract fun syncHistoryDao(): SyncHistoryDao

    /**
     * フォロー済みチャンネルのDAOを取得する。
     */
    abstract fun followedChannelDao(): FollowedChannelDao
}

/**
 * RoomDatabaseConstructorの実装。
 *
 * Room KMPでは、expect/actualパターンでデータベースのインスタンス化を行う。
 * KSPによりactual実装が自動生成される。
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
