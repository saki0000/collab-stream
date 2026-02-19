package org.example.project

/**
 * JVM プラットフォーム用のサーバーベースURL
 *
 * JVM からは localhost で直接アクセス可能
 */
actual val SERVER_BASE_URL: String = "http://localhost:$SERVER_PORT"
