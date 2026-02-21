# 設計メモ: フォロー/アンフォロー UI

> **US**: US-2（フォロー/アンフォロー UI）
> **SPECIFICATION**: `feature/channel_follow/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| ComposeApp | `TimelineSyncUiState.kt` | `followedChannelIds: Set<String>` 追加 |
| ComposeApp | `TimelineSyncIntent.kt` | `ToggleFollow` Intent + `ShowFollowFeedback` SideEffect 追加 |
| ComposeApp | `TimelineSyncViewModel.kt` | `ChannelFollowRepository` 依存追加、フォロー監視・操作ロジック |
| ComposeApp | `ChannelAddBottomSheet.kt` | フォローボタン追加（ChannelSuggestionItem にハートアイコン） |
| ComposeApp | `TimelineSyncScreen.kt` | 新パラメータの受け渡し |
| ComposeApp | `TimelineSyncContainer.kt` | フォローフィードバック SideEffect ハンドリング |
| ComposeApp | `AppModule.kt` | ViewModel に `ChannelFollowRepository` 注入 |

### Shared Layer

**変更なし** - US-1 で `ChannelFollowRepository`、`FollowedChannelDao`、`FollowedChannel` モデルは実装済み。

### 既存コードとの関連

- 参考実装: `feature/timeline_sync/` の既存 MVI パターン
- 準拠ADR: ADR-002（MVI）, ADR-003（4層Component）

---

## 設計詳細

### フォロー状態の管理

検索結果は常に `selectedPlatform` でフィルタされているため、フォロー済みチャンネルIDも同プラットフォームでフィルタした `Set<String>` を UiState に保持する。

```
ChannelFollowRepository.observeFollowedChannels()
  → filter by selectedPlatform
  → map to Set<channelId>
  → followedChannelIds in UiState
```

プラットフォーム切替時にも `followedChannelIds` を再計算する。

### UI 変更

`ChannelSuggestionItem` に追加アイコン（既存）の左にフォローアイコンボタンを配置:
- 未フォロー: `FavoriteBorder`（アウトラインハート）
- フォロー済み: `Favorite`（塗りつぶしハート、`error` カラー）

フォロー操作は「追加」操作と独立しており、チャンネルをタイムラインに追加しなくてもフォロー可能。

### SideEffect

フォロー/アンフォロー後に Snackbar でフィードバック:
- フォロー時: `"{チャンネル名}をフォローしました"`
- アンフォロー時: `"{チャンネル名}のフォローを解除しました"`

---

## 技術的な注意点

- `observeFollowedChannels()` は Flow で自動更新されるため、別タブでのフォロー操作もリアルタイム反映
- フォロー操作は `ChannelFollowRepository` の Result で成功/失敗をハンドリング
- `ChannelSuggestionItem` の Row タップは既存の「追加」機能を維持、フォローは独立した `IconButton`
- DB操作はローカル Room のため高速、楽観的UI更新は不要（Flow 監視で即反映）
