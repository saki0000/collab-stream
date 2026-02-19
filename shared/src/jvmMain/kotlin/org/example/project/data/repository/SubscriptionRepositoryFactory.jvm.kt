package org.example.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.example.project.domain.model.SubscriptionStatus
import org.example.project.domain.model.SubscriptionTier
import org.example.project.domain.repository.SubscriptionRepository
import org.example.project.domain.repository.UserRepository

/**
 * JVM用のSubscriptionRepositoryを作成する。
 *
 * RevenueCat KMP SDKはJVMに対応していないため、スタブ実装を提供する。
 * サーバーサイドではサブスクリプション機能は使用しないため問題ない。
 *
 * Epic: サブスクリプション基盤
 * Story Issue: US-3（RevenueCat SDK統合）
 */
actual fun createSubscriptionRepository(
    userRepository: UserRepository,
): SubscriptionRepository {
    return StubSubscriptionRepository()
}

/**
 * JVM用のスタブSubscriptionRepository実装。
 *
 * RevenueCat SDKが利用できない環境用のフォールバック実装。
 * 常にFreeプランを返す。
 */
private class StubSubscriptionRepository : SubscriptionRepository {

    private val freeStatus = SubscriptionStatus(
        tier = SubscriptionTier.FREE,
        isActive = false,
        expiresAtMillis = null,
        willRenew = false,
    )

    override suspend fun getSubscriptionStatus(): Result<SubscriptionStatus> {
        return Result.success(freeStatus)
    }

    override fun observeSubscriptionStatus(): Flow<SubscriptionStatus> {
        return flowOf(freeStatus)
    }

    override suspend fun purchaseProPlan(): Result<SubscriptionStatus> {
        return Result.failure(
            UnsupportedOperationException("サブスクリプション購入はJVMでは利用できません"),
        )
    }

    override suspend fun restorePurchases(): Result<SubscriptionStatus> {
        return Result.failure(
            UnsupportedOperationException("購入復元はJVMでは利用できません"),
        )
    }
}
