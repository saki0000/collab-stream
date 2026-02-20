package org.example.project

const val SERVER_PORT = 8080

/**
 * サーバーのベースURL
 *
 * Cloud Run にデプロイされた本番サーバーのURL。
 * ローカル開発時は以下に切り替える:
 *   - Android エミュレータ: "http://10.0.2.2:$SERVER_PORT"
 *   - iOS シミュレータ / JVM: "http://localhost:$SERVER_PORT"
 */
const val SERVER_BASE_URL: String = "https://collabstream-server-mmaqsoxiwq-an.a.run.app"
