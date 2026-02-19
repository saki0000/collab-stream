package org.example.project

/**
 * Android プラットフォーム用のサーバーベースURL
 *
 * Android エミュレータからローカルホストにアクセスする場合、
 * 10.0.2.2 を使用する必要がある（エミュレータのループバックインターフェース）
 */
actual val SERVER_BASE_URL: String = "http://10.0.2.2:$SERVER_PORT"
