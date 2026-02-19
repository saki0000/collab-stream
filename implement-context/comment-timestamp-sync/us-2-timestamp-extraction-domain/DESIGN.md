# 設計メモ: タイムスタンプ抽出ドメイン & データ層

> **US**: implement-context/comment-timestamp-sync/us-2-timestamp-extraction-domain/US.md
> **SPECIFICATION**: `feature/timeline_sync/comment_timestamp/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Shared Domain | `shared/.../domain/model/TimestampExtractor.kt` | **新規** タイムスタンプ抽出ユーティリティ |
| Shared Data | `shared/.../data/repository/CommentRepositoryImpl.kt` | **新規** CommentRepository実装（サーバープロキシ呼び出し + タイムスタンプ抽出） |
| Shared DI | `shared/.../di/SharedModule.kt` | CommentRepository バインディング追加 |
| Shared Test | `shared/.../domain/model/TimestampExtractorTest.kt` | **新規** タイムスタンプ抽出テスト |
| Shared Test | `shared/.../data/repository/CommentRepositoryImplTest.kt` | **新規** Repository実装テスト |

### 既に存在するファイル（US-1で作成済み、変更不要）

| レイヤー | ファイル | 状態 |
|---------|---------|------|
| Shared Domain | `shared/.../domain/model/VideoComment.kt` | 作成済み |
| Shared Domain | `shared/.../domain/model/TimestampMarker.kt` | 作成済み |
| Shared Domain | `shared/.../domain/model/CommentTimestampResult.kt` | 作成済み |
| Shared Domain | `shared/.../domain/model/VideoCommentsResponse.kt` | 作成済み |
| Shared Domain | `shared/.../domain/repository/CommentRepository.kt` | 作成済み |
| Shared Data | `shared/.../data/model/YouTubeCommentThreadsResponse.kt` | 作成済み |
| Shared Data | `shared/.../data/mapper/YouTubeCommentMapper.kt` | 作成済み |

### 既存コードとの関連

- 参考実装: `data/repository/TimelineSyncRepositoryImpl.kt`（HTTPクライアントパターン）
- サーバーエンドポイント: `GET /api/videos/{id}/comments`（US-1で実装済み）
- 準拠ADR: ADR-005（Phase 2: プロキシサーバー方式）

---

## 設計詳細

### TimestampExtractor

コメントテキストからタイムスタンプを正規表現で抽出するユーティリティ。

```kotlin
object TimestampExtractor {
    fun extractTimestamps(text: String, videoDurationSeconds: Long? = null): List<ExtractedTimestamp>
    fun toSeconds(hours: Int, minutes: Int, seconds: Int): Long
}

data class ExtractedTimestamp(
    val timestampSeconds: Long,
    val displayTimestamp: String,
)
```

**正規表現パターン**: `(?<!\d)(\d{1,2}):(\d{2})(?::(\d{2}))?(?!\d)`

- `M:SS`, `MM:SS` → 2グループマッチ（時間なし）
- `H:MM:SS`, `HH:MM:SS` → 3グループマッチ（時間あり）
- 負の先読み/後読みで数字連続を除外（日付やIP等の誤検出防止）
- 秒が60以上 → 除外
- 分が60以上（H:MM:SS形式時） → 除外
- `videoDurationSeconds` が指定された場合、超過するタイムスタンプを除外

### CommentRepositoryImpl

サーバープロキシエンドポイントを呼び出し、タイムスタンプ抽出を行う。

```kotlin
class CommentRepositoryImpl(
    private val httpClient: HttpClient,
    private val serverBaseUrl: String,
) : CommentRepository {
    override suspend fun getVideoComments(...): Result<CommentTimestampResult>
}
```

**処理フロー**:
1. `GET {serverBaseUrl}/api/videos/{videoId}/comments` を呼び出し
2. `ApiResponse<VideoCommentsResponse>` をデシリアライズ
3. 各コメントの `textContent` から `TimestampExtractor` でタイムスタンプ抽出
4. `TimestampMarker` リストを構築
5. `CommentTimestampResult` を返却

**エラーハンドリング**:
- 403レスポンス → `CommentTimestampResult(commentsDisabled = true)` を返却
- ネットワークエラー → `Result.failure()` で伝播

---

## 技術的な注意点

- サーバーレスポンスは `ApiResponse.Success<VideoCommentsResponse>` 形式
- タイムスタンプ抽出はクライアント側で実施（サーバーは生のコメントを返す）
- `serverBaseUrl` はDIで注入（Constants.ktの`SERVER_PORT`を利用）
- HttpClient は既存の `sharedModule` で登録済みのものを再利用
- 既存のHttpClientは直接外部API（YouTube/Twitch）を呼び出す設定のため、サーバープロキシ用に別途HttpClientまたはbase URL設定が必要
