# 進捗管理: クライアント側Repository移行

> **US**: `implement-context/api-proxy-server/us-4-client-migration/US.md`
> **SPECIFICATION**: `feature/api_proxy_migration/SPECIFICATION.md`
> **ブランチ**: `feature/api-proxy-us4-client-migration`

---

## Shared Layer

### サーバーAPI基盤
- [x] `Constants.kt` - サーバーベースURL定義追加（expect/actual でプラットフォーム別対応）
- [x] `ApiResponseHandler.kt` - ApiResponse デシリアライズ共通ヘルパー作成

### Repository 書き換え
- [x] `VideoSyncRepositoryImpl.kt` - サーバーAPI経由に書き換え（`GET /api/videos/{id}`）
- [x] `TimelineSyncRepositoryImpl.kt` - サーバーAPI経由に書き換え（`GET /api/videos/{id}` + `GET /api/channels/{id}/videos`）
- [x] `VideoSearchRepositoryImpl.kt` - DataSource依存除去、サーバーAPI直接呼び出し（`GET /api/search/videos` + `GET /api/search/channels`）
- [x] `VideoSearchRepository.kt` - `searchChannels()` メソッド追加（Interface）

### UseCase 修正
- [x] `ChannelSearchUseCase.kt` - DataSource依存 → VideoSearchRepository依存に変更

### DataSource 削除
- [x] `YouTubeSearchDataSourceImpl.kt` - 削除
- [x] `TwitchSearchDataSourceImpl.kt` - 削除
- [x] `YouTubeSearchDataSource.kt` - インターフェース削除
- [x] `TwitchSearchDataSource.kt` - インターフェース削除

### BuildKonfig 除去
- [x] `shared/build.gradle.kts` - API_KEY, TWITCH_API_KEY, TWITCH_CLIENT_ID の定義除去

### DI 更新
- [x] `SharedModule.kt` - DataSource登録削除、Repository/UseCase 依存関係更新

### Shared テスト
- [x] `ChannelSearchUseCaseTest.kt` - FakeVideoSearchRepository ベースに書き換え
- [x] `./gradlew :shared:compileKotlinJvm :shared:jvmTest` 成功

---

## ComposeApp Layer

（UI変更なし - 既存の画面は Repository インターフェースに依存しているため影響なし）

- [x] ComposeApp テスト修正（DataSource参照 → VideoSearchRepository参照に変更）
- [x] `./gradlew :composeApp:compileDebugKotlin` 成功
- [x] `./gradlew :composeApp:test` 成功

---

## Integration

### 最終確認
- [x] `./gradlew :server:build :server:test` 成功
- [x] `./gradlew :shared:jvmTest` 成功
- [x] `./gradlew :composeApp:test` 成功
- [x] SPECIFICATION.md の全ビジネスルールが実装済み
- [x] BuildKonfig に YouTube / Twitch APIキーが含まれていないこと

---

## メモ

- data/mapper/ と data/model/ の外部APIモデルは、サーバーモジュールが shared に依存しているため保持
- プラットフォーム別サーバーURL は将来 Cloud Run 移行時に環境変数化が必要
- iOS native テストの `expect runTest` の actual 未実装は既存の問題（本 US とは無関係）
