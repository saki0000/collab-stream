# 設計メモ: RevenueCat SDK統合

> **US**: implement-context/subscription/us-3-revenuecat-integration/US.md
> **SPECIFICATION**: `feature/subscription/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Gradle | `gradle/libs.versions.toml` | RevenueCat KMP SDK依存追加 |
| Gradle | `shared/build.gradle.kts` | purchases-kmp-core依存追加 |
| Shared Data | `shared/.../data/repository/SubscriptionRepositoryImpl.kt` | RevenueCat SDKラッパー実装 |
| Shared Data | `shared/.../data/repository/FeatureGateImpl.kt` | Feature判定ロジック実装 |
| Shared Data | `shared/.../data/mapper/SubscriptionMapper.kt` | CustomerInfo → SubscriptionStatus変換 |
| Shared Platform | `shared/src/androidMain/.../RevenueCatConfig.kt` | Android用APIキー提供 |
| Shared Platform | `shared/src/iosMain/.../RevenueCatConfig.kt` | iOS用APIキー提供 |
| Shared DI | `shared/.../di/DatabaseModule.kt` | SubscriptionRepository, FeatureGate登録 |
| Shared Test | `shared/src/commonTest/.../FeatureGateImplTest.kt` | FeatureGateテスト |

### 既存コードとの関連

- **依存US-1**: `SubscriptionRepository`インターフェース、`SubscriptionStatus`、`SubscriptionTier`、`Feature`、`FeatureGate`インターフェース（全て定義済み）
- **依存US-2**: `UserRepository`（`getDeviceId()`でデバイスID取得 → RevenueCatのappUserIdに使用）
- 参考実装: `UserRepositoryImpl`（Mutex排他制御、Double-Checked Lockingパターン）
- 準拠ADR: ADR-001（Android Architecture）

---

## 技術的な設計

### RevenueCat SDK初期化

SubscriptionRepositoryImpl内で遅延初期化（Double-Checked Locking）を行う。

```
アプリ起動 → Koin初期化 → SubscriptionRepository注入
→ 初回API呼び出し時にRevenueCat SDK初期化
  → UserRepository.getDeviceId()でデバイスID取得
  → Purchases.configure(apiKey, appUserId = deviceId)
```

**理由**: Koinの初期化完了後にUserRepository経由でデバイスIDを取得する必要があるため、コンストラクタではなく遅延初期化とする。

### APIキー管理

expect/actualパターンで`getRevenueCatApiKey(): String`を定義。

- Android: BuildKonfigまたはBuildConfigから取得
- iOS: Bundleまたはハードコードから取得
- **開発時**: プレースホルダー値を使用（RevenueCatダッシュボード設定は別途）

### CustomerInfo → SubscriptionStatus マッピング

RevenueCatの`CustomerInfo`からドメインモデルへの変換:

| CustomerInfo | SubscriptionStatus | 変換ルール |
|---|---|---|
| `entitlements["pro"]?.isActive` | `tier` | true→PRO, false→FREE |
| `entitlements["pro"]?.isActive` | `isActive` | そのまま |
| `entitlements["pro"]?.expirationDate` | `expiresAtMillis` | Date→Long（null許容） |
| `entitlements["pro"]?.willRenew` | `willRenew` | そのまま（デフォルトfalse） |

**Entitlement ID**: RevenueCatダッシュボードで`"pro"`として設定想定

### 購入フロー

```
purchaseProPlan()
  → ensureConfigured()
  → Purchases.sharedInstance.awaitOfferings()
  → offerings.current?.availablePackages から対象パッケージ取得
  → Purchases.sharedInstance.awaitPurchase(package)
  → CustomerInfo → SubscriptionStatus変換
```

### 状態監視（Flow）

```
observeSubscriptionStatus()
  → callbackFlow {
      → ensureConfigured()
      → UpdatedCustomerInfoListener設定
      → 初回値emit
      → awaitClose { listener解除 }
    }
```

---

## 技術的な注意点

- **RevenueCat KMP SDK バージョン**: `2.5.1+17.33.1`（purchases-kmp-core）
- **Mutex排他制御**: SDK初期化にMutex使用（UserRepositoryImplのパターンに準拠）
- **テスト戦略**: RevenueCat SDKのモック化が困難なため、FeatureGateImplのテストに注力。SubscriptionRepositoryImplは統合テスト対象
- **Entitlement ID**: `"pro"` をconst valで一元管理
- **エラーハンドリング**: `Result<T>`パターンで統一（既存パターンに準拠）
