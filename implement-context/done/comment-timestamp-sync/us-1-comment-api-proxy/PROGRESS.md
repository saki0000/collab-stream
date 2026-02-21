# 進捗管理: コメントAPIプロキシエンドポイント

> **US**: implement-context/comment-timestamp-sync/us-1-comment-api-proxy/US.md
> **SPECIFICATION**: `feature/timeline_sync/comment_timestamp/SPECIFICATION.md`
> **ブランチ**: `feature/comment-timestamp-us1-comment-api`

---

## Shared Layer

### Domain Model
- [x] `VideoComment.kt` - `@Serializable` アノテーション追加
- [x] `VideoCommentsResponse.kt` - サーバーレスポンスモデル（新規作成）

### Data Model（YouTube API DTO）
- [x] `YouTubeCommentThreadsResponse.kt` - YouTube commentThreads.list APIレスポンスDTO（新規作成）
- [x] `YouTubeCommentMapper.kt` - YouTubeCommentItem → VideoComment 変換（新規作成）

### Shared テスト
- [x] `YouTubeCommentMapperTest.kt` - マッパーテスト（7テストケース）
- [x] `./gradlew :shared:jvmTest` 成功

---

## Server Layer

### エラーハンドリング
- [x] `StatusPages.kt` - `CommentsDisabledException` 例外クラス追加 + 403ハンドラー追加

### Routes
- [x] `CommentRoutes.kt` - `GET /api/videos/{id}/comments` ルーティング定義（新規作成）

### Service
- [x] `CommentService.kt` - サービスインターフェース（新規作成）
- [x] `CommentServiceImpl.kt` - YouTube commentThreads.list プロキシ実装（新規作成）

### Application
- [x] `Application.kt` - CommentServiceImpl 初期化 + commentRoutes 登録

### Server テスト
- [x] `CommentRoutesTest.kt` - ルートテスト（正常系5件・エラー系4件）
- [x] `./gradlew :server:build` 成功
- [x] `./gradlew :server:test` 成功

---

## Integration

### 最終確認
- [x] `./gradlew :shared:jvmTest :server:test :composeApp:compileCommonMainKotlinMetadata` 全成功
- [x] 受け入れ条件の確認:
  - [x] `GET /api/videos/{videoId}/comments` でYouTubeコメントが取得できる
  - [x] `maxResults`, `pageToken`, `order` パラメータが正しく機能する
  - [x] コメント無効化された動画で403エラーレスポンスが返る
  - [x] APIキーがレスポンスに露出しない
  - [x] 既存テストが壊れていない

---

## メモ

- タイムスタンプ抽出はUS-2（クライアント側）の責務。サーバーは生コメントを返すのみ。
- YouTube API `commentThreads.list` のクォータは1ユニット/リクエストで非常に効率的。
- `shared:build` はiOS向けテスト基盤の既存問題（TestUtils.kt runTest actual missing）でiOS buildが失敗するが、今回の変更とは無関係。JVMテストは全て通過。
