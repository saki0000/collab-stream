package org.example.project.domain.model

import kotlinx.serialization.Serializable

/**
 * サブスクリプションの現在の状態を表すドメインモデル。
 *
 * @property tier 現在のプラン種別
 * @property isActive サブスクリプションが有効かどうか
 * @property expiresAtMillis 有効期限（エポックミリ秒）。nullの場合は無期限（Freeプラン等）
 * @property willRenew 自動更新が有効かどうか
 */
@Serializable
data class SubscriptionStatus(
    val tier: SubscriptionTier,
    val isActive: Boolean,
    val expiresAtMillis: Long? = null,
    val willRenew: Boolean = false,
)
