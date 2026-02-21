# 進捗管理: 動画詳細 & チャンネル動画APIエンドポイント

> **US**: `implement-context/api-proxy-server/us-2-video-api/US.md`
> **SPECIFICATION**: `server/src/main/kotlin/org/example/project/video/SPECIFICATION.md`
> **ブランチ**: `feature/api-proxy-us2-video-api`

---

## Server Layer

### ビルド設定
- [x] `gradle/libs.versions.toml` - `ktor-client-java` エントリ追加
- [x] `server/build.gradle.kts` - Ktor Client 依存追加（client-core, client-java, client-content-negotiation）

### エラーハンドリング
- [x] `StatusPages.kt` - `ExternalApiException`（502 Bad Gateway）追加
- [x] `StatusPages.kt` - `ServiceUnavailableException`（503 Service Unavailable）追加

### Service
- [x] `VideoService.kt` - Service interface定義（getVideoDetails, getChannelVideos）
- [x] `VideoServiceImpl.kt` - YouTube動画詳細取得（videos.list API呼び出し + マッパー）
- [x] `VideoServiceImpl.kt` - Twitch動画詳細取得（helix/videos API呼び出し + マッパー）
- [x] `VideoServiceImpl.kt` - YouTubeチャンネル動画取得（search.list → videos.list 2段階 + 日付フィルタ）
- [x] `VideoServiceImpl.kt` - Twitchチャンネル動画取得（helix/videos + サーバー側日付フィルタ）

### Routes
- [x] `VideoRoutes.kt` - `GET /api/videos/{id}` エンドポイント（パラメータバリデーション + Service呼び出し）
- [x] `VideoRoutes.kt` - `GET /api/channels/{id}/videos` エンドポイント（日付パラメータ含むバリデーション + Service呼び出し）

### エントリーポイント
- [x] `Application.kt` - videoRoutes 登録、HttpClient 生成・管理

### Server テスト
- [x] `VideoRoutesTest.kt` - 動画詳細エンドポイントテスト（正常系・異常系）
- [x] `VideoRoutesTest.kt` - チャンネル動画エンドポイントテスト（正常系・異常系）
- [x] `./gradlew :server:build` 成功
- [x] `./gradlew :server:test` 成功

---

## Integration

### 最終確認
- [x] `./gradlew test` 全テスト成功
- [x] SPECIFICATION.md の全ユーザーストーリーが実装済み

---

## メモ

実装中に気づいたこと、次回への申し送り事項などをここに記録。

- shared モジュールの既存データモデル・マッパーを再利用した
- Twitch API の日付フィルタリングはサーバー側で行う（API制約）
- `ktor-client-java` をバージョンカタログに追加（ハードコード回避）
- HttpClient のライフサイクル管理: `ApplicationStopPreparing` イベントでクローズ
