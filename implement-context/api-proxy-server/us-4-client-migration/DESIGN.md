# 設計メモ: クライアント側Repository移行

> **US**: `implement-context/api-proxy-server/us-4-client-migration/US.md`
> **SPECIFICATION**: `feature/api_proxy_migration/SPECIFICATION.md`

---

## 実装方針

### 概要

クライアントの Repository/DataSource 実装を、直接外部API（YouTube/Twitch）呼び出しからサーバーAPI（`/api/*`）呼び出しに書き換える。サーバーはドメインモデル形状のレスポンスを `ApiResponse<T>` で返すため、クライアント側のマッピングロジックが大幅に簡素化される。

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Shared Data | `data/repository/VideoSyncRepositoryImpl.kt` | サーバーAPI経由に書き換え（BuildKonfig参照除去） |
| Shared Data | `data/repository/TimelineSyncRepositoryImpl.kt` | サーバーAPI経由に書き換え（BuildKonfig参照除去） |
| Shared Data | `data/repository/VideoSearchRepositoryImpl.kt` | DataSource依存を除去し、サーバーAPI直接呼び出しに変更 |
| Shared Data | `data/datasource/YouTubeSearchDataSourceImpl.kt` | **削除**（サーバー移行により不要） |
| Shared Data | `data/datasource/TwitchSearchDataSourceImpl.kt` | **削除**（サーバー移行により不要） |
| Shared Data | `data/datasource/YouTubeSearchDataSource.kt` | **削除**（インターフェース不要） |
| Shared Data | `data/datasource/TwitchSearchDataSource.kt` | **削除**（インターフェース不要） |
| Shared Domain | `domain/repository/VideoSearchRepository.kt` | `searchChannels()` メソッド追加 |
| Shared Domain | `domain/usecase/ChannelSearchUseCase.kt` | DataSource依存 → VideoSearchRepository依存に変更 |
| Shared Config | `Constants.kt` | サーバーベースURL定義追加 |
| Shared Build | `shared/build.gradle.kts` | BuildKonfig からAPIキー定義を除去 |
| Shared DI | `di/SharedModule.kt` | DataSource登録削除、Repository依存関係更新 |

### 既存コードとの関連

- **参考実装**: サーバー側 `server/src/main/kotlin/.../routes/VideoRoutes.kt`, `SearchRoutes.kt`
- **共有モデル**: `domain/model/ApiResponse.kt`（サーバー・クライアント共通）
- **準拠ADR**: ADR-001（Android Architecture）, ADR-005（段階的APIセキュリティ Phase 2）

---

## 設計詳細

### 1. サーバーURL設定

`Constants.kt` にサーバーベースURLを追加。プラットフォーム別の考慮：
- **Android エミュレータ**: `http://10.0.2.2:8080`（localhost はホストマシンを指さない）
- **iOS シミュレータ / デスクトップ**: `http://localhost:8080`

開発段階では `expect/actual` または BuildKonfig でプラットフォーム別設定を管理。

### 2. ApiResponse ハンドリング

サーバーは `ApiResponse.Success<T>` 形式でレスポンスを返す。クライアント側で共通ヘルパーを用意：

```kotlin
// HttpResponse からドメインモデルを抽出する共通処理
suspend inline fun <reified T> HttpResponse.toResult(): Result<T> {
    return if (status.isSuccess()) {
        val apiResponse: ApiResponse.Success<T> = body()
        Result.success(apiResponse.data)
    } else {
        val error: ApiResponse.Error = body()
        Result.failure(RuntimeException("API error (${error.code}): ${error.message}"))
    }
}
```

### 3. Repository 書き換え戦略

**VideoSyncRepositoryImpl**:
- `getVideoDetails()` → `GET /api/videos/{id}?service=youtube|twitch`
- サーバーが `YouTubeVideoDetails` / `TwitchVideoDetails` を返すため、Mapper不要

**TimelineSyncRepositoryImpl**:
- `getVideoDetails()` → 同上
- `getChannelVideos()` → `GET /api/channels/{id}/videos?service=...&startDate=...&endDate=...`
- サーバーが日付フィルタリングを処理するため、クライアント側フィルタ不要

**VideoSearchRepositoryImpl**:
- DataSource 依存を除去
- `searchVideos()` → `GET /api/search/videos?q=...` (service未指定で両方検索)
- `searchVideosByService()` → `GET /api/search/videos?q=...&service=...`
- 新規: `searchChannels()` → `GET /api/search/channels?q=...&service=...`
- サーバーが `SearchResponse` / `ChannelSearchResponse` を直接返す

### 4. ChannelSearchUseCase 依存関係変更

現在 DataSource に直接依存しているため、VideoSearchRepository 経由に変更：

```
Before: ChannelSearchUseCase → YouTubeSearchDataSource / TwitchSearchDataSource
After:  ChannelSearchUseCase → VideoSearchRepository (→ Server API)
```

### 5. 削除可能なコード

移行完了後に不要となるもの：
- `YouTubeSearchDataSource` / `TwitchSearchDataSource`（インターフェース + 実装）
- `data/mapper/` 内の外部API用マッパー（YouTubeVideoMapper, TwitchVideoMapper, YouTubeSearchMapper, TwitchSearchMapper, YouTubeChannelMapper, TwitchChannelMapper）※サーバー側で使用されている場合は `server` モジュールのみに残す
- `data/model/` 内の外部APIレスポンスDTO（YouTubeApiResponse, TwitchApiResponse, YouTubeSearchResponse, TwitchSearchResponse 等）※同上
- BuildKonfig のAPIキー設定

**注意**: mapper と data/model はサーバーモジュールで使用されている可能性がある。shared モジュールからの削除はサーバーへの影響を確認の上で行う。サーバーが shared を依存している場合、mapper/model は残すか server に移動する。

---

## 技術的な注意点

- **ApiResponse のシリアライゼーション**: sealed class + generics の組み合わせ。`ApiResponse.Success` のデシリアライズには型パラメータが必要。レスポンス形式（discriminator 有無）を実装時に確認
- **HttpClient 共有**: 既存の `createHttpClient()` をサーバーAPI用に調整（ベースURL設定等）
- **外部API DTO/Mapper の扱い**: server モジュールが shared モジュールの mapper/model を使用している場合、安易に削除するとサーバーがビルドできなくなる。削除前にサーバー側の依存を確認
- **プラットフォーム別 URL**: Android エミュレータの `10.0.2.2` 問題は expect/actual パターンで解決
- **既存テスト**: 外部API モック → サーバーAPI モックへの変更が必要
