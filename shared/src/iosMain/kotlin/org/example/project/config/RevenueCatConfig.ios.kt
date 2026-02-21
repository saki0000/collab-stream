package org.example.project.config

import org.example.project.BuildKonfig

/**
 * iOS用RevenueCat APIキー提供。
 *
 * BuildKonfig経由でビルド時に注入された値を返す。
 * CI環境ではGitHub Secretsから、ローカル環境ではプレースホルダー値が使用される。
 *
 * Epic: サブスクリプション基盤
 * Story Issue: US-3（RevenueCat SDK統合）
 */
actual fun getRevenueCatApiKey(): String {
    return BuildKonfig.REVENUECAT_API_KEY
}
