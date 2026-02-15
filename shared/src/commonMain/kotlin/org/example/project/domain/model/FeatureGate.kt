package org.example.project.domain.model

import kotlinx.coroutines.flow.Flow

/**
 * Feature Gateインターフェース。
 *
 * ユーザーのサブスクリプション状態に基づいて、
 * 機能の利用可否を判定する。
 */
interface FeatureGate {

    /**
     * 指定された機能が現在のサブスクリプションで利用可能かを判定する。
     *
     * @param feature 判定対象の機能
     * @return 利用可能ならtrue
     */
    suspend fun isFeatureAvailable(feature: Feature): Boolean

    /**
     * 現在のサブスクリプションプラン種別をFlowとして監視する。
     *
     * プラン変更時に新しい値が発行される。
     *
     * @return 現在のSubscriptionTierのFlow
     */
    fun observeCurrentTier(): Flow<SubscriptionTier>
}
