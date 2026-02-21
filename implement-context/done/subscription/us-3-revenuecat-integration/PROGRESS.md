# 進捗管理: RevenueCat SDK統合

> **US**: implement-context/subscription/us-3-revenuecat-integration/US.md
> **SPECIFICATION**: `feature/subscription/SPECIFICATION.md`
> **ブランチ**: `feature/subscription-us3-revenuecat-integration`

---

## Shared Layer

### Gradle依存追加
- [x] `gradle/libs.versions.toml` - RevenueCat KMP SDK `2.5.1+17.33.1` 追加
- [x] `shared/build.gradle.kts` - androidMain/iosMainにpurchases-kmp-core依存追加

### RevenueCat APIキー設定（expect/actual）
- [x] `RevenueCatConfig.kt`（common） - `expect fun getRevenueCatApiKey(): String`
- [x] `RevenueCatConfig.android.kt` - Android用APIキー実装（プレースホルダー）
- [x] `RevenueCatConfig.ios.kt` - iOS用APIキー実装（プレースホルダー）
- [x] `RevenueCatConfig.jvm.kt` - JVM用APIキー実装（スタブ）

### Repository実装
- [x] `SubscriptionMapper.kt`（Android/iOS各） - CustomerInfo → SubscriptionStatus変換
- [x] `SubscriptionRepositoryImpl.kt`（Android/iOS各） - RevenueCat SDKラッパー
- [x] `SubscriptionRepositoryFactory.kt`（expect/actual） - プラットフォーム固有インスタンス生成
- [x] JVM用スタブ実装（`StubSubscriptionRepository`）

### FeatureGate実装
- [x] `FeatureGateImpl.kt` - サブスクリプション状態に基づく機能判定

### Shared テスト
- [x] `FeatureGateImplTest.kt` - FeatureGate判定ロジック・プラン監視テスト
- [x] `./gradlew :shared:jvmTest :shared:testDebugUnitTest` 成功

---

## Integration

### DI（Koin）
- [x] `DatabaseModule.kt` - SubscriptionRepository, FeatureGate登録

### 最終確認
- [x] `./gradlew :shared:jvmTest :shared:testDebugUnitTest :composeApp:testDebugUnitTest` 全テスト成功
- [x] SPECIFICATION.md の受け入れ条件が実装済み

---

## メモ

- RevenueCat APIキーはプレースホルダー値で実装。本番キーは別途RevenueCatダッシュボード設定後に差し替え
- RevenueCat KMP SDKはJVM非対応のため、expect/actualパターンでプラットフォーム分離（JVMはスタブ）
- Android: `UpdatedCustomerInfoDelegate`（SDK提供の便利クラス）使用
- iOS: `PurchasesDelegate`インターフェースを直接実装（`onPurchasePromoProduct`は未対応）
- `kotlin.time.Instant`は`@OptIn(ExperimentalTime::class)`が必要（SubscriptionMapper）
- TestUtils.kt のiOS用 `runTest` actual未実装は既存問題（US-3スコープ外）
