# 設計メモ: 検索APIエンドポイント

> **US**: `implement-context/api-proxy-server/us-3-search-api/US.md`
> **SPECIFICATION**: `server/src/main/kotlin/org/example/project/search/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Shared Domain | `shared/.../domain/model/ChannelSearchResponse.kt` | チャンネル検索レスポンスのドメインモデル（新規） |
| Server | `server/.../service/SearchService.kt` | 検索サービスinterface（新規） |
| Server | `server/.../service/SearchServiceImpl.kt` | YouTube/Twitch検索APIプロキシ実装（新規） |
| Server | `server/.../routes/SearchRoutes.kt` | `GET /api/search/videos`, `GET /api/search/channels`（新規） |
| Server | `server/.../Application.kt` | searchRoutes 登録追加 |
| Server Test | `server/src/test/.../routes/SearchRoutesTest.kt` | エンドポイントテスト（新規） |

### 既存コードとの関連

- **shared データモデル再利用**: `YouTubeSearchResponse`, `TwitchSearchResponse`, `TwitchUserResponse`, `YouTubeChannelSearchResponse` をサーバー側でもHTTPクライアントのレスポンスデシリアライズに使用
- **shared ドメインモデル再利用**: `SearchResult`, `SearchQuery`, `SearchResponse`, `ChannelInfo`, `VideoServiceType` をレスポンスに使用
- **クライアント側DataSource実装パターン参考**: `TwitchSearchDataSourceImpl`, `YouTubeSearchDataSourceImpl` のAPI呼び出しパターンをサーバー側に移植
- **US-1/US-2基盤**: `ApiKeyConfig`, `StatusPages`, `ContentNegotiation`, `ApiResponse`, `HttpClient`生成パターンを活用
- 準拠ADR: ADR-001, ADR-005

### アーキテクチャ

```
Routes (SearchRoutes)
  ↓ リクエストパース・バリデーション
Service (SearchService)
  ↓ 外部API呼び出し
Ktor HttpClient → YouTube Search API / Twitch Search API
  ↓ レスポンスデシリアライズ
shared/data/model (YouTubeSearchResponse, TwitchSearchResponse, TwitchUserResponse, YouTubeChannelSearchResponse)
  ↓ マッピング
shared/domain/model (SearchResult, ChannelInfo)
  ↓ ApiResponse.Success でラップ
Routes → クライアント
```

---

## 技術的な注意点

- **Twitch動画検索は2段階**: Twitch Helix APIに直接の動画検索エンドポイントがないため、`search/channels` → ユーザーID取得 → `/videos` の2段階で取得（クライアント側の `TwitchSearchDataSourceImpl` と同パターン）
- **統合検索（service未指定時）**: YouTube と Twitch を `coroutineScope` + `async` で並行呼び出し。片方がエラーでも成功した方の結果を返す
- **ページネーション**: YouTube は `pageToken`、Twitch は `cursor` と異なるパラメータを使用。統合検索時はサービス別にページネーショントークンを管理
- **shared SearchResult マッピング**: サーバー側で `YouTubeSearchItem` → `SearchResult`、`TwitchSearchItem` → `SearchResult` の変換を行う。既存の shared マッパーがない場合はサービス内で変換ロジックを実装
- **ChannelSearchResponse**: チャンネル検索専用のレスポンスモデルを shared に追加（`List<ChannelInfo>` + ページネーション情報）
- **HttpClient共有**: US-2で作成した `VideoServiceImpl` と同じ `HttpClient` インスタンスを `SearchServiceImpl` でも使用
