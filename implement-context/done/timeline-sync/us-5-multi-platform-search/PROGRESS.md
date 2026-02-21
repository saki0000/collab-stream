# 進捗管理: マルチプラットフォームチャンネル検索

> **US**: Timeline Sync US-5
> **SPECIFICATION**: `feature/timeline_sync/channel_add/SPECIFICATION.md`
> **ブランチ**: `feature/multi-platform-search`

---

## Shared Layer

### Domain Model
- [x] `ChannelInfo.kt` - `serviceType: VideoServiceType` フィールド追加（デフォルト値: TWITCH）

### Data Model
- [x] `YouTubeChannelResponse.kt` - YouTube チャンネル検索レスポンスモデル（新規作成）

### Data Source
- [x] `YouTubeSearchDataSource.kt` - `searchChannels()` メソッド追加（Interface）
- [x] `YouTubeSearchDataSourceImpl.kt` - YouTube Search API（type=channel）実装

### Mapper
- [x] `YouTubeChannelMapper.kt` - YouTubeChannelSearchItem → ChannelInfo 変換（新規作成）
- [x] `TwitchChannelMapper.kt` - `serviceType = TWITCH` を設定するよう更新

### UseCase
- [x] `ChannelSearchUseCase.kt` - YouTubeSearchDataSource依存追加、`searchChannels(query, serviceType)` メソッド追加

### Shared テスト
- [x] `ChannelSearchUseCaseTest.kt` - マルチプラットフォーム検索のテスト
- [x] `./gradlew :shared:jvmTest` 成功（iOS actual 未定義の既存問題のため :shared:build は iOS テストでエラー）

---

## ComposeApp Layer

### State / Intent
- [x] `TimelineSyncUiState.kt` - `selectedPlatform: VideoServiceType` 追加
- [x] `TimelineSyncIntent.kt` - `SelectPlatform(platform)` Intent 追加

### ViewModel
- [x] `TimelineSyncViewModel.kt` - プラットフォーム切り替えロジック実装
- [x] `TimelineSyncViewModel.kt` - `searchChannels()` をプラットフォーム対応に修正
- [x] `TimelineSyncViewModel.kt` - `toSyncChannel()` の serviceType ハードコード修正

### UI
- [x] `ChannelAddBottomSheet.kt` - プラットフォーム選択タブ（SegmentedButton）追加
- [x] `ChannelAddBottomSheet.kt` - 検索結果・追加済みリストにプラットフォームアイコン表示

### ComposeApp テスト
- [x] `TimelineSyncViewModelTest.kt` - プラットフォーム切り替え関連テスト追加
- [x] `./gradlew :composeApp:testDebugUnitTest` 成功

---

## Integration

### DI（Koin）
- [x] `SharedModule.kt` - ChannelSearchUseCase への YouTubeSearchDataSource 依存追加

### 最終確認
- [x] `./gradlew :shared:jvmTest :composeApp:testDebugUnitTest` 全テスト成功
- [x] SPECIFICATION.md の US-5 ユーザーストーリーが全て実装済み

---

## メモ

実装中に気づいたこと、次回への申し送り事項などをここに記録。

- iOS テスト（`compileTestKotlinIosX64`）は `expect fun runTest` に iOS actual がない既存の問題でエラーになる。US-5 固有の問題ではない。
- 既存の JVM テストファイル（ChannelAddViewModelTest, ExternalAppNavigationViewModelTest, SyncTimeCalculationViewModelTest）にも `FakeYouTubeSearchDataSource` / `TestYouTubeSearchDataSource` を追加して修正済み。
- `ChannelAddBottomSheet` のシグネチャ変更に伴い `TimelineSyncScreen.kt` の呼び出し箇所も更新済み。
