package org.example.project.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

/**
 * Android用のデータベースビルダー実装。
 *
 * ApplicationContextを使用してデータベースファイルのパスを取得し、
 * BundledSQLiteDriverで一貫したSQLite動作を保証する。
 *
 * Story Issue: #36
 * Epic: EPIC-003（同期チャンネル履歴保存）
 */
actual class DatabaseBuilder(private val context: Context) {
    /**
     * AppDatabaseインスタンスを構築する。
     *
     * @return 構築されたAppDatabase
     */
    actual fun build(): AppDatabase {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        return Room.databaseBuilder<AppDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    companion object {
        private const val DATABASE_NAME = "collabstream.db"
    }
}
