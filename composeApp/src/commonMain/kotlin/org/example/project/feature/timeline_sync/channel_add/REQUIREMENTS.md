# 機能仕様: Timeline Sync - チャンネル追加・管理

> **配置場所**: `composeApp/src/commonMain/kotlin/org/example/project/feature/timeline_sync/channel_add/REQUIREMENTS.md`
> **目的**: AI実装のためのSSoT（Single Source of Truth）
> **Story**: Story 2 of EPIC-002 (Timeline Sync)
> **Issue**: #46

---

## 1. ユーザーストーリー

### チャンネル追加
- ユーザーがタイムライン画面で「+ Add」ボタンをタップすると、チャンネル追加モーダル（ボトムシート）が表示される
- モーダルには検索フィールドと追加済みチャンネルリストが表示される
- ユーザーが検索フィールドに入力すると、500msデバウンス後にチャンネル検索が実行される
- 検索結果はチャンネル候補としてリスト表示される（最大5件）
- ユーザーがチャンネル候補をタップすると、そのチャンネルがタイムラインに追加される
- 追加完了後、モーダルは閉じずに継続して追加可能（複数チャンネル追加対応）
- ユーザーがモーダルを閉じると、タイムライン画面に戻り追加されたチャンネルが表示される

### チャンネル削除
- チャンネル追加モーダル内で、追加済みチャンネルの横に削除ボタン（×）が表示される
- ユーザーが削除ボタンをタップすると、確認なしでチャンネルが削除される
- 削除後、タイムラインからそのチャンネルのカードも即時削除される

### タイムライン画面のAddボタン
- Story 1で非活性だったAddボタンが活性化される
- ボタンをタップするとチャンネル追加モーダルが開く
- チャンネル数が最大（10）に達している場合はボタンが非活性になる

---

## 2. ビジネスルール

### チャンネル検索
- **使用UseCase**: 既存の`ChannelSearchUseCase.searchTwitchChannels()`
- **デバウンス**: 500ms（StreamerSearchViewModelと同じパターン）
- **最大結果数**: 5件
- **対象サービス**: Twitch（YouTubeは将来拡張）
- **空クエリ**: 検索候補をクリア

### チャンネル追加
- **最大チャンネル数**: 10（タイムライン表示の制限）
- **重複チェック**: channelIdで重複を防止（既に追加済みの場合はエラー表示）
- **変換**: `ChannelInfo` → `SyncChannel`
  - selectedStream: null
  - syncStatus: NOT_SYNCED
  - serviceType: TWITCH

### チャンネル削除
- **確認ダイアログ**: 不要（即時削除）
- **最小チャンネル数**: 0（全削除可能 → 空状態表示）

### モーダル状態
- **表示**: ボトムシート形式
- **閉じる方法**: 背景タップ、スワイプダウン、完了ボタン
- **閉じる時の動作**: 検索状態をリセット（検索クエリ、検索候補をクリア）

### エラー処理
- **検索エラー**: モーダル内にエラーメッセージを表示
- **重複追加**: スナックバーで「既に追加済みです」を表示（2秒後に自動消去）
- **最大数超過**: スナックバーで「最大10チャンネルまで追加可能です」を表示

---

## 3. 画面フローと状態遷移

機能の詳細な振る舞いと状態遷移については、以下を参照してください。

### 画面内の振る舞い（Level 3）
画面の状態（モーダル非表示、検索中、検索結果表示等）とユーザーアクション:
- **Screen Transition**: [screen-transition.md](./screen-transition.md)

### モジュールナビゲーション（Level 2）
Timeline機能モジュール内の画面遷移:
- **Module Navigation**: [/docs/navigation/timeline-module.md](/docs/navigation/timeline-module.md)

### アプリ全体のインデックス（Level 1）
この機能が全体のどこに位置するか:
- **App Navigation**: [/docs/screen-navigation.md](/docs/screen-navigation.md)

---

## 4. Phase 2実装進捗

**Phase 1完了時に作成し、Phase 2実装中に随時更新します。**

**最終更新**: 2026-01-14

### Shared Layer
- [ ] ChannelSearchUseCaseの再利用確認
- [ ] Build成功（`./gradlew :shared:build`）

### ComposeApp Layer
- [ ] TimelineSyncUiState拡張（モーダル状態追加）
- [ ] TimelineSyncIntent拡張（チャンネル追加・削除Intent）
- [ ] TimelineSyncViewModel拡張（チャンネル管理ロジック）
- [ ] UI Components実装
  - [ ] ChannelAddBottomSheet（モーダル）
  - [ ] ChannelSearchField（検索フィールド）
  - [ ] ChannelSuggestionList（検索結果リスト）
  - [ ] AddedChannelList（追加済みチャンネルリスト）
  - [ ] AddChannelButton活性化
- [ ] ViewModel Tests実装
- [ ] DI設定更新（Koin）
- [ ] Build成功（`./gradlew :composeApp:build`）
- [ ] 全テスト成功（`./gradlew test`）
- [ ] Phase 3レビュー準備完了

**更新タイミング**:
- Phase 2開始時: このセクションを参照
- Phase 2実装中: 各タスク完了時にチェックボックスを更新
- Phase 3開始時: 全チェック完了を確認

---

## 補足

### 使用するドメインモデル
- `ChannelInfo` - 検索結果チャンネル情報（id, displayName, thumbnailUrl等）
- `SyncChannel` - タイムライン表示用チャンネル（Story 1で定義済み）
- `ChannelSearchUseCase` - Twitchチャンネル検索
- `VideoServiceType` - YOUTUBE / TWITCH

### 変換ロジック
```kotlin
fun ChannelInfo.toSyncChannel(): SyncChannel = SyncChannel(
    channelId = id,
    channelName = displayName,
    channelIconUrl = thumbnailUrl ?: "",
    serviceType = VideoServiceType.TWITCH,
    selectedStream = null,
    syncStatus = SyncStatus.NOT_SYNCED,
)
```

### UiState拡張（Phase 2で実装）
```kotlin
// 追加プロパティ
val isChannelAddModalVisible: Boolean = false
val channelSearchQuery: String = ""
val channelSuggestions: List<ChannelInfo> = emptyList()
val isSearchingChannels: Boolean = false
val channelAddError: String? = null

// computed property
val canAddChannel: Boolean get() = channels.size < 10
```

### Intent拡張（Phase 2で実装）
```kotlin
// 追加Intent
data object OpenChannelAddModal
data object CloseChannelAddModal
data class UpdateChannelSearchQuery(val query: String)
data class AddChannel(val channel: ChannelInfo)
data class RemoveChannel(val channelId: String)
data object ClearChannelAddError
```

### Story 2スコープ外
- YouTubeチャンネル検索（将来のStoryまたはバックログ）
- ストリーム選択（Story 3）
- 外部アプリ連携（Story 4）

### 参照
- **類似機能**: `feature/streamer_search/`（検索デバウンスパターン）
- **参照ADR**:
  - ADR-002（MVIパターン）
  - ADR-003（4層コンポーネント構造）

---

**作成者**: Claude Code
**作成日**: 2026-01-14
**関連Issue**: #46
**Epic**: Timeline Sync (EPIC-002)
