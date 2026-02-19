# 設計メモ: アーカイブHome画面 - フィード表示

> **US**: US-3（チャンネルフォロー & アーカイブHome Epic）
> **SPECIFICATION**: `feature/archive_home/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| ComposeApp | `feature/archive_home/ArchiveHomeUiState.kt` | 画面状態定義（新規） |
| ComposeApp | `feature/archive_home/ArchiveHomeIntent.kt` | Intent + SideEffect定義（新規） |
| ComposeApp | `feature/archive_home/ArchiveHomeViewModel.kt` | MVI ViewModel（新規） |
| ComposeApp | `feature/archive_home/ui/ArchiveHomeContainer.kt` | Container層 - Stateful（新規） |
| ComposeApp | `feature/archive_home/ui/ArchiveHomeScreen.kt` | Screen層 - 状態別レイアウト（新規） |
| ComposeApp | `feature/archive_home/ui/ArchiveHomeContent.kt` | Content層 - WeekCalendar + カードリスト（新規） |
| ComposeApp | `feature/archive_home/ui/components/ArchiveCard.kt` | Component層 - アーカイブカード（新規） |
| ComposeApp | `core/navigation/Routes.kt` | `ArchiveHomeRoute` 追加 |
| ComposeApp | `core/navigation/NavGraph.kt` | startDestination変更 + composable追加 |
| ComposeApp | `core/di/AppModule.kt` | ArchiveHomeViewModel登録 |

**Shared Layer**: 変更なし（既存の`ChannelFollowRepository`と`TimelineSyncRepository`を再利用）

### US-3 と US-4 の境界

US-3（今回）:
- アーカイブの**表示のみ**（日付別フィード）
- 空状態、ローディング、エラーの各状態
- 検索モーダル経由のフォロー追加導線

US-4（次回）:
- カードタップでの選択トグル
- ボトムアクションバー
- TimelineSyncへのプリセット遷移

### 既存コードとの関連

- **参考実装**: `feature/timeline_sync/`（MVI + 4層構造の完全な実装例）
- **WeekCalendar**: `feature/timeline_sync/ui/components/WeekCalendar.kt` をそのまま再利用
- **ChannelAddBottomSheet**: `feature/timeline_sync/channel_add/ChannelAddBottomSheet.kt` を再利用（フォロー機能付き）
- **FakeChannelFollowRepository**: テスト用Fake（`composeApp/src/test/...`）を再利用
- **準拠ADR**: ADR-001, ADR-002, ADR-003

---

## データフロー

```
ChannelFollowRepository.observeFollowedChannels()
    ↓ (Flow)
ArchiveHomeViewModel
    ↓ フォロー中チャンネルごとに
TimelineSyncRepository.getChannelVideos(channelId, serviceType, dateRange)
    ↓ (Result<List<VideoDetails>>)
VideoDetails → ArchiveItem 変換
    ↓
ArchiveHomeUiState.archives
    ↓ (StateFlow)
ArchiveHomeScreen → ArchiveHomeContent → ArchiveCard
```

### ArchiveItem（UI モデル）

`VideoDetails` + `FollowedChannel` から派生するUI表示用モデル:

```kotlin
data class ArchiveItem(
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val channelId: String,
    val channelName: String,
    val channelIconUrl: String,
    val serviceType: VideoServiceType,
    val publishedAt: Instant?,
    val durationSeconds: Float?,
)
```

### 状態定義

```kotlin
data class ArchiveHomeUiState(
    val isLoading: Boolean = false,
    val archives: List<ArchiveItem> = emptyList(),
    val followedChannels: List<FollowedChannel> = emptyList(),
    val selectedDate: LocalDate,
    val displayedWeekStart: LocalDate,
    val errorMessage: String? = null,
    // 検索モーダル（ChannelAddBottomSheet再利用）
    val isChannelAddModalVisible: Boolean = false,
    val selectedPlatform: VideoServiceType = VideoServiceType.TWITCH,
    val channelSearchQuery: String = "",
    val channelSuggestions: List<ChannelInfo> = emptyList(),
    val isSearchingChannels: Boolean = false,
    val followedChannelIds: Set<String> = emptySet(),
)
```

---

## 技術的な注意点

- **WeekCalendar再利用**: 現在 `timeline_sync/ui/components/` にあるため、直接インポートする。将来的な共通化は別タスク
- **ChannelAddBottomSheet再利用**: `onChannelSelect` は空実装（US-3ではチャンネル追加不要、フォローのみ）
- **Clock使用**: Container層のみで`Clock.System`を使用し、Screen以下には引数で渡す
- **日付範囲**: `getChannelVideos` の `dateRange` は選択日の1日分（`selectedDate..selectedDate`）
- **並列API呼び出し**: フォロー中チャンネルごとの`getChannelVideos`は`async`で並列実行
- **Flow監視**: `observeFollowedChannels()` の変更時に自動でアーカイブを再取得
