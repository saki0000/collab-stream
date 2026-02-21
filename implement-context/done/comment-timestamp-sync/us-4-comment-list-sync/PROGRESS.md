# 進捗管理: コメントリスト表示 & 同期連携

> **US**: implement-context/comment-timestamp-sync/us-4-comment-list-sync/US.md
> **SPECIFICATION**: `feature/timeline_sync/comment_timestamp/SPECIFICATION.md`
> **ブランチ**: `feature/us4-comment-list-sync`

---

## ComposeApp Layer

### State / Intent

- [x] `TimelineSyncUiState.kt` - CommentSortOrder enum追加、コメントリスト用状態フィールド追加（isCommentListVisible, commentListChannelId, commentSortOrder, isLoadingMoreComments）、ChannelCommentState に nextPageToken 追加
- [x] `TimelineSyncIntent.kt` - コメントリスト操作Intent追加（OpenCommentList, CloseCommentList, ChangeCommentSortOrder, LoadMoreComments, TapCommentTimestamp）

### ViewModel

- [x] `TimelineSyncViewModel.kt` - 新Intent処理追加（コメントリスト開閉、ソート切替、ページネーション、タイムスタンプタップ→同期時刻更新）

### UI（4層構造）

- [x] `ui/components/CommentListBottomSheet.kt` - **新規** ModalBottomSheet（ヘッダー + ソート切替 + LazyColumn + ページネーション）
- [x] `ui/components/CommentListItem.kt` - **新規** コメントアイテム（アイコン・著者名・テキスト・タイムスタンプリンク・いいね数・投稿日時）
- [x] `ui/components/TimelineCardsWithSyncLine.kt` - コメントリストボタン追加（LOADED状態のチャンネルカードに表示）
- [x] `ui/TimelineSyncContent.kt` - BottomSheet統合（isCommentListVisible時にCommentListBottomSheet表示）

### ComposeApp テスト

- [x] `TimelineSyncViewModelCommentListTest.kt` - **新規** コメントリスト開閉、ソート切替、ページネーション、タイムスタンプタップ同期のテスト（20件すべてPASS）
- [x] `./gradlew :composeApp:build` 成功

---

## Integration

### DI（Koin）

- [x] DI変更なし（既存のCommentRepository注入で十分）を確認

### 最終確認

- [x] `./gradlew test` 全テスト成功
- [x] SPECIFICATION.md の全ユーザーストーリーが実装済み

---

## メモ

実装中に気づいたこと、次回への申し送り事項などをここに記録。

- Shared層の変更は不要（CommentRepositoryが既にpageToken/orderサポート済み）
- ソートはクライアントサイドで実施（APIリフェッチ不要）
- タイムスタンプタップ → 同期時刻更新は既存の calculateChannelsSyncInfo() を再利用
