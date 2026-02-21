# User Story: 共通基盤 & サーバー骨格構築

> **Epic**: APIプロキシサーバー移行
> **作成日**: 2026-02-11

---

## User Story

開発者として、Ktorサーバーの基本骨格とプラグイン設定を構築したい。なぜなら、後続のAPIエンドポイント実装の共通基盤が必要だから。

---

## ゴール

Ktorサーバーにプロキシとして機能するための共通プラグイン設定とヘルスチェックエンドポイントを構築し、shared内に共通APIレスポンスDTOを定義する

---

## 依存

- なし

---

## スコープ

- Ktorプラグイン設定（ContentNegotiation, StatusPages, CORS）
- ヘルスチェックエンドポイント（`GET /health`）
- shared内の共通APIレスポンスDTO（`ApiResponse<T>`等）
- エラーハンドリングの共通基盤（StatusPages設定）
- 環境変数からのAPIキー読み込み設定

---

## 受け入れ条件

- `GET /health` でサーバーの稼働状態を確認できる
- ContentNegotiationによるJSON シリアライゼーションが機能する
- StatusPagesで未処理例外が適切なエラーレスポンスに変換される
- CORSが適切に設定されている
- shared内に共通DTOが定義され、serverモジュールから参照できる
- 既存テストが壊れていない

---

## 次のアクション

`/develop` を実行して実装開始
