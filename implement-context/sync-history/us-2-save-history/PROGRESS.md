# 進捗管理: 履歴保存機能

> **US**: sync-history/US-2: 履歴保存機能
> **SPECIFICATION**: `feature/timeline_sync/sync_history/SPECIFICATION.md`
> **ブランチ**: `feature/save-history`

---

## Shared Layer

US-1で実装済みのため変更なし。

- [x] `SyncHistory.kt` - ドメインモデル（US-1完了）
- [x] `SavedChannelInfo.kt` - チャンネル情報モデル（US-1完了）
- [x] `SyncHistoryRepository.kt` - Repository Interface（US-1完了）
- [x] `SyncHistoryRepositoryImpl.kt` - Repository実装（US-1完了）
- [x] `SyncHistoryDao.kt` - Room DAO（US-1完了）
- [x] `SyncHistoryMapper.kt` - Entity ↔ Domain マッパー（US-1完了）

---

## ComposeApp Layer

### State / Intent
- [x] `TimelineSyncUiState.kt` - isSavingHistory, showDuplicateDialog, duplicateHistoryId, canSaveHistory 追加
- [x] `TimelineSyncIntent.kt` - SaveHistory, ConfirmOverwriteHistory, CancelOverwriteHistory Intent追加 + SideEffect追加

### ViewModel
- [x] `TimelineSyncViewModel.kt` - SyncHistoryRepository注入、保存・重複検出ロジック追加

### UI（4層構造）
- [x] `ui/components/TimelineSyncHeader.kt` - 保存IconButton追加（canSave, isSaving, onSaveClick）
- [x] `ui/TimelineSyncScreen.kt` - ヘッダーへの保存ボタン接続 + 重複確認ダイアログ追加
- [x] `ui/TimelineSyncContainer.kt` - SideEffectハンドラー追加

### DI
- [x] `core/di/AppModule.kt` - TimelineSyncViewModel に syncHistoryRepository を追加

### ComposeApp テスト
- [x] `TimelineSyncViewModelTest.kt` - 保存機能テスト（保存ボタン有効条件、重複ダイアログ、キャンセル、保存中フラグ、最小チャンネル数）
- [x] `FakeSyncHistoryRepository.kt` - テスト用フェイク実装追加
- [x] 既存テスト更新（ChannelAdd, ExternalApp, SyncTime）- SyncHistoryRepository引数追加
- [x] `./gradlew :composeApp:build` 成功

---

## Integration

### 最終確認
- [x] `./gradlew test` 全テスト成功
- [x] SPECIFICATION.md の全ユーザーストーリーが実装済み

---

## メモ

- Shared層はUS-1で完全実装済み。本USの変更はComposeApp層のみ
- SyncHistoryRepositoryのDI登録は `databaseModule` で済んでいるため、AppModuleでの `get()` で取得可能
