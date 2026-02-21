# 設計メモ: 履歴保存機能

> **US**: sync-history/US-2: 履歴保存機能
> **SPECIFICATION**: `feature/timeline_sync/sync_history/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Shared Domain | 変更なし | US-1で実装済み（SyncHistory, SyncHistoryRepository等） |
| ComposeApp | `TimelineSyncUiState.kt` | 保存関連の状態プロパティ追加 |
| ComposeApp | `TimelineSyncIntent.kt` | SaveHistory, ConfirmOverwrite, CancelOverwrite Intent追加 |
| ComposeApp | `TimelineSyncViewModel.kt` | 保存・重複検出ロジック追加、SyncHistoryRepository注入 |
| ComposeApp | `ui/components/TimelineSyncHeader.kt` | 保存ボタン追加 |
| ComposeApp | `ui/TimelineSyncScreen.kt` | ヘッダーへの保存ボタン接続 |
| ComposeApp | `core/di/AppModule.kt` | ViewModelにSyncHistoryRepository注入 |
| ComposeApp Test | `TimelineSyncViewModelTest.kt` | 保存ロジックのテスト |

### 既存コードとの関連

- **データ層**: US-1で構築済み。SyncHistoryRepository.saveHistory(), getAllHistories() を使用
- **SyncChannel → SavedChannelInfo**: `SavedChannelInfo.kt` の拡張関数 `toSavedChannelInfo()` を使用
- **既存UIパターン**: ChannelAddBottomSheetの確認ダイアログ実装を参考
- **準拠ADR**: ADR-002 (MVI), ADR-003 (4層Component)

---

## 設計詳細

### UiState追加プロパティ

```kotlin
// 保存処理中かどうか
val isSavingHistory: Boolean = false
// 重複確認ダイアログの表示状態
val showDuplicateDialog: Boolean = false
// 重複履歴ID（上書き時に使用）
val duplicateHistoryId: String? = null
```

### 算出プロパティ

```kotlin
// 保存ボタンの有効/無効（チャンネル2つ以上）
val canSaveHistory: Boolean
    get() = channels.size >= MIN_CHANNELS_FOR_SAVE

companion object {
    const val MIN_CHANNELS_FOR_SAVE = 2
}
```

### Intent追加

```kotlin
// 保存ボタンタップ
data object SaveHistory : TimelineSyncIntent
// 重複確認ダイアログ: 上書き
data object ConfirmOverwriteHistory : TimelineSyncIntent
// 重複確認ダイアログ: キャンセル
data object CancelOverwriteHistory : TimelineSyncIntent
```

### SideEffect追加

```kotlin
// 保存成功フィードバック
data class ShowSaveHistorySuccess(val message: String) : TimelineSyncSideEffect
// 保存失敗フィードバック
data class ShowSaveHistoryError(val message: String) : TimelineSyncSideEffect
```

### 重複検出ロジック

```
1. SaveHistory Intent受信
2. SyncHistoryRepository.getAllHistories() 呼び出し
3. 各履歴のchannelIdセットと現在のchannelIdセットを比較
4. 一致あり → showDuplicateDialog = true, duplicateHistoryId = 一致ID
5. 一致なし → repository.saveHistory() 実行
```

### ヘッダーUIデザイン

TopAppBar の `actions` パラメータに IconButton を追加:
- アイコン: `Icons.Outlined.BookmarkAdd`（保存アイコン）
- 有効条件: `canSaveHistory && !isSavingHistory`
- 保存中: CircularProgressIndicator 表示

---

## 技術的な注意点

- SyncHistoryRepositoryは既にdatabaseModuleで登録済み。AppModuleでのViewModel注入のみ変更が必要
- 重複検出はViewModel内で実装（Repository側の `saveHistory()` は重複チェックなし）
- channelIdのセット比較で重複を判定（順序は無関係）
- 保存中のUI操作を防ぐため `isSavingHistory` フラグで制御
