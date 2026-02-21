# 設計メモ: 動画詳細 & チャンネル動画APIエンドポイント

> **US**: `implement-context/api-proxy-server/us-2-video-api/US.md`
> **SPECIFICATION**: `server/src/main/kotlin/org/example/project/video/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Server | `server/build.gradle.kts` | Ktor Client依存追加（java engine, content-negotiation） |
| Server | `gradle/libs.versions.toml` | `ktor-client-java` エントリ追加 |
| Server | `server/.../service/VideoService.kt` | 外部API呼び出しService（interface + 実装） |
| Server | `server/.../routes/VideoRoutes.kt` | `GET /api/videos/{id}`, `GET /api/channels/{id}/videos` |
| Server | `server/.../plugins/StatusPages.kt` | `ExternalApiException`(502), `ServiceUnavailableException`(503) 追加 |
| Server | `server/.../Application.kt` | videoRoutes 登録 |
| Server Test | `server/src/test/.../routes/VideoRoutesTest.kt` | エンドポイントテスト |
| Server Test | `server/src/test/.../service/VideoServiceTest.kt` | サービスロジックテスト |

### 既存コードとの関連

- **shared データモデル再利用**: `YouTubeApiResponse`, `TwitchApiResponse`, `YouTubeVideoItem`, `TwitchVideoItem` をサーバー側でもHTTPクライアントのレスポンスデシリアライズに使用
- **shared マッパー再利用**: `YouTubeVideoMapper.toDomainModel()`, `TwitchVideoMapper.toDomainModel()` でドメインモデル変換
- **shared ドメインモデル**: `VideoDetails`（sealed class）、`VideoSnippet`、`LiveStreamingDetails`、`TwitchStreamInfo` をレスポンスに使用
- **US-1基盤**: `ApiKeyConfig`, `StatusPages`, `ContentNegotiation`, `ApiResponse` を活用
- **クライアント側Repository実装パターン**: `VideoSyncRepositoryImpl`, `TimelineSyncRepositoryImpl` のAPI呼び出しパターンをサーバー側に移植
- 準拠ADR: ADR-001, ADR-005

### アーキテクチャ

```
Routes (VideoRoutes)
  ↓ リクエストパース・バリデーション
Service (VideoService)
  ↓ 外部API呼び出し
Ktor HttpClient → YouTube API / Twitch API
  ↓ レスポンスデシリアライズ
shared/data/model (YouTubeApiResponse, TwitchApiResponse)
  ↓ マッピング
shared/data/mapper → shared/domain/model (VideoDetails)
  ↓ ApiResponse.Success でラップ
Routes → クライアント
```

---

## 技術的な注意点

- **Ktor Client Engine**: サーバー側では `ktor-client-java`（JDK HttpClient）を使用。クライアント側の `ktor-client-okhttp` / `ktor-client-darwin` とは異なる
- **shared マッパーのアクセス**: `server` モジュールは `projects.shared` に依存しているため、`data/mapper/` パッケージのマッパーを直接import可能
- **Twitch日付フィルタリング**: Twitch APIには日付範囲パラメータがないため、サーバー側で全件取得後にフィルタリングする（クライアント実装と同じパターン）
- **YouTube 2段階取得**: チャンネル動画は `search.list` → `videos.list` の2段階。`search.list` はquotaコストが高い（100 units/call）
- **HttpClient ライフサイクル**: `VideoServiceImpl` のコンストラクタで HttpClient を受け取り、Application終了時にcloseする
- **APIキー検証**: ルートハンドラ内で `ApiKeyConfig` のキー存在を確認し、未設定時は `ServiceUnavailableException` をthrow
- **テスト戦略**: ルートテストは `testApplication` で、外部API呼び出しはモックHttpClientでテスト
