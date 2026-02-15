package org.example.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.SubscriptionStatus

/**
 * サブスクリプション管理のRepository。
 *
 * RevenueCat SDKによる課金処理とサブスクリプション状態管理を担当する。
 * suspend + Result<T> + Flow パターンに準拠。
 *
 * Epic: サブスクリプション基盤
 * Shared across: US-3 (RevenueCat統合), US-4 (管理UI)
 */
interface SubscriptionRepository {

    /**
     * 現在のサブスクリプション状態を取得する。
     *
     * @return サブスクリプション状態のResult
     */
    suspend fun getSubscriptionStatus(): Result<SubscriptionStatus>

    /**
     * サブスクリプション状態の変更をFlowとして監視する。
     *
     * 購入・復元・期限切れなどで新しい状態が発行される。
     *
     * @return サブスクリプション状態のFlow
     */
    fun observeSubscriptionStatus(): Flow<SubscriptionStatus>

    /**
     * Proプランを購入する。
     *
     * @return 購入後のサブスクリプション状態のResult
     */
    suspend fun purchaseProPlan(): Result<SubscriptionStatus>

    /**
     * 過去の購入を復元する。
     *
     * @return 復元後のサブスクリプション状態のResult
     */
    suspend fun restorePurchases(): Result<SubscriptionStatus>
}
