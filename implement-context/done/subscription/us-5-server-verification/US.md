# User Story: サーバーサイド検証API

> **Epic**: サブスクリプション基盤
> **作成日**: 2026-02-15

---

## User Story

開発者として、サーバー側でユーザーのサブスクリプション状態を検証したい。なぜなら、将来的にサーバー側でPro限定機能のアクセス制御を行う基盤が必要だから。

---

## ゴール

Ktorサーバーに `/api/subscription/status` エンドポイントを実装し、RevenueCat REST APIと連携してサブスクリプション状態を検証する。

---

## 依存

- US-1: ドメインモデル & Feature Gate基盤（SubscriptionStatus, SubscriptionTier）

---

## スコープ

- `/api/subscription/status` GETエンドポイント
- RevenueCat REST API連携（Subscribers API）
- デバイスIDによるサブスクリプション状態の照会
- レスポンスのドメインモデル変換

---

## 受け入れ条件

- `GET /api/subscription/status?deviceId={deviceId}` が正しいJSON応答を返すこと
- RevenueCat REST APIからサブスクリプション情報が正しく取得されること
- 未登録のデバイスIDに対してFreeプランのステータスが返されること
- エラー時に適切なHTTPステータスコードとエラーメッセージが返されること

---

## 次のアクション

`/develop` を実行して実装開始
