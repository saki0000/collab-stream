# 設計メモ: コメントAPIプロキシエンドポイント

> **US**: implement-context/comment-timestamp-sync/us-1-comment-api-proxy/US.md
> **SPECIFICATION**: `feature/timeline_sync/comment_timestamp/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Shared Domain | `shared/.../domain/model/VideoComment.kt` | `@Serializable` 追加 |
| Shared Domain | `shared/.../domain/model/VideoCommentsResponse.kt` | **新規** サーバーレスポンスモデル |
| Shared Data | `shared/.../data/model/YouTubeCommentThreadsResponse.kt` | **新規** YouTube API レスポンスDTO |
| Shared Data | `shared/.../data/mapper/YouTubeCommentMapper.kt` | **新規** DTO → Domain 変換 |
| Server | `server/.../plugins/StatusPages.kt` | `CommentsDisabledException` 追加 |
| Server | `server/.../routes/CommentRoutes.kt` | **新規** コメントAPIルーティング |
| Server | `server/.../service/CommentService.kt` | **新規** サービスインターフェース |
| Server | `server/.../service/CommentServiceImpl.kt` | **新規** サービス実装 |
| Server | `server/.../Application.kt` | CommentService + ルート登録 |
| Server Test | `server/.../routes/CommentRoutesTest.kt` | **新規** ルートテスト |

### 既存コードとの関連

- 参考実装: `server/.../routes/VideoRoutes.kt` + `server/.../service/VideoServiceImpl.kt`
- 準拠ADR: ADR-005（Phase 2: プロキシサーバー方式）

---

## 設計詳細

### サーバーレスポンスモデル

```kotlin
@Serializable
data class VideoCommentsResponse(
    val videoId: String,
    val comments: List<VideoComment>,
    val nextPageToken: String?,
)
```

- `timestampMarkers` はUS-2（クライアント側）で抽出するため、サーバーレスポンスには含めない
- `ApiResponse.Success(VideoCommentsResponse)` の形式で返却

### エンドポイント仕様

```
GET /api/videos/{id}/comments?maxResults=100&pageToken=xxx&order=relevance
```

| パラメータ | 種別 | 必須 | デフォルト | 説明 |
|-----------|------|------|-----------|------|
| id | パス | Yes | - | YouTube動画ID |
| maxResults | クエリ | No | 100 | 取得件数（1-100） |
| pageToken | クエリ | No | null | ページネーショントークン |
| order | クエリ | No | relevance | ソート順（relevance/time） |

### YouTube CommentThreads API プロキシフロー

```
Client → GET /api/videos/{id}/comments
  → Server: パラメータバリデーション
  → Server: YouTube commentThreads.list 呼び出し
    - part=snippet
    - videoId={id}
    - textFormat=plainText
    - maxResults / pageToken / order
    - key={YOUTUBE_API_KEY}
  → Server: レスポンスをVideoComment形状にマッピング
  → Server: ApiResponse.Success(VideoCommentsResponse) 返却
```

### エラーハンドリング

| YouTube APIレスポンス | サーバーの処理 | クライアントへのレスポンス |
|---------------------|--------------|----------------------|
| 200 + items | 正常変換 | 200 + VideoCommentsResponse |
| 200 + items空 | 空リスト返却 | 200 + comments: [] |
| 403 commentsDisabled | CommentsDisabledException | 403 + エラーメッセージ |
| その他エラー | ExternalApiException | 502 + エラーメッセージ |
| APIキー未設定 | ServiceUnavailableException | 503 + エラーメッセージ |

### YouTube API 403エラー検出

YouTube API が 403 を返した場合、レスポンスボディのエラー理由をチェック:
- `reason: "commentsDisabled"` → `CommentsDisabledException` をスロー
- その他の403 → `ExternalApiException` をスロー

---

## 技術的な注意点

- YouTube `commentThreads.list` は APIキーのみでOK（OAuth不要）
- クォータ消費: 1ユニット/リクエスト（非常に効率的）
- `maxResults` の上限は100（YouTube API制約）
- `textFormat=plainText` を指定してプレーンテキストで取得（タイムスタンプ抽出はUS-2で実施）
- サーバーは手動DI（Koin不使用）：VideoServiceImpl と同様にコンストラクタ注入
- `authorProfileImageUrl` は `snippet.authorProfileImageUrl` から取得
