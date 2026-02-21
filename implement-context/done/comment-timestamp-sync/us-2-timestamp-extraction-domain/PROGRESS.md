# 進捗管理: タイムスタンプ抽出ドメイン & データ層

> **US**: implement-context/comment-timestamp-sync/us-2-timestamp-extraction-domain/US.md
> **SPECIFICATION**: `feature/timeline_sync/comment_timestamp/SPECIFICATION.md`
> **ブランチ**: `feature/comment-timestamp-us2-timestamp-extraction`

---

## Shared Layer

### Domain Model（US-1で作成済み）
- [x] `VideoComment.kt` - コメントドメインモデル
- [x] `TimestampMarker.kt` - タイムスタンプマーカーモデル
- [x] `CommentTimestampResult.kt` - コメント+タイムスタンプ結果モデル
- [x] `VideoCommentsResponse.kt` - サーバーレスポンスモデル

### Repository Interface（US-1で作成済み）
- [x] `CommentRepository.kt` - Interface定義

### タイムスタンプ抽出
- [x] `TimestampExtractor.kt` - 正規表現によるタイムスタンプ抽出ユーティリティ

### Repository 実装
- [x] `CommentRepositoryImpl.kt` - サーバープロキシ呼び出し + タイムスタンプ抽出

### DI
- [x] `SharedModule.kt` - CommentRepository バインディング追加

### Shared テスト
- [x] `TimestampExtractorTest.kt` - タイムスタンプ抽出テスト
- [x] `CommentRepositoryImplTest.kt` - Repository実装テスト
- [x] `./gradlew :shared:jvmTest :shared:testDebugUnitTest :shared:testReleaseUnitTest` 成功

---

## メモ

実装中に気づいたこと、次回への申し送り事項などをここに記録。

- US-2はShared Layer のみ。ComposeApp / Server の変更なし。
- サーバーエンドポイント（US-1）は実装済みのため、クライアント側の接続とタイムスタンプ抽出に集中。
