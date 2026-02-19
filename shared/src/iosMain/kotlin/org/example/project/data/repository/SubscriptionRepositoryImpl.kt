package org.example.project.data.repository

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.project.config.getRevenueCatApiKey
import org.example.project.data.mapper.toSubscriptionStatus
import org.example.project.domain.model.SubscriptionStatus
import org.example.project.domain.repository.SubscriptionRepository
import org.example.project.domain.repository.UserRepository

/**
 * SubscriptionRepositoryのiOS実装。
 *
 * RevenueCat SDKを使用したサブスクリプション管理を担当する。
 * SDK初期化はDouble-Checked Locking with Mutexパターンで行う。
 */
internal class SubscriptionRepositoryImpl(
    private val userRepository: UserRepository,
) : SubscriptionRepository {

    private val mutex = Mutex()
    private var isConfigured = false

    /**
     * delegateからの更新を複数のコレクターに安全にファンアウトするためのSharedFlow。
     *
     * シングルトンである Purchases.sharedInstance.delegate を直接 callbackFlow で
     * 設定・クリアすると、複数コレクター間で上書き・null化が発生するため、
     * SDK初期化時に一度だけdelegateを設定し、このSharedFlowで中継する。
     */
    private val _customerInfoUpdates = MutableSharedFlow<SubscriptionStatus>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * RevenueCat SDKを初期化する。
     *
     * delegateもここで一度だけ設定し、以降は変更しない。
     */
    private suspend fun ensureConfigured() {
        if (isConfigured) return

        mutex.withLock {
            if (isConfigured) return

            val deviceId = userRepository.getDeviceId()
            val config = PurchasesConfiguration(apiKey = getRevenueCatApiKey()) {
                appUserId = deviceId
            }
            Purchases.configure(config)

            Purchases.sharedInstance.delegate = object : PurchasesDelegate {
                override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
                    _customerInfoUpdates.tryEmit(customerInfo.toSubscriptionStatus())
                }

                override fun onPurchasePromoProduct(
                    product: StoreProduct,
                    startPurchase: (
                        onError: (error: PurchasesError, userCancelled: Boolean) -> Unit,
                        onSuccess: (storeTransaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit,
                    ) -> Unit,
                ) {
                    // App Storeプロモーション購入は未対応
                }
            }

            isConfigured = true
        }
    }

    override suspend fun getSubscriptionStatus(): Result<SubscriptionStatus> = runCatching {
        ensureConfigured()
        val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()
        customerInfo.toSubscriptionStatus()
    }

    override fun observeSubscriptionStatus(): Flow<SubscriptionStatus> = flow {
        ensureConfigured()

        // 初回値を発行
        val initialCustomerInfo = Purchases.sharedInstance.awaitCustomerInfo()
        emit(initialCustomerInfo.toSubscriptionStatus())

        // delegateからの更新をSharedFlow経由で受信
        emitAll(_customerInfoUpdates)
    }

    override suspend fun purchaseProPlan(): Result<SubscriptionStatus> = runCatching {
        ensureConfigured()

        val offerings = Purchases.sharedInstance.awaitOfferings()
        val currentOffering = offerings.current
            ?: throw IllegalStateException("利用可能なOfferingが見つかりません")

        val packageToPurchase = currentOffering.availablePackages.firstOrNull()
            ?: throw IllegalStateException("利用可能なパッケージが見つかりません")

        val purchaseResult = Purchases.sharedInstance.awaitPurchase(packageToPurchase)
        purchaseResult.customerInfo.toSubscriptionStatus()
    }

    override suspend fun restorePurchases(): Result<SubscriptionStatus> = runCatching {
        ensureConfigured()
        val customerInfo = Purchases.sharedInstance.awaitRestore()
        customerInfo.toSubscriptionStatus()
    }
}
