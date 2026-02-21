# 進捗管理: 履歴一覧表示UI

> **US**: sync-history / US-3: 履歴一覧表示UI
> **SPECIFICATION**: `feature/timeline_sync/sync_history/SPECIFICATION.md`
> **ブランチ**: `feature/us3-history-list-ui`

---

## Shared Layer

Shared層の変更なし（SyncHistoryRepositoryが全メソッド実装済み）

---

## ComposeApp Layer

### State / Intent / SideEffect
- [x] `SyncHistoryListUiState.kt` - 画面状態定義（履歴リスト、ソート順、ダイアログ状態）
- [x] `SyncHistoryListIntent.kt` - ユーザー操作定義（ソート変更、削除、名前変更等）
- [x] `SyncHistoryListSideEffect.kt` - 副作用定義（Snackbar表示）

### ViewModel
- [x] `SyncHistoryListViewModel.kt` - MVI ViewModel（observeHistoriesでリアルタイム監視）

### UI（4層構造）
- [x] `ui/SyncHistoryListContainer.kt` - Container層（ViewModel接続、SideEffect処理）
- [x] `ui/SyncHistoryListScreen.kt` - Screen層（Scaffold、TopAppBar、状態分岐）
- [x] `ui/SyncHistoryListContent.kt` - Content層（LazyColumn、履歴リスト表示）
- [x] `ui/components/SyncHistoryCard.kt` - Component層（履歴カード）

### Navigation
- [x] `Routes.kt` に `SyncHistoryListRoute` 追加
- [x] `NavGraph.kt` に composable 登録
- [x] `ArchiveHomeContainer.kt` に `onNavigateToSyncHistory` コールバック追加
- [x] `ArchiveHomeScreen.kt` TopAppBar に履歴アイコンボタン追加

### ComposeApp テスト
- [x] `SyncHistoryListViewModelTest.kt`
- [x] `./gradlew :composeApp:assembleDebug` 成功
- [x] `./gradlew :composeApp:testDebugUnitTest` 全テスト成功

---

## Integration

### DI（Koin）
- [x] `AppModule.kt` に `SyncHistoryListViewModel` 登録

### 最終確認
- [x] `./gradlew :composeApp:testDebugUnitTest` 全テスト成功
- [x] SPECIFICATION.md の全ユーザーストーリーが実装済み

---

## メモ

- `observeHistories` が Flow を返すため、ViewModel で `startObservingHistories` でJobを管理する方式を採用。ソート変更時に前のJobをキャンセルして再購読する。
- テストでは `Dispatchers.setMain(StandardTestDispatcher())` + `advanceUntilIdle()` を使用して非同期処理を確定的にテスト。
- 相対日時表示（"3日前"等）の計算はContent層内の `formatRelativeTime()` 関数で実装し、`now` は Container 層から引数で渡す（Clock使用ルール準拠）。
