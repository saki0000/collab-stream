# 進捗管理: サブスクリプション管理UI

> **US**: implement-context/subscription/us-4-subscription-ui/US.md
> **SPECIFICATION**: `feature/subscription/SPECIFICATION.md`
> **ブランチ**: `feature/subscription-ui`

---

## ComposeApp Layer

### State / Intent
- [x] `SubscriptionUiState.kt` - 画面状態定義（ScreenState: Loading/FreePlan/ProPlan/Error + isPurchasing/isRestoring）
- [x] `SubscriptionIntent.kt` - ユーザー操作定義（LoadSubscription/PurchaseProPlan/RestorePurchases/Retry/NavigateBack）+ SideEffect

### ViewModel
- [x] `SubscriptionViewModel.kt` - MVI パターン（SubscriptionRepository依存、状態遷移ロジック）

### UI（4層構造）
- [x] `ui/SubscriptionContainer.kt` - Container層（ViewModel接続、SideEffect処理、Navigation）
- [x] `ui/SubscriptionScreen.kt` - Screen層（状態別レイアウト切替: Loading/Free/Pro/Error + Preview 6種）
- [x] `ui/SubscriptionContent.kt` - Content層（FreePlanContent + ProPlanContent + 内部コンポーネント + Preview 6種）

### Navigation
- [x] `core/navigation/Routes.kt` に `SubscriptionRoute` 追加
- [x] `core/navigation/NavGraph.kt` にcomposable登録 + Container接続
- [x] `feature/archive_home/ui/ArchiveHomeScreen.kt` にTopAppBar追加（設定アイコン）
- [x] `feature/archive_home/ui/ArchiveHomeContainer.kt` に `onNavigateToSubscription` コールバック追加

### DI
- [x] `core/di/AppModule.kt` に `SubscriptionViewModel` 登録

### ComposeApp テスト
- [x] `SubscriptionViewModelTest.kt` - ViewModel テスト（初期状態/読み込み/購入/復元/エラー/ナビゲーション/二重処理防止: 14テスト）
- [x] `./gradlew :composeApp:test` 成功

---

## Integration

### 最終確認
- [x] `./gradlew test` 全テスト成功
- [x] SPECIFICATION.md の全ユーザーストーリーが実装済み

---

## メモ

- Shared Layer の変更なし（SubscriptionRepository / ドメインモデルはUS-1〜3で実装済み）
- ArchiveHomeScreenのTopAppBarに設定（歯車）アイコンを追加してSubscription画面への導線を確保
