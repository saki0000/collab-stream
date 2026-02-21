# 設計メモ: Docker化 & Cloud Runデプロイ CI/CD

> **US**: api-proxy-server/us-5-docker-deploy
> **SPECIFICATION**: UIなし（インフラ構成）

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Server | `server/src/.../Application.kt` | PORT環境変数対応 |
| Infrastructure | `Dockerfile` | マルチステージビルド（プロジェクトルート配置） |
| Infrastructure | `.dockerignore` | ビルドコンテキスト最適化 |
| Infrastructure | `docker-compose.yml` | ローカル開発用 |
| CI/CD | `.github/workflows/deploy-cloud-run.yml` | Cloud Runデプロイワークフロー |

### Dockerfile設計

プロジェクトルートに配置（serverモジュールがsharedモジュールに依存するため）。

```
Stage 1: Build
  - gradle:8-jdk17 ベース
  - ./gradlew :server:buildFatJar でFat JAR生成

Stage 2: Runtime
  - eclipse-temurin:17-jre-alpine ベース（軽量）
  - Fat JARをコピーして実行
```

### Cloud Run対応

- Cloud Runは `PORT` 環境変数でリスニングポートを指定する
- Application.kt で `System.getenv("PORT")` を読み取り、未設定時は8080にフォールバック

### GitHub Actionsワークフロー

- トリガー: mainブランチへのpush（server/shared変更時）+ 手動実行
- 認証: Workload Identity Federation（推奨）
- ビルド: Docker → Artifact Registry → Cloud Run

### 必要なGCPリソース（手動セットアップ）

1. GCPプロジェクト
2. API有効化: Cloud Run, Artifact Registry
3. Artifact Registryリポジトリ作成
4. Workload Identity Federation設定（GitHub Actions用）
5. GitHub Secrets設定

### 必要なGitHub Secrets

| Secret名 | 内容 |
|----------|------|
| `GCP_PROJECT_ID` | GCPプロジェクトID |
| `GCP_REGION` | デプロイリージョン（例: asia-northeast1） |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | WIF プロバイダID |
| `GCP_SERVICE_ACCOUNT` | サービスアカウントメール |
| `YOUTUBE_API_KEY` | YouTube API Key |
| `TWITCH_CLIENT_ID` | Twitch Client ID |
| `TWITCH_CLIENT_SECRET` | Twitch Client Secret |

---

## 既存コードとの関連

- `HealthRoutes.kt`: `/health` エンドポイント → Cloud Runのヘルスチェックに利用
- `ApiKeyConfig.kt`: 環境変数からAPIキー読み込み → Cloud Runの環境変数設定で対応済み
- `Constants.kt`: `SERVER_PORT = 8080` → PORT環境変数で上書き可能に
- `.github/workflows/ci.yml`: 既存CIワークフロー → パターン参考

---

## 技術的な注意点

- Dockerfileはプロジェクトルートに配置（マルチモジュールGradleプロジェクトのため）
- `.dockerignore` でiOSApp、ビルドキャッシュ等を除外し、ビルドコンテキストを軽量化
- Cloud RunのコンテナはステートレスなのでAPIプロキシとの相性が良い
- Gradle Daemonはコンテナビルド内では使わない（`--no-daemon`）
