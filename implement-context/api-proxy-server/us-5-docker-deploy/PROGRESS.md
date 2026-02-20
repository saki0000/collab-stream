# 進捗管理: Docker化 & Cloud Runデプロイ CI/CD

> **US**: api-proxy-server/us-5-docker-deploy
> **ブランチ**: `feature/us5-docker-deploy`

---

## Server Layer

### PORT環境変数対応
- [x] `Application.kt` - PORT環境変数からポート番号を読み取り（Cloud Run対応）

### Server テスト
- [x] `./gradlew :server:build` 成功
- [x] `./gradlew :server:test` 成功

---

## Infrastructure

### Docker
- [x] `Dockerfile` - マルチステージビルド（build + runtime）
- [x] `.dockerignore` - 不要ファイル除外
- [x] `docker-compose.yml` - ローカル開発用

### CI/CD
- [x] `.github/workflows/deploy-cloud-run.yml` - Cloud Runデプロイワークフロー

---

## Integration

### 最終確認
- [x] `./gradlew :server:test` 全テスト成功
- [ ] `docker build` でイメージビルド成功（ローカル確認）

---

## メモ

- Cloud Run URLはデプロイ後に確定する。クライアント側のSERVER_BASE_URL更新は別タスクで対応。
- GCPリソースのセットアップ（プロジェクト作成、API有効化、WIF設定）はユーザーが手動で行う。
- `.dockerignore` から `testing/` の除外を取り消した（settings.gradle.kts が参照するため）
