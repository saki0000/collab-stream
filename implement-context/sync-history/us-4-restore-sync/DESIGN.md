# 設計メモ: 履歴からの再同期機能

> **US**: implement-context/sync-history/us-4-restore-sync/US.md
> **SPECIFICATION**: `feature/timeline_sync/sync_history/SPECIFICATION.md` (US-4セクション)

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| ComposeApp | `SyncHistoryListIntent.kt` | `RestoreHistory(historyId)` Intent追加 |
| ComposeApp | `SyncHistoryListSideEffect.kt` | `NavigateToTimeline`, `ShowRestoreError` SideEffect追加 |
| ComposeApp | `SyncHistoryListViewModel.kt` | restoreHistory() ロジック追加 |
| ComposeApp | `SyncHistoryListContainer.kt` | `onNavigateToTimeline` コールバック追加、SideEffect処理追加 |
| ComposeApp | `SyncHistoryListScreen.kt` | カードタップ → `RestoreHistory` Intent発行 |
| ComposeApp | `SyncHistoryCard.kt` | `onClick` パラメータ追加 |
| ComposeApp | `SyncHistoryListContent.kt` | `onCardClick` パラメータ追加 |
| ComposeApp | `NavGraph.kt` | `SyncHistoryListContainer` に `onNavigateToTimeline` を渡す |
| ComposeApp Test | `SyncHistoryListViewModelTest.kt` | 復元ロジックのテスト追加 |

### Shared Layer の変更

**変更なし** — 必要なAPIはすべて既存:
- `SyncHistoryRepository.getHistoryById(historyId)`: 履歴取得
- `SyncHistoryRepository.recordUsage(historyId)`: 使用状況更新
- `SavedChannelInfo` → `PresetChannel` 変換は ComposeApp 側で実施

### 既存コードとの関連

- **プリセット遷移パターン**: ArchiveHome → TimelineSync の `PresetChannel` JSON方式を再利用
  - `Routes.kt` の `PresetChannel` data class
  - `TimelineSyncRoute(presetDate, presetChannelsJson)`
  - `TimelineSyncViewModel.loadWithPresets()`
- **SyncHistoryListViewModel**: US-3で実装済み。Intent/SideEffect/UiStateを拡張
- **SyncHistoryCard**: US-3で実装済み。`onClick` パラメータを追加するだけ
- 準拠ADR: ADR-002 (MVI), ADR-003 (4層Component)

---

## 技術的な注意点

- `recordUsage` の失敗はナビゲーションをブロックしない（fire-and-forget パターンではなく、待機はするが失敗しても遷移する）
- `SavedChannelInfo.serviceType` → `PresetChannel.serviceType` は `.name` で文字列変換
- 今日の日付は `LocalDate` の ISO文字列 (`toString()`) で `TimelineSyncRoute.presetDate` に渡す
