package org.example.project.data.local

/**
 * JVM用のデータベースビルダー実装（ダミー）。
 *
 * Room KMPはJVMプラットフォームをサポートしていないため、
 * この実装は使用されることを想定していません。
 * サーバーモジュールでローカル永続化が必要な場合は別途設計が必要です。
 *
 * Story Issue: #36
 * Epic: EPIC-003（同期チャンネル履歴保存）
 */
actual class DatabaseBuilder {
    /**
     * JVMではサポートされていないため、常に例外をスローする。
     *
     * @throws UnsupportedOperationException 常にスロー
     */
    actual fun build(): AppDatabase {
        throw UnsupportedOperationException(
            "Room KMP is not supported on JVM platform. " +
                "This database is intended for Android and iOS only.",
        )
    }
}
