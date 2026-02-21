# 進捗管理: タイムスタンプマーカーUI

> **US**: US-3（コメントタイムスタンプ同期 Epic）
> **SPECIFICATION**: `feature/timeline_sync/comment_timestamp/SPECIFICATION.md`
> **ブランチ**: `feature/us3-timestamp-marker-ui`

---

## Shared Layer

変更なし（US-2で CommentRepository, TimestampExtractor, TimestampMarker 等は実装済み）

### Shared テスト（補完）
- [x] `TimestampExtractorTest.kt` - タイムスタンプ抽出ロジックのユニットテスト
- [x] `./gradlew :shared:build` 成功

---

## ComposeApp Layer

### State / Intent
- [x] `TimelineSyncUiState.kt` - `ChannelCommentState`, `CommentLoadStatus`, `TimestampMarkerPreview` 型追加
- [x] `TimelineSyncUiState.kt` - `channelComments`, `selectedMarkerPreview` プロパティ追加
- [x] `TimelineSyncIntent.kt` - `SelectMarker`, `DismissMarkerPreview`, `RetryLoadComments` 追加

### ViewModel
- [x] `TimelineSyncViewModel.kt` - `CommentRepository` 依存追加
- [x] `TimelineSyncViewModel.kt` - コメント自動取得ロジック（YouTube チャンネルの selectedStream 変更時）
- [x] `TimelineSyncViewModel.kt` - `SelectMarker` / `DismissMarkerPreview` / `RetryLoadComments` ハンドリング
- [x] `TimelineSyncViewModel.kt` - エラーハンドリング（commentsDisabled → DISABLED、ネットワークエラー → ERROR）

### UI（4層構造）
- [x] `TimelineSyncContainer.kt` - uiState 全体を Screen に受け渡し（変更不要）
- [x] `TimelineSyncScreen.kt` - MarkerPreviewPopup オーバーレイ配置
- [x] `TimelineSyncContent.kt` - channelMarkersMap 計算（remember）、プレビューPopup 配置
- [x] `TimelineCardsWithSyncLine.kt` - 各チャンネルカードに TimestampMarkerDots を追加

### 新規コンポーネント
- [x] `TimestampMarkerDots.kt` - マーカードット描画（クラスタリング・タップ処理）
- [x] `MarkerPreviewPopup.kt` - コメントプレビューPopup（著者名・テキスト・いいね数）

### DI
- [x] `AppModule.kt` - `TimelineSyncViewModel` に `CommentRepository` 依存を追加

### ComposeApp テスト
- [x] `ClusterMarkersTest.kt` - `clusterMarkers()` 関数のユニットテスト（fraction計算修正済み）
- [x] `TimelineSyncViewModelTest.kt` - コメント関連UiState操作のテスト
- [x] `./gradlew :composeApp:build` 成功

---

## Integration

### 最終確認
- [x] `./gradlew test` 全テスト成功
- [x] SPECIFICATION.md の US-3 スコープ（マーカー表示・プレビュー）が実装済み

---

## メモ

- 実装コードは全て完了済み（前セッションでマージ済み）
- テストが未作成のため、本セッションではテスト作成が主な作業
- `clusterMarkers()` は純粋関数であり、テストが書きやすい
- ViewModel テストでは `CommentRepository` の mock が必要
- US-4（コメントリスト BottomSheet・同期時刻更新）はスコープ外
- Twitch チャンネルにはマーカー非表示（YouTube 限定）
