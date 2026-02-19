package org.example.project.config

/**
 * RevenueCat SDK設定。
 *
 * プラットフォーム固有のAPIキーを提供する。
 * expect/actualパターンにより、Android/iOSで異なるAPIキーを使用。
 *
 * Epic: サブスクリプション基盤
 * Story Issue: US-3（RevenueCat SDK統合）
 */
expect fun getRevenueCatApiKey(): String
