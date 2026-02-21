# User Story: Docker化 & Cloud Runデプロイ CI/CD

> **Epic**: APIプロキシサーバー移行
> **作成日**: 2026-02-11

---

## User Story

開発者として、KtorサーバーをDocker化しCloud Runにデプロイしたい。なぜなら、本番環境でAPIプロキシサーバーを安定稼働させるためのインフラ基盤が必要だから。

---

## ゴール

Ktorサーバーのコンテナ化とGitHub Actions経由でのCloud Run自動デプロイパイプラインを構築する

---

## 依存

- US-1: 共通基盤 & サーバー骨格構築（US-2, US-3と並行可能）

---

## スコープ

- Dockerfile作成（マルチステージビルド）
- docker-compose.yml（ローカル開発用）
- GitHub Actionsデプロイワークフロー（Cloud Run）
- 環境変数（APIキー等）のシークレット管理設定
- ヘルスチェックによるコンテナ監視設定

---

## 受け入れ条件

- `docker build` でサーバーイメージがビルドできる
- `docker-compose up` でローカル環境が起動する
- mainブランチへのマージでCloud Runに自動デプロイされる
- 環境変数（APIキー）がCloud Runのシークレットとして管理されている
- ヘルスチェックエンドポイントによるコンテナ監視が機能する
- デプロイ後にヘルスチェックが成功する

---

## 次のアクション

`/develop` を実行して実装開始
