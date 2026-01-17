package org.example.project.data.local

/**
 * プラットフォーム固有のデータベースビルダー。
 *
 * Android/iOSでそれぞれ異なる方法でデータベースファイルのパスを取得し、
 * AppDatabaseインスタンスを構築する。
 *
 * Story Issue: #36
 * Epic: EPIC-003（同期チャンネル履歴保存）
 */
expect class DatabaseBuilder {
    /**
     * AppDatabaseインスタンスを構築する。
     *
     * @return 構築されたAppDatabase
     */
    fun build(): AppDatabase
}
