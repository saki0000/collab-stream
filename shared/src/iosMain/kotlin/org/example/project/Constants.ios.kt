package org.example.project

/**
 * iOS プラットフォーム用のサーバーベースURL
 *
 * iOS シミュレータからは localhost で直接アクセス可能
 */
actual val SERVER_BASE_URL: String = "http://localhost:$SERVER_PORT"
