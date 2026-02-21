# 進捗管理: フォロー/アンフォロー UI

> **US**: US-2（フォロー/アンフォロー UI）
> **SPECIFICATION**: `feature/channel_follow/SPECIFICATION.md`
> **ブランチ**: `feature/channel-follow-us2-follow-ui`

---

## ComposeApp Layer

### State / Intent
- [x] `TimelineSyncUiState.kt` - `followedChannelIds: Set<String>` フィールド追加
- [x] `TimelineSyncIntent.kt` - `ToggleFollow(channel: ChannelInfo)` Intent 追加
- [x] `TimelineSyncIntent.kt` - `ShowFollowFeedback` SideEffect 追加

### ViewModel
- [x] `TimelineSyncViewModel.kt` - `ChannelFollowRepository` 依存追加
- [x] `TimelineSyncViewModel.kt` - `observeFollowedChannels()` による Flow 監視（init）
- [x] `TimelineSyncViewModel.kt` - `ToggleFollow` Intent ハンドリング（follow/unfollow 切り替え）
- [x] `TimelineSyncViewModel.kt` - プラットフォーム切替時の `followedChannelIds` 再計算

### UI（4層構造）
- [x] `ChannelAddBottomSheet.kt` - Screen/Content に `followedChannelIds` と `onToggleFollow` パラメータ追加
- [x] `ChannelAddBottomSheet.kt` - `ChannelSuggestionItem` にフォローアイコンボタン追加
- [x] `TimelineSyncScreen.kt` - `ChannelAddBottomSheet` への新パラメータ受け渡し
- [x] `TimelineSyncContainer.kt` - `ShowFollowFeedback` SideEffect ハンドリング（Snackbar）

### DI
- [x] `AppModule.kt` - `TimelineSyncViewModel` に `channelFollowRepository: get()` 追加

### ComposeApp テスト
- [x] 既存テスト修正（FakeChannelFollowRepository 追加、3ファイル）
- [x] `./gradlew :composeApp:build` 成功
- [x] `./gradlew :composeApp:test` 成功

---

## Integration

### 最終確認
- [x] `./gradlew test` 全テスト成功
- [x] SPECIFICATION.md の全ユーザーストーリーが実装済み

---

## メモ

- Shared Layer は US-1 で完了済み、変更不要
- 既存テスト3ファイル（ChannelAdd, ExternalAppNavigation, SyncTimeCalculation）に FakeChannelFollowRepository を追加
