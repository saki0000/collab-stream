package org.example.project.config

/**
 * iOS用RevenueCat APIキー提供。
 *
 * 開発時はプレースホルダー値を返す。
 * 本番環境では環境変数やBuildKonfigから取得する。
 *
 * Epic: サブスクリプション基盤
 * Story Issue: US-3（RevenueCat SDK統合）
 */
actual fun getRevenueCatApiKey(): String {
    // TODO: 本番環境では環境変数やBuildKonfigから取得
    return "ios_placeholder_api_key"
}
