package org.example.project.data.repository

import org.example.project.domain.repository.SubscriptionRepository
import org.example.project.domain.repository.UserRepository

/**
 * iOS用のSubscriptionRepositoryを作成する。
 *
 * RevenueCat KMP SDKを使用したiOS実装を提供する。
 *
 * Epic: サブスクリプション基盤
 * Story Issue: US-3（RevenueCat SDK統合）
 */
actual fun createSubscriptionRepository(
    userRepository: UserRepository,
): SubscriptionRepository {
    return SubscriptionRepositoryImpl(userRepository)
}
