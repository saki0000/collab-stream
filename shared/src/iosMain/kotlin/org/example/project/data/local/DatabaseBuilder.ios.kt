package org.example.project.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSHomeDirectory

/**
 * iOS用のデータベースビルダー実装。
 *
 * NSHomeDirectoryを使用してDocumentsディレクトリにデータベースファイルを配置し、
 * BundledSQLiteDriverで一貫したSQLite動作を保証する。
 *
 * Story Issue: #36
 * Epic: EPIC-003（同期チャンネル履歴保存）
 */
actual class DatabaseBuilder {
    /**
     * AppDatabaseインスタンスを構築する。
     *
     * @return 構築されたAppDatabase
     */
    actual fun build(): AppDatabase {
        val dbFilePath = NSHomeDirectory() + "/Documents/$DATABASE_NAME"
        return Room.databaseBuilder<AppDatabase>(name = dbFilePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    companion object {
        private const val DATABASE_NAME = "collabstream.db"
    }
}
