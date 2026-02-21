# 進捗管理: 履歴からの再同期機能

> **US**: implement-context/sync-history/us-4-restore-sync/US.md
> **SPECIFICATION**: `feature/timeline_sync/sync_history/SPECIFICATION.md` (US-4セクション)
> **ブランチ**: `feature/us4-restore-sync`

---

## ComposeApp Layer

### State / Intent / SideEffect
- [x] `SyncHistoryListIntent.kt` - `RestoreHistory(historyId: String)` Intent追加
- [x] `SyncHistoryListSideEffect.kt` - `NavigateToTimeline(presetChannelsJson, presetDate)` と `ShowRestoreError` 追加
- [x] `SyncHistoryListUiState.kt` - 変更不要（復元中の状態はSideEffectで処理するため）

### ViewModel
- [x] `SyncHistoryListViewModel.kt` - `restoreHistory()` メソッド追加（getHistoryById → recordUsage → NavigateToTimeline SideEffect発行）

### UI（4層構造）
- [x] `SyncHistoryCard.kt` - `onClick` パラメータ追加
- [x] `SyncHistoryListContent.kt` - `onIntent` 経由で `RestoreHistory` Intent発行
- [x] `SyncHistoryListScreen.kt` - 変更不要（onIntent をContent層に伝搬済み）
- [x] `SyncHistoryListContainer.kt` - `onNavigateToTimeline(presetChannelsJson, presetDate)` コールバック追加、`NavigateToTimeline` SideEffect処理

### Navigation
- [x] `NavGraph.kt` - `SyncHistoryListContainer` の `onNavigateToTimeline` を `TimelineSyncRoute` ナビゲーションに接続

### ComposeApp テスト
- [x] `SyncHistoryListViewModelTest.kt` - 復元ロジックのテスト追加（成功パス、recordUsage呼出確認、存在しないID、日付フォーマット）
- [x] `./gradlew :composeApp:compileDebugKotlinAndroid` 成功
- [x] `./gradlew :composeApp:testDebugUnitTest` 成功

---

## Integration

### 最終確認
- [x] `./gradlew :shared:test` 全テスト成功
- [x] SPECIFICATION.md の全ユーザーストーリーが実装済み

---

## メモ

- Shared Layer の変更なし（SyncHistoryRepository.recordUsage() が既に存在）
- DI（Koin）の変更なし（SyncHistoryListViewModel は既に登録済み、clock はデフォルト引数で対応）
- 既存の ArchiveHome → TimelineSync プリセット遷移パターンを再利用
- iOSターゲットのテストリンクエラーは既存の問題（本US固有ではない）
