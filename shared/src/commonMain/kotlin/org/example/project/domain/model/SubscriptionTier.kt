package org.example.project.domain.model

import kotlinx.serialization.Serializable

/**
 * サブスクリプションのプラン種別。
 * Free/Proの2段階構成。
 */
@Serializable
enum class SubscriptionTier {
    FREE,
    PRO,
}
