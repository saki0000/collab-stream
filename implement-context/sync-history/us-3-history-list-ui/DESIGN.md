# 設計メモ: 履歴一覧表示UI

> **US**: sync-history / US-3: 履歴一覧表示UI
> **SPECIFICATION**: `feature/timeline_sync/sync_history/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| ComposeApp State | `SyncHistoryListUiState.kt` | 画面状態定義（履歴リスト、ソート順、ダイアログ状態） |
| ComposeApp Intent | `SyncHistoryListIntent.kt` | ユーザー操作定義（ソート変更、削除、名前変更等） |
| ComposeApp SideEffect | `SyncHistoryListSideEffect.kt` | 副作用定義（Snackbar表示） |
| ComposeApp ViewModel | `SyncHistoryListViewModel.kt` | MVI ViewModel（observeHistoriesでリアルタイム監視） |
| ComposeApp UI | `ui/SyncHistoryListContainer.kt` | Container層（Stateful、ViewModel接続） |
| ComposeApp UI | `ui/SyncHistoryListScreen.kt` | Screen層（Scaffold、TopAppBar、状態別分岐） |
| ComposeApp UI | `ui/SyncHistoryListContent.kt` | Content層（LazyColumn、履歴リスト表示） |
| ComposeApp UI | `ui/components/SyncHistoryCard.kt` | Component層（履歴カード） |
| Navigation | `core/navigation/Routes.kt` | `SyncHistoryListRoute` 追加 |
| Navigation | `core/navigation/NavGraph.kt` | composable登録 + ArchiveHomeからの遷移 |
| Navigation | `archive_home/ui/ArchiveHomeContainer.kt` | `onNavigateToSyncHistory` コールバック追加 |
| Navigation | `archive_home/ui/ArchiveHomeScreen.kt` | TopAppBarに履歴アイコンボタン追加 |
| DI | `core/di/AppModule.kt` | SyncHistoryListViewModel 登録 |
| Test | `SyncHistoryListViewModelTest.kt` | ViewModel テスト |

### 既存コードとの関連

- 参考実装: `feature/archive_home/`（4層Component構造、MVI、Container/Screen/Content分離）
- ドメイン層: `SyncHistoryRepository` は全メソッド実装済み（Shared層の変更不要）
  - `observeHistories(sortBy)`: リアルタイム監視（Flow）
  - `deleteHistory(historyId)`: 削除
  - `updateHistoryName(historyId, newName)`: 名前変更
- 準拠ADR: ADR-001（Android Architecture）, ADR-002（MVI）, ADR-003（4層Component）

---

## 技術的な注意点

- `observeHistories` が `Flow<List<SyncHistory>>` を返すため、ViewModel で `collect` してリアルタイム更新
- ソート変更時は `observeHistories` に新しい `sortBy` を渡して再購読
- `SyncHistory.displayName` 拡張プロパティで表示名を取得（name ?? 自動生成名）
- 相対日時表示（"3日前" 等）はComposeApp側で計算（`kotlin.time.Clock` をContainer層で取得し、差分を計算）
- `@OptIn(ExperimentalTime::class)` が必要（SyncHistoryモデルでInstantを使用）
- ArchiveHome の TopAppBar actions に履歴アイコンを追加（Settings アイコンの隣）
