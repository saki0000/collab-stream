# User Story: RevenueCat SDK統合

> **Epic**: サブスクリプション基盤
> **作成日**: 2026-02-15

---

## User Story

ユーザーとして、アプリ内からProプランを購入・復元したい。なぜなら、プレミアム機能にアクセスするためにシームレスな課金体験が必要だから。

---

## ゴール

RevenueCat KMP SDKを導入し、Android/iOS両対応のアプリ内課金処理を実現する。

---

## 依存

- US-1: ドメインモデル & Feature Gate基盤（SubscriptionRepository, SubscriptionStatusインターフェース）
- US-2: 匿名認証（デバイスID）（RevenueCatのユーザー識別子として使用）

---

## スコープ

- `com.revenuecat.purchases:purchases-kmp-core` の導入
- RevenueCat SDKの初期化（デバイスIDをユーザー識別子として設定）
- `SubscriptionRepository` の実装（購入・復元・状態監視）
- `FeatureGate` の実装（サブスクリプション状態に基づく機能判定）

---

## 受け入れ条件

- RevenueCat SDKがAndroid/iOS両方で正常に初期化されること
- デバイスIDがRevenueCatのユーザーIDとして設定されること
- `purchaseProPlan()` でProプランの購入フローが起動すること
- `restorePurchases()` で過去の購入が正しく復元されること
- `observeSubscriptionStatus()` でリアルタイムに状態変更が通知されること

---

## 次のアクション

`/develop` を実行して実装開始
