package org.example.project.data.repository

import org.example.project.domain.repository.SubscriptionRepository
import org.example.project.domain.repository.UserRepository

/**
 * プラットフォーム固有のSubscriptionRepositoryを作成する。
 *
 * RevenueCat KMP SDKはAndroid/iOSのみサポートのため、
 * expect/actualパターンでプラットフォーム固有実装を提供する。
 *
 * Epic: サブスクリプション基盤
 * Story Issue: US-3（RevenueCat SDK統合）
 */
expect fun createSubscriptionRepository(
    userRepository: UserRepository,
): SubscriptionRepository
