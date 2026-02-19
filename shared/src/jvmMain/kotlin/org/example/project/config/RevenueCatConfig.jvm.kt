package org.example.project.config

/**
 * JVM用RevenueCat APIキー提供。
 *
 * RevenueCat KMP SDKはJVMに対応していないため、スタブ値を返す。
 * サーバーサイドではサブスクリプション機能は使用しないため問題ない。
 *
 * Epic: サブスクリプション基盤
 * Story Issue: US-3（RevenueCat SDK統合）
 */
actual fun getRevenueCatApiKey(): String {
    return "jvm_stub_api_key"
}
