package org.example.project.feature.subscription

/**
 * サブスクリプション管理画面のユーザーIntent定義。
 *
 * MVI アーキテクチャパターンに従う。
 * ユーザーの操作はすべてこのsealed interfaceを通じてViewModelに伝達される。
 *
 * Feature: サブスクリプション管理 (US-4)
 * Specification: feature/subscription/SPECIFICATION.md
 */
sealed interface SubscriptionIntent {
    /**
     * サブスクリプション状態を読み込む。
     * 画面初回表示時に呼ばれる。
     */
    data object LoadSubscription : SubscriptionIntent

    /**
     * Proプランを購入する。
     * ストアの購入フローを起動する。
     */
    data object PurchaseProPlan : SubscriptionIntent

    /**
     * 過去の購入を復元する。
     * 機種変更などで再インストールした場合に使用する。
     */
    data object RestorePurchases : SubscriptionIntent

    /**
     * エラー時にデータ再読み込みを実行する。
     */
    data object Retry : SubscriptionIntent

    /**
     * 前の画面に戻る。
     */
    data object NavigateBack : SubscriptionIntent
}

/**
 * サブスクリプション管理画面のSideEffect定義。
 *
 * 一度だけ実行されるイベント（Snackbar、Navigation等）。
 * ViewModelからUIへの一方向通知に使用する。
 */
sealed interface SubscriptionSideEffect {
    /**
     * エラーメッセージをSnackbarで表示する。
     *
     * @property message 表示するエラーメッセージ
     */
    data class ShowError(val message: String) : SubscriptionSideEffect

    /**
     * 成功メッセージをSnackbarで表示する。
     *
     * @property message 表示する成功メッセージ
     */
    data class ShowSuccess(val message: String) : SubscriptionSideEffect

    /**
     * 前の画面に戻る。
     */
    data object NavigateBack : SubscriptionSideEffect
}
