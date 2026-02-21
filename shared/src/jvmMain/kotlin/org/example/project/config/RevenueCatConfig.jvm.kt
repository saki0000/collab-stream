package org.example.project.config

import org.example.project.BuildKonfig

/**
 * JVM用RevenueCat APIキー提供。
 *
 * RevenueCat KMP SDKはJVMに対応していないため、
 * BuildKonfigのデフォルト値（プレースホルダー）がそのまま使用される。
 *
 * Epic: サブスクリプション基盤
 * Story Issue: US-3（RevenueCat SDK統合）
 */
actual fun getRevenueCatApiKey(): String {
    return BuildKonfig.REVENUECAT_API_KEY
}
