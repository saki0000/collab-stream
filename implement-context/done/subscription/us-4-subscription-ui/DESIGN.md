# 設計メモ: サブスクリプション管理UI

> **US**: implement-context/subscription/us-4-subscription-ui/US.md
> **SPECIFICATION**: `feature/subscription/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| ComposeApp | `feature/subscription/SubscriptionUiState.kt` | 画面状態定義（Loading/Free/Pro/Purchasing/Restoring/Error） |
| ComposeApp | `feature/subscription/SubscriptionIntent.kt` | Intent + SideEffect 定義 |
| ComposeApp | `feature/subscription/SubscriptionViewModel.kt` | MVI ViewModel（SubscriptionRepository依存） |
| ComposeApp | `feature/subscription/ui/SubscriptionContainer.kt` | Stateful層（ViewModel接続、SideEffect処理） |
| ComposeApp | `feature/subscription/ui/SubscriptionScreen.kt` | Stateless層（状態別レイアウト切替 + Preview） |
| ComposeApp | `feature/subscription/ui/SubscriptionContent.kt` | Freeプラン/Proプラン表示のContent |
| ComposeApp | `core/navigation/Routes.kt` | `SubscriptionRoute` 追加 |
| ComposeApp | `core/navigation/NavGraph.kt` | composable登録 + ArchiveHomeからの遷移 |
| ComposeApp | `feature/archive_home/ui/ArchiveHomeScreen.kt` | TopAppBar追加（設定アイコン → Subscription画面遷移） |
| ComposeApp | `feature/archive_home/ui/ArchiveHomeContainer.kt` | `onNavigateToSubscription` コールバック追加 |
| ComposeApp | `core/di/AppModule.kt` | SubscriptionViewModel登録 |
| ComposeApp Test | `feature/subscription/SubscriptionViewModelTest.kt` | ViewModel テスト |

**Shared Layer**: 変更なし（SubscriptionRepository / FeatureGate / ドメインモデルはUS-1〜3で実装済み）

### 既存コードとの関連

- **参考実装**: `feature/archive_home/`（MVI + 4層Component構造の最新実装例）
- **SubscriptionRepository**: `shared/.../domain/repository/SubscriptionRepository.kt`（`getSubscriptionStatus()`, `purchaseProPlan()`, `restorePurchases()`, `observeSubscriptionStatus()`）
- **SubscriptionStatus**: `shared/.../domain/model/SubscriptionStatus.kt`（`tier`, `isActive`, `expiresAtMillis`, `willRenew`）
- **SubscriptionTier**: `shared/.../domain/model/SubscriptionTier.kt`（`FREE`, `PRO`）
- **DI**: `DatabaseModule` で `SubscriptionRepository` は登録済み

### 準拠ADR

- ADR-001: Android Architecture（UI Layer → Data Layer直接）
- ADR-002: MVI パターン（Intent → ViewModel → State → View）
- ADR-003: 4層Component構造（Container → Screen → Content → Component）

---

## 設計詳細

### UiState設計

```kotlin
data class SubscriptionUiState(
    val screenState: ScreenState = ScreenState.Loading,
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
)

sealed interface ScreenState {
    data object Loading : ScreenState
    data class FreePlan(/* no extra fields */) : ScreenState
    data class ProPlan(
        val expiresAtMillis: Long?,
        val willRenew: Boolean,
    ) : ScreenState
    data class Error(val message: String) : ScreenState
}
```

### Intent設計

```kotlin
sealed interface SubscriptionIntent {
    data object LoadSubscription : SubscriptionIntent
    data object PurchaseProPlan : SubscriptionIntent
    data object RestorePurchases : SubscriptionIntent
    data object Retry : SubscriptionIntent
    data object NavigateBack : SubscriptionIntent
}

sealed interface SubscriptionSideEffect {
    data class ShowError(val message: String) : SubscriptionSideEffect
    data class ShowSuccess(val message: String) : SubscriptionSideEffect
    data object NavigateBack : SubscriptionSideEffect
}
```

### ナビゲーション設計

- `ArchiveHomeScreen` に TopAppBar を追加し、設定（歯車）アイコンを配置
- 歯車アイコンタップ → `SubscriptionRoute` へ navigate
- `SubscriptionScreen` に TopAppBar + 戻るボタン
- 戻るボタン → `NavigateBack` SideEffect → `navController.popBackStack()`

---

## 技術的な注意点

- `purchaseProPlan()` はRevenueCat SDKのネイティブ購入UIを起動するため、UIスレッドから呼ばれる必要がある（viewModelScope.launch でOK）
- `expiresAtMillis` の日付フォーマットは `kotlinx.datetime` を使用（SPECIFICATION: 「YYYY年MM月DD日まで」形式）
- JVMプラットフォーム（デスクトップ/サーバー）ではStub実装のため、常にFREE表示になる
- `isPurchasing` / `isRestoring` フラグで購入/復元処理中のUIブロッキングを管理
