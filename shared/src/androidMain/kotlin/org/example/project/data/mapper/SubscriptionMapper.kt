package org.example.project.data.mapper

import com.revenuecat.purchases.kmp.models.CustomerInfo
import org.example.project.domain.model.SubscriptionStatus
import org.example.project.domain.model.SubscriptionTier

/** RevenueCat Dashboardで設定するProプランのEntitlement ID */
private const val ENTITLEMENT_ID_PRO = "pro"

/**
 * RevenueCat CustomerInfo から SubscriptionStatus へ変換する。
 */
@OptIn(kotlin.time.ExperimentalTime::class)
internal fun CustomerInfo.toSubscriptionStatus(): SubscriptionStatus {
    val proEntitlement = entitlements[ENTITLEMENT_ID_PRO]
    val isProActive = proEntitlement?.isActive == true

    return SubscriptionStatus(
        tier = if (isProActive) SubscriptionTier.PRO else SubscriptionTier.FREE,
        isActive = isProActive,
        expiresAtMillis = proEntitlement?.expirationDate?.toEpochMilliseconds(),
        willRenew = proEntitlement?.willRenew ?: false,
    )
}
