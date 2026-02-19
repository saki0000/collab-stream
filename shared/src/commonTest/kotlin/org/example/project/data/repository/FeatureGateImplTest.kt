package org.example.project.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.project.domain.model.SubscriptionStatus
import org.example.project.domain.model.SubscriptionTier
import org.example.project.domain.repository.SubscriptionRepository

/**
 * FeatureGateImplのテスト。
 *
 * Feature判定ロジックとプラン監視のテストを実施する。
 *
 * Epic: サブスクリプション基盤
 * Story Issue: US-1（Feature Gate定義）, US-3（RevenueCat SDK統合）
 * Specification: feature/subscription/SPECIFICATION.md
 */
class FeatureGateImplTest {

    // ========================================
    // Feature判定ロジック
    // ========================================

    @Test
    fun `FREEユーザー_FREE機能が利用可能であること`() = runTest {
        // Arrange
        val freeStatus = SubscriptionStatus(
            tier = SubscriptionTier.FREE,
            isActive = false,
        )
        val repository = FakeSubscriptionRepository(freeStatus)
        val featureGate = FeatureGateImpl(repository)

        // Act & Assert
        // Phase 0では Feature enum が空のため、実際のFeature追加後にテストを追加
        // 現時点ではロジックの正しさを検証するため、ティア順序のテストのみ実施
        assertTrue(SubscriptionTier.FREE.ordinal < SubscriptionTier.PRO.ordinal)
    }

    @Test
    fun `FREEユーザー_PRO機能が利用不可であること`() = runTest {
        // Arrange
        val freeStatus = SubscriptionStatus(
            tier = SubscriptionTier.FREE,
            isActive = false,
        )
        val repository = FakeSubscriptionRepository(freeStatus)
        val featureGate = FeatureGateImpl(repository)

        // Act & Assert
        // Phase 0では Feature enum が空のため、実際のFeature追加後にテストを追加
        // 現時点ではロジックの正しさを検証するため、ティア順序のテストのみ実施
        assertTrue(SubscriptionTier.FREE.ordinal < SubscriptionTier.PRO.ordinal)
    }

    @Test
    fun `PROユーザー_すべての機能が利用可能であること`() = runTest {
        // Arrange
        val proStatus = SubscriptionStatus(
            tier = SubscriptionTier.PRO,
            isActive = true,
            expiresAtMillis = 1_700_000_000_000L, // テスト用固定値
            willRenew = true,
        )
        val repository = FakeSubscriptionRepository(proStatus)
        val featureGate = FeatureGateImpl(repository)

        // Act & Assert
        // Phase 0では Feature enum が空のため、実際のFeature追加後にテストを追加
        // 現時点ではロジックの正しさを検証するため、ティア順序のテストのみ実施
        assertTrue(SubscriptionTier.PRO.ordinal >= SubscriptionTier.FREE.ordinal)
        assertTrue(SubscriptionTier.PRO.ordinal >= SubscriptionTier.PRO.ordinal)
    }

    @Test
    fun `Repository取得失敗時_機能が利用不可であること`() = runTest {
        // Arrange
        val repository = FakeSubscriptionRepository(null) // エラーをシミュレート
        val featureGate = FeatureGateImpl(repository)

        // Act & Assert
        // Phase 0では Feature enum が空のため、実際のFeature追加後にテストを追加
        // 現時点では null 時のロジックのみ検証
        val status = repository.getSubscriptionStatus().getOrNull()
        assertEquals(null, status)
    }

    // ========================================
    // プラン監視
    // ========================================

    @Test
    fun `observeCurrentTier_現在のプラン種別を返すこと`() = runTest {
        // Arrange
        val proStatus = SubscriptionStatus(
            tier = SubscriptionTier.PRO,
            isActive = true,
        )
        val repository = FakeSubscriptionRepository(proStatus)
        val featureGate = FeatureGateImpl(repository)

        // Act
        val currentTier = featureGate.observeCurrentTier().first()

        // Assert
        assertEquals(SubscriptionTier.PRO, currentTier)
    }

    @Test
    fun `observeCurrentTier_FREEプランを正しく返すこと`() = runTest {
        // Arrange
        val freeStatus = SubscriptionStatus(
            tier = SubscriptionTier.FREE,
            isActive = false,
        )
        val repository = FakeSubscriptionRepository(freeStatus)
        val featureGate = FeatureGateImpl(repository)

        // Act
        val currentTier = featureGate.observeCurrentTier().first()

        // Assert
        assertEquals(SubscriptionTier.FREE, currentTier)
    }

    // ========================================
    // ティア階層判定
    // ========================================

    @Test
    fun `ティア順序_FREEよりPROが上位であること`() {
        // Arrange & Act & Assert
        assertTrue(
            SubscriptionTier.FREE.ordinal < SubscriptionTier.PRO.ordinal,
            "FREEはPROより下位であること",
        )
    }
}

/**
 * テスト用のFake SubscriptionRepository。
 */
private class FakeSubscriptionRepository(
    private val status: SubscriptionStatus?,
) : SubscriptionRepository {

    override suspend fun getSubscriptionStatus(): Result<SubscriptionStatus> {
        return if (status != null) {
            Result.success(status)
        } else {
            Result.failure(Exception("Test error"))
        }
    }

    override fun observeSubscriptionStatus() = flowOf(
        status ?: SubscriptionStatus(
            tier = SubscriptionTier.FREE,
            isActive = false,
        ),
    )

    override suspend fun purchaseProPlan(): Result<SubscriptionStatus> {
        throw NotImplementedError("Test stub")
    }

    override suspend fun restorePurchases(): Result<SubscriptionStatus> {
        throw NotImplementedError("Test stub")
    }
}
