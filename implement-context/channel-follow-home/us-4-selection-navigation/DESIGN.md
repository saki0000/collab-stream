# 設計メモ: アーカイブ選択 & 同期画面遷移

> **US**: US-4（チャンネルフォロー & アーカイブHome Epic）
> **SPECIFICATION**: `feature/archive_home/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| ComposeApp | `feature/archive_home/ArchiveHomeUiState.kt` | 選択状態（`selectedArchiveIds`）追加 |
| ComposeApp | `feature/archive_home/ArchiveHomeIntent.kt` | `ToggleArchiveSelection`, `OpenTimeline` Intent追加、`NavigateToTimeline` SideEffect追加 |
| ComposeApp | `feature/archive_home/ArchiveHomeViewModel.kt` | 選択トグル、タイムライン遷移ロジック追加 |
| ComposeApp | `feature/archive_home/ui/ArchiveHomeScreen.kt` | ボトムアクションバー追加、選択状態の受け渡し |
| ComposeApp | `feature/archive_home/ui/ArchiveHomeContent.kt` | 選択状態 + `onToggleSelection` コールバック追加 |
| ComposeApp | `feature/archive_home/ui/components/ArchiveCard.kt` | `isSelected` パラメータ追加、チェックマーク表示 |
| ComposeApp | `feature/archive_home/ui/ArchiveHomeContainer.kt` | `onNavigateToTimeline` コールバック追加、SideEffect処理 |
| ComposeApp | `core/navigation/Routes.kt` | `TimelineSyncRoute` を data class に変更（プリセットパラメータ追加） |
| ComposeApp | `core/navigation/NavGraph.kt` | ArchiveHome → TimelineSync 遷移設定、プリセット受け渡し |
| ComposeApp | `feature/timeline_sync/TimelineSyncIntent.kt` | `LoadWithPresets` Intent追加 |
| ComposeApp | `feature/timeline_sync/TimelineSyncViewModel.kt` | プリセット読み込み・チャンネル追加ロジック追加 |
| ComposeApp | `feature/timeline_sync/TimelineSyncUiState.kt` | `presetDate` フィールド追加（日付プリセット用） |
| ComposeApp | `feature/timeline_sync/ui/TimelineSyncContainer.kt` | プリセットパラメータ受け取り、`LoadWithPresets` Intent発行 |

### Shared Layer

変更なし。`SyncChannel`, `SelectedStreamInfo` などの既存モデルをそのまま利用する。

### 既存コードとの関連

- 参考実装: `ArchiveCard.kt` の `onClick` は US-4 用のプレースホルダーとして空実装済み
- 参考実装: `MainPlayerRoute` のデータクラスRouteパターン（複合パラメータ）
- 参考実装: `StreamerSearchRoute` の SavedStateHandle パターン
- 準拠ADR: ADR-002（MVI）, ADR-003（4層Component構造）

---

## 技術的な注意点

### プリセットデータの受け渡し方式

`TimelineSyncRoute` を `data object` → `data class` に変更し、オプショナルなプリセットパラメータを追加する。

```kotlin
@Serializable
data class TimelineSyncRoute(
    val presetDate: String? = null,          // ISO日付文字列 "2024-01-15"
    val presetChannelsJson: String? = null,  // JSON配列文字列
)
```

プリセットチャンネル情報はJSON文字列で渡す（Navigation Composeのルートパラメータは基本型のみサポート）。
`kotlinx.serialization.json.Json` で encode/decode する。

### プリセットチャンネルの型

```kotlin
@Serializable
data class PresetChannel(
    val channelId: String,
    val channelName: String,
    val channelIconUrl: String,
    val serviceType: String,
)
```

ArchiveItem → PresetChannel → SyncChannel の変換チェーン。

### 選択状態の管理

- `selectedArchiveIds: Set<String>` で videoId ベースの選択管理
- 最大10件（SyncChannel の MAX_CHANNELS と整合）
- 日付変更時に選択をクリア（新しいアーカイブが読み込まれるため）

### startDestination の変更

既に `ArchiveHomeRoute` が `startDestination` に設定済み（US-3で完了）。追加変更不要。

### TimelineSyncRoute の data class 化による影響

`TimelineSyncRoute` が `data object` → `data class` になるため、NavGraph内で参照する箇所を更新する必要がある。
デフォルトパラメータにより、パラメータなしの `TimelineSyncRoute()` でも既存と同じ動作を維持。
