# 設計メモ: タイムスタンプマーカーUI

> **US**: US-3（コメントタイムスタンプ同期 Epic）
> **SPECIFICATION**: `feature/timeline_sync/comment_timestamp/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| ComposeApp State | `TimelineSyncUiState.kt` | コメント状態・マーカープレビュー状態を追加 |
| ComposeApp Intent | `TimelineSyncIntent.kt` | マーカータップ・プレビュー閉じる・リトライ Intent 追加 |
| ComposeApp ViewModel | `TimelineSyncViewModel.kt` | コメント取得ロジック・マーカー選択処理追加、CommentRepository 依存追加 |
| ComposeApp Container | `TimelineSyncContainer.kt` | 新しい state/intent を Screen に受け渡し |
| ComposeApp Screen | `TimelineSyncScreen.kt` | マーカーデータを Content に受け渡し |
| ComposeApp Content | `TimelineSyncContent.kt` | マーカー分数計算・プレビューPopup配置 |
| ComposeApp Component | `TimelineCardsWithSyncLine.kt` | 各チャンネルカードにマーカードット描画を追加 |
| ComposeApp Component（新規） | `TimestampMarkerDots.kt` | マーカードットの描画・タップ処理コンポーネント |
| ComposeApp Component（新規） | `MarkerPreviewPopup.kt` | コメントプレビューTooltipコンポーネント |
| DI | `AppModule.kt` | ViewModel に CommentRepository 依存追加 |

### 既存コードとの関連

- **US-2 成果物の利用**: `CommentRepository`, `TimestampExtractor`, `TimestampMarker`, `VideoComment`, `CommentTimestampResult`（shared層、変更なし）
- **既存TimelineSync UI**: Route → Screen → Content → Component の4層構造に準拠して拡張
- **TimelineBarInfo パターン**: バー表示情報と同様に、マーカー位置を0.0-1.0分数で管理
- 準拠ADR: ADR-002（MVI）, ADR-003（4層Component）

---

## 設計詳細

### 1. UiState 拡張

`TimelineSyncUiState` に以下を追加:

```kotlin
// コメントタイムスタンプ関連
val channelComments: Map<String, ChannelCommentState> = emptyMap(), // channelId -> state
val selectedMarkerPreview: TimestampMarkerPreview? = null,
```

新規データクラス（`TimelineSyncUiState.kt` 内に定義）:

```kotlin
enum class CommentLoadStatus {
    NOT_LOADED, LOADING, LOADED, ERROR, DISABLED
}

data class ChannelCommentState(
    val videoId: String,
    val status: CommentLoadStatus,
    val markers: List<TimestampMarker> = emptyList(),
    val errorMessage: String? = null,
)

data class TimestampMarkerPreview(
    val channelId: String,
    val marker: TimestampMarker,
)
```

### 2. Intent 追加

```kotlin
// コメントタイムスタンプ関連
data class SelectMarker(val channelId: String, val marker: TimestampMarker) : TimelineSyncIntent
data object DismissMarkerPreview : TimelineSyncIntent
data class RetryLoadComments(val channelId: String) : TimelineSyncIntent
```

### 3. ViewModel ロジック

- **自動コメント取得**: チャンネルの `selectedStream` が設定されたタイミング（既存の動画取得完了後）でYouTubeチャンネルのコメントを自動取得
- **YouTube限定**: `serviceType == YOUTUBE` のチャンネルのみ対象
- **CommentRepository 利用**: `getVideoComments()` で取得、結果の `timestampMarkers` を使用
- **エラーハンドリング**: `commentsDisabled` → DISABLED状態、その他 → ERROR状態

### 4. マーカー位置計算

Content層で `remember` を使ってマーカーの日内分数を計算:

```
markerDayFraction = (stream.startTime + marker.timestampSeconds - dayStart) / dayDuration
```

バー内の相対位置:
```
markerBarFraction = (markerDayFraction - bar.startFraction) / (bar.endFraction - bar.startFraction)
```

### 5. マーカー集約（クラスタリング）

近接マーカーの集約ロジック（Component層で実装）:
- バー幅に対して 3% 以内のマーカーをグループ化
- グループの中央位置にドットを配置
- 複数マーカーの場合はカウントバッジを表示
- タップ時は最もいいね数の多いコメントをプレビュー

### 6. UIコンポーネント設計

**TimestampMarkerDots** (Component層):
- Canvas上にドット（6dp）を描画
- タップ領域は24dp確保
- `tertiary` カラー使用（既存バーと区別）
- クラスタリング後のドット描画

**MarkerPreviewPopup** (Component層):
- Popup / DropdownMenu で実装
- 著者名・コメントテキスト（2行まで）・いいね数を表示
- マーカーのタップ位置付近に表示

---

## 技術的な注意点

- **YouTube限定**: Twitch チャンネルにはマーカーを表示しない（SPECIFICATION のスコープ）
- **duration 制約**: `SelectedStreamInfo.duration` が null の場合（ライブ等）はduration超過チェックをスキップ
- **パフォーマンス**: マーカー位置計算は `remember(markers, barInfo)` でキャッシュ
- **US-4 との境界**: コメントリスト BottomSheet、タイムスタンプタップによる同期時刻更新は US-4 スコープ。US-3 ではマーカー表示とプレビューのみ
