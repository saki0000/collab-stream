package org.example.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.domain.model.Feature
import org.example.project.domain.model.FeatureGate
import org.example.project.domain.model.SubscriptionTier
import org.example.project.domain.repository.SubscriptionRepository

/**
 * FeatureGateの実装。
 *
 * サブスクリプション状態に基づいて、機能の利用可否を判定する。
 * 判定ロジック: Feature.requiredTier <= currentTier
 *
 * Epic: サブスクリプション基盤
 * Story Issue: US-1（Feature Gate定義）, US-3（RevenueCat SDK統合）
 */
class FeatureGateImpl(
    private val subscriptionRepository: SubscriptionRepository,
) : FeatureGate {

    /**
     * 指定された機能が現在のサブスクリプションで利用可能かを判定する。
     *
     * 判定ロジック:
     * - FREEユーザー: requiredTier == FREE の機能のみ利用可能
     * - PROユーザー: すべての機能が利用可能
     *
     * @param feature 判定対象の機能
     * @return 利用可能ならtrue
     */
    override suspend fun isFeatureAvailable(feature: Feature): Boolean {
        val status = subscriptionRepository.getSubscriptionStatus()
            .getOrNull() ?: return false

        return isFeatureAvailableForTier(feature.requiredTier, status.tier)
    }

    /**
     * 現在のサブスクリプションプラン種別をFlowとして監視する。
     *
     * プラン変更時に新しい値が発行される。
     *
     * @return 現在のSubscriptionTierのFlow
     */
    override fun observeCurrentTier(): Flow<SubscriptionTier> {
        return subscriptionRepository.observeSubscriptionStatus()
            .map { it.tier }
    }

    /**
     * 機能が指定されたプランで利用可能かを判定する。
     *
     * SubscriptionTierの序数（ordinal）を使用して階層判定を行う。
     * FREE < PRO の順序になっているため、currentTier >= requiredTier で判定できる。
     *
     * @param requiredTier 機能に必要なプラン種別
     * @param currentTier 現在のプラン種別
     * @return 利用可能ならtrue
     */
    private fun isFeatureAvailableForTier(
        requiredTier: SubscriptionTier,
        currentTier: SubscriptionTier,
    ): Boolean {
        return currentTier.ordinal >= requiredTier.ordinal
    }
}
