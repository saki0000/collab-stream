package org.example.project.feature.subscription

/**
 * サブスクリプション管理画面のUI状態。
 *
 * MVI アーキテクチャパターンに従う。
 *
 * @property screenState 画面の主要な表示状態（Loading/FreePlan/ProPlan/Error）
 * @property isPurchasing 購入処理中フラグ（UIブロッキング用）
 * @property isRestoring 復元処理中フラグ（UIブロッキング用）
 *
 * Feature: サブスクリプション管理 (US-4)
 * Specification: feature/subscription/SPECIFICATION.md
 */
data class SubscriptionUiState(
    val screenState: ScreenState = ScreenState.Loading,
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
)

/**
 * 画面の主要な表示状態を表すsealed interface。
 *
 * 状態遷移:
 * Loading → FreePlan | ProPlan | Error
 * FreePlan → (購入/復元処理後) → ProPlan | Error
 * Error → (リトライ後) → Loading
 */
sealed interface ScreenState {
    /**
     * データ読み込み中の状態。
     * ローディングインジケーターを表示する。
     */
    data object Loading : ScreenState

    /**
     * Freeプランの状態。
     * 「Proにアップグレード」ボタンと「購入を復元」ボタンを表示する。
     */
    data object FreePlan : ScreenState

    /**
     * Proプランの状態。
     * 有効期限と自動更新状態を表示する。
     *
     * @property expiresAtMillis 有効期限（エポックミリ秒）。nullの場合は表示しない
     * @property willRenew 自動更新が有効かどうか
     */
    data class ProPlan(
        val expiresAtMillis: Long?,
        val willRenew: Boolean,
    ) : ScreenState

    /**
     * エラーが発生した状態。
     * エラーメッセージとリトライボタンを表示する。
     *
     * @property message エラーメッセージ
     */
    data class Error(val message: String) : ScreenState
}
