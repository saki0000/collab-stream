# 設計メモ: コメントリスト表示 & 同期連携

> **US**: implement-context/comment-timestamp-sync/us-4-comment-list-sync/US.md
> **SPECIFICATION**: `feature/timeline_sync/comment_timestamp/SPECIFICATION.md`

---

## 実装方針

### Shared Layer: 変更なし

既存の `CommentRepository` が `pageToken` / `order` パラメータを既にサポートしており、
`CommentTimestampResult` に `nextPageToken` も含まれているため、Shared層の変更は不要。

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| ComposeApp | `TimelineSyncUiState.kt` | コメントリスト用状態追加（BottomSheet表示、ソート順、ページネーション） |
| ComposeApp | `TimelineSyncIntent.kt` | コメントリスト操作Intent追加（開閉、ソート、追加読込、タイムスタンプタップ） |
| ComposeApp | `TimelineSyncViewModel.kt` | 新Intent処理、ページネーション、同期時刻更新ロジック |
| ComposeApp | `ui/components/CommentListBottomSheet.kt` | **新規** BottomSheetコンテナ |
| ComposeApp | `ui/components/CommentListItem.kt` | **新規** コメントリストアイテム |
| ComposeApp | `ui/components/TimelineCardsWithSyncLine.kt` | コメントリストボタン追加 |
| ComposeApp | `ui/TimelineSyncContent.kt` | BottomSheet統合 |
| ComposeApp Test | `TimelineSyncViewModelCommentListTest.kt` | **新規** ViewModel テスト |

### 既存コードとの関連

- 参考実装: `ui/components/MarkerPreviewPopup.kt`（コメント表示パターン）
- 準拠ADR: ADR-002（MVI）, ADR-003（4層Component）
- 既存: `ChannelCommentState`（US-3で追加済み）にページネーション用フィールドを追加
- 既存: `TimestampMarker.comment: VideoComment` でコメント詳細にアクセス可能

---

## 詳細設計

### 1. UiState 拡張

```kotlin
// 新規 enum
enum class CommentSortOrder {
    LIKES,  // いいね数順（降順）
    TIME,   // 時間順（タイムスタンプ秒昇順）
}

// TimelineSyncUiState に追加するフィールド
val isCommentListVisible: Boolean = false
val commentListChannelId: String? = null
val commentSortOrder: CommentSortOrder = CommentSortOrder.LIKES
val isLoadingMoreComments: Boolean = false

// ChannelCommentState に追加するフィールド
val nextPageToken: String? = null
```

### 2. ソート戦略

ソートはクライアントサイドで実施する（APIから取得済みの `markers` を再ソート）:

- **いいね数順**: `markers` を `comment.likeCount` 降順でソート → `commentId` で重複除去
- **時間順**: `markers` を `timestampSeconds` 昇順でソート → `commentId` で重複除去

ソート切替時にAPIリフェッチは不要。

### 3. ページネーション

- `loadMoreComments()`: `ChannelCommentState.nextPageToken` を使って次ページを取得
- 取得した `timestampMarkers` を既存の `markers` リストに追記
- `nextPageToken` が null の場合、追加読み込み不可
- `isLoadingMoreComments` で読み込み中インジケーター表示

### 4. タイムスタンプタップ → 同期時刻更新

```
absoluteTime = channel.selectedStream.startTime + timestampSeconds.seconds
```

- チャンネルの動画開始時刻 + タイムスタンプの秒数 = 絶対時刻
- 既存の `updateSyncTime()` / `calculateChannelsSyncInfo()` を再利用

### 5. UIコンポーネント設計

#### CommentListBottomSheet（ModalBottomSheet）
- ヘッダー: チャンネル名 + ソート切替トグル
- コンテンツ: LazyColumn でコメントリスト
- フッター: ページネーションローディング
- 状態: Loading / Content / Empty / Error / Disabled

#### CommentListItem
- 左: 著者アイコン（丸形）
- 右上: 著者名 + 投稿日時
- 右中: コメントテキスト（タイムスタンプ部分はクリッカブルリンク）
- 右下: いいね数

#### コメントリストボタン配置
- 各チャンネルのタイムラインカード内に配置
- マーカーが読み込み済み（`CommentLoadStatus.LOADED`）かつタイムスタンプ付きコメントが存在する場合のみ表示
- `Icons.AutoMirrored.Filled.Chat` アイコン + バッジ（コメント数）

### 6. コメントテキスト内タイムスタンプの検出

`TimestampExtractor` の正規表現をUIで再利用し、テキスト内のタイムスタンプ部分を `AnnotatedString` + `ClickableText` で実現。

---

## 技術的な注意点

- BottomSheetは `ModalBottomSheet`（Material3）を使用
- LazyColumn の `onReachEnd` は `LaunchedEffect` + `snapshotFlow` で末尾検出
- タイムスタンプリンクの色は `MaterialTheme.colorScheme.primary` を使用
- コメントテキストの最大表示行数制限なし（全文表示）
- 著者アイコンの読み込みには既存の `AsyncImage`（Coil）を使用
- `publishedAt` は相対時間表示（例: "3日前"）ではなく、日付表示（例: "2024/01/15"）
