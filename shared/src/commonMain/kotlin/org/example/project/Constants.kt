package org.example.project

const val SERVER_PORT = 8080

/**
 * サーバーのベースURL
 *
 * 開発環境用のローカルサーバーURL。
 * Android エミュレータからは 10.0.2.2 を使用する必要がある。
 * その他のプラットフォームは localhost を使用。
 *
 * TODO: 本番環境用のCloud Run URLに切り替え可能にする
 */
expect val SERVER_BASE_URL: String
