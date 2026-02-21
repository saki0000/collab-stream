# 進捗管理: アーカイブ選択 & 同期画面遷移

> **US**: US-4（チャンネルフォロー & アーカイブHome Epic）
> **SPECIFICATION**: `feature/archive_home/SPECIFICATION.md`
> **ブランチ**: `feature/archive-selection-navigation`

---

## Shared Layer

変更なし（既存モデルを利用）。

---

## ComposeApp Layer

### State / Intent

- [x] `ArchiveHomeUiState.kt` - `selectedArchiveIds: Set<String>` 追加、`selectedCount` / `hasSelection` computed properties 追加
- [x] `ArchiveHomeIntent.kt` - `ToggleArchiveSelection(videoId: String)` / `OpenTimeline` Intent 追加、`NavigateToTimeline` SideEffect 追加
- [x] `TimelineSyncIntent.kt` - `LoadWithPresets(presetChannelsJson: String, presetDate: LocalDate)` Intent 追加
- [x] `TimelineSyncUiState.kt` - 変更不要（既存フィールドで対応可能）

### ViewModel

- [x] `ArchiveHomeViewModel.kt` - 選択トグル（`toggleArchiveSelection`）、選択クリア（日付変更時）、タイムライン遷移（`openTimeline`）ロジック追加
- [x] `TimelineSyncViewModel.kt` - `LoadWithPresets` ハンドラ実装（PresetChannel JSON → SyncChannel 変換、日付設定、動画データ取得）

### UI（4層構造）

- [x] `ArchiveHomeContainer.kt` - `onNavigateToTimeline` コールバック追加、`NavigateToTimeline` SideEffect 処理
- [x] `ArchiveHomeScreen.kt` - ボトムアクションバー追加（`selectedCount > 0` 時に表示）、選択状態を Content へ受け渡し
- [x] `ArchiveHomeContent.kt` - `selectedArchiveIds` + `onToggleSelection` パラメータ追加、ArchiveCard へ選択状態伝搬
- [x] `ArchiveCard.kt` - `isSelected: Boolean` パラメータ追加、チェックマークオーバーレイ表示、選択時のカード枠色変更

### Navigation

- [x] `Routes.kt` - `TimelineSyncRoute` を `data class` に変更（`presetDate: String?`, `presetChannelsJson: String?`）、`PresetChannel` data class 追加
- [x] `NavGraph.kt` - ArchiveHomeContainer に `onNavigateToTimeline` を渡す、TimelineSyncRoute へプリセット付きナビゲーション設定、TimelineSyncContainer にプリセット受け渡し

### TimelineSync Container

- [x] `TimelineSyncContainer.kt` - ルートからプリセットパラメータ受け取り、`LoadWithPresets` Intent 発行

### ComposeApp テスト

- [x] `ArchiveHomeViewModelTest.kt` - 選択トグル、選択クリア、タイムライン遷移のテスト
- [x] `./gradlew :composeApp:assembleDebug` 成功
- [x] `./gradlew :composeApp:testDebugUnitTest` 成功

---

## Integration

### DI（Koin）

- [x] 変更なし（既存のViewModel登録をそのまま利用）

### 最終確認

- [x] ビルド成功（警告のみ、エラーなし）
- [x] SPECIFICATION.md の全ユーザーストーリー（US-4）が実装済み

---

## メモ

- TimelineSyncViewModel の `loadChannelsData()` は現在モックデータを使用。プリセット時はモックではなくプリセットデータを使用する。
- ArchiveCard の `onClick` は US-3 で空実装のプレースホルダーとして配置済み。US-4 で実装済み。
- startDestination は US-3 で既に `ArchiveHomeRoute` に変更済み。
- `TimelineSyncRoute` を `data object` から `data class` に変更したため、既存のTimelineSyncRoute使用箇所も更新済み。
