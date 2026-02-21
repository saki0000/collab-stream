package org.example.project.feature.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.model.SubscriptionTier
import org.example.project.domain.repository.SubscriptionRepository

/**
 * サブスクリプション管理画面のViewModel。
 *
 * MVI アーキテクチャパターンに従う。
 * [SubscriptionRepository] を通じてサブスクリプション状態の取得・購入・復元を行う。
 *
 * Feature: サブスクリプション管理 (US-4)
 * Specification: feature/subscription/SPECIFICATION.md
 */
class SubscriptionViewModel(
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<SubscriptionSideEffect>()
    val sideEffect: SharedFlow<SubscriptionSideEffect> = _sideEffect.asSharedFlow()

    /**
     * ユーザーIntentを処理する。
     */
    fun handleIntent(intent: SubscriptionIntent) {
        when (intent) {
            SubscriptionIntent.LoadSubscription -> loadSubscription()
            SubscriptionIntent.PurchaseProPlan -> purchaseProPlan()
            SubscriptionIntent.RestorePurchases -> restorePurchases()
            SubscriptionIntent.Retry -> loadSubscription()
            SubscriptionIntent.NavigateBack -> navigateBack()
        }
    }

    // ============================================
    // サブスクリプション状態の読み込み
    // ============================================

    /**
     * サブスクリプション状態を読み込む。
     */
    private fun loadSubscription() {
        _uiState.value = _uiState.value.copy(
            screenState = ScreenState.Loading,
        )

        viewModelScope.launch {
            subscriptionRepository.getSubscriptionStatus().fold(
                onSuccess = { status ->
                    val screenState = when (status.tier) {
                        SubscriptionTier.PRO -> ScreenState.ProPlan(
                            expiresAtMillis = status.expiresAtMillis,
                            willRenew = status.willRenew,
                        )
                        SubscriptionTier.FREE -> ScreenState.FreePlan
                    }
                    _uiState.value = _uiState.value.copy(screenState = screenState)
                },
                onFailure = { throwable ->
                    _uiState.value = _uiState.value.copy(
                        screenState = ScreenState.Error(
                            message = throwable.message ?: "サブスクリプション情報の取得に失敗しました",
                        ),
                    )
                },
            )
        }
    }

    // ============================================
    // 購入処理
    // ============================================

    /**
     * Proプランを購入する。
     * 購入中は isPurchasing フラグで UI をブロッキングする。
     */
    private fun purchaseProPlan() {
        // 既に処理中の場合は無視する
        if (_uiState.value.isPurchasing || _uiState.value.isRestoring) return

        _uiState.value = _uiState.value.copy(isPurchasing = true)

        viewModelScope.launch {
            subscriptionRepository.purchaseProPlan().fold(
                onSuccess = { status ->
                    val screenState = when (status.tier) {
                        SubscriptionTier.PRO -> {
                            _sideEffect.emit(SubscriptionSideEffect.ShowSuccess("Proプランへのアップグレードが完了しました"))
                            ScreenState.ProPlan(
                                expiresAtMillis = status.expiresAtMillis,
                                willRenew = status.willRenew,
                            )
                        }
                        SubscriptionTier.FREE -> {
                            // 購入キャンセル時はFreeプランに戻る
                            ScreenState.FreePlan
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        screenState = screenState,
                        isPurchasing = false,
                    )
                },
                onFailure = { throwable ->
                    _uiState.value = _uiState.value.copy(
                        screenState = ScreenState.Error(
                            message = throwable.message ?: "購入処理に失敗しました",
                        ),
                        isPurchasing = false,
                    )
                    _sideEffect.emit(
                        SubscriptionSideEffect.ShowError(throwable.message ?: "購入処理に失敗しました"),
                    )
                },
            )
        }
    }

    // ============================================
    // 購入復元処理
    // ============================================

    /**
     * 過去の購入を復元する。
     * 復元中は isRestoring フラグで UI をブロッキングする。
     */
    private fun restorePurchases() {
        // 既に処理中の場合は無視する
        if (_uiState.value.isPurchasing || _uiState.value.isRestoring) return

        _uiState.value = _uiState.value.copy(isRestoring = true)

        viewModelScope.launch {
            subscriptionRepository.restorePurchases().fold(
                onSuccess = { status ->
                    val screenState = when (status.tier) {
                        SubscriptionTier.PRO -> {
                            _sideEffect.emit(SubscriptionSideEffect.ShowSuccess("購入の復元が完了しました"))
                            ScreenState.ProPlan(
                                expiresAtMillis = status.expiresAtMillis,
                                willRenew = status.willRenew,
                            )
                        }
                        SubscriptionTier.FREE -> {
                            // 復元対象なし → Freeプランのまま
                            _sideEffect.emit(SubscriptionSideEffect.ShowSuccess("復元できる購入がありませんでした"))
                            ScreenState.FreePlan
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        screenState = screenState,
                        isRestoring = false,
                    )
                },
                onFailure = { throwable ->
                    _uiState.value = _uiState.value.copy(
                        screenState = ScreenState.Error(
                            message = throwable.message ?: "購入の復元に失敗しました",
                        ),
                        isRestoring = false,
                    )
                    _sideEffect.emit(
                        SubscriptionSideEffect.ShowError(throwable.message ?: "購入の復元に失敗しました"),
                    )
                },
            )
        }
    }

    // ============================================
    // ナビゲーション
    // ============================================

    /**
     * 前の画面に戻るSideEffectを発行する。
     */
    private fun navigateBack() {
        viewModelScope.launch {
            _sideEffect.emit(SubscriptionSideEffect.NavigateBack)
        }
    }
}
