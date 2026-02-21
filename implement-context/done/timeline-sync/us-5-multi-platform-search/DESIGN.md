# 設計メモ: マルチプラットフォームチャンネル検索

> **US**: Timeline Sync US-5
> **SPECIFICATION**: `feature/timeline_sync/channel_add/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Shared Domain | `shared/.../domain/model/ChannelInfo.kt` | `serviceType: VideoServiceType` フィールド追加 |
| Shared Data | `shared/.../data/model/YouTubeChannelResponse.kt` | YouTube チャンネル検索レスポンスモデル（新規） |
| Shared Data | `shared/.../data/datasource/YouTubeSearchDataSource.kt` | `searchChannels()` メソッド追加 |
| Shared Data | `shared/.../data/datasource/YouTubeSearchDataSourceImpl.kt` | YouTube Search API（type=channel）実装 |
| Shared Data | `shared/.../data/mapper/YouTubeChannelMapper.kt` | YouTubeチャンネル → ChannelInfo 変換（新規） |
| Shared Data | `shared/.../data/mapper/TwitchChannelMapper.kt` | serviceType設定を追加 |
| Shared Domain | `shared/.../domain/usecase/ChannelSearchUseCase.kt` | マルチプラットフォーム対応に拡張 |
| ComposeApp | `composeApp/.../feature/timeline_sync/TimelineSyncUiState.kt` | `selectedPlatform` 状態追加 |
| ComposeApp | `composeApp/.../feature/timeline_sync/TimelineSyncIntent.kt` | `SelectPlatform` Intent追加 |
| ComposeApp | `composeApp/.../feature/timeline_sync/TimelineSyncViewModel.kt` | プラットフォーム選択ロジック、toSyncChannel()修正 |
| ComposeApp | `composeApp/.../feature/timeline_sync/channel_add/ChannelAddBottomSheet.kt` | プラットフォーム選択タブUI、アイコン表示 |
| Shared DI | `shared/.../di/SharedModule.kt` | ChannelSearchUseCase依存更新 |

### 既存コードとの関連

- 参考実装: TwitchSearchDataSource のチャンネル検索（`searchChannels()`）
- 参考実装: VideoSearchRepositoryImpl のマルチサービス対応パターン
- 準拠ADR: ADR-001（Android Architecture）, ADR-002（MVI）, ADR-003（4層Component）

---

## 設計詳細

### 1. ChannelInfo への serviceType 追加

```kotlin
data class ChannelInfo(
    val id: String,
    val displayName: String,
    val thumbnailUrl: String? = null,
    val broadcasterLanguage: String? = null,
    val gameId: String? = null,
    val gameName: String? = null,
    val serviceType: VideoServiceType = VideoServiceType.TWITCH, // 追加（後方互換のためデフォルト値）
)
```

### 2. YouTube チャンネル検索 API

YouTube Data API v3 の `/search` エンドポイントを `type=channel` で使用:

```
GET https://www.googleapis.com/youtube/v3/search
  ?part=snippet
  &type=channel
  &q={query}
  &maxResults={maxResults}
  &key={API_KEY}
```

レスポンスモデル:
```kotlin
@Serializable
data class YouTubeChannelSearchResponse(
    val items: List<YouTubeChannelSearchItem> = emptyList(),
)

@Serializable
data class YouTubeChannelSearchItem(
    val id: YouTubeChannelSearchId,
    val snippet: YouTubeChannelSnippet,
)
```

### 3. ChannelSearchUseCase の拡張

現在 TwitchSearchDataSource に直接依存 → YouTube対応を追加:

```kotlin
class ChannelSearchUseCase(
    private val twitchSearchDataSource: TwitchSearchDataSource,
    private val youTubeSearchDataSource: YouTubeSearchDataSource, // 追加
) {
    suspend fun searchChannels(
        query: String,
        serviceType: VideoServiceType,
        maxResults: Int = 5,
    ): Result<List<ChannelInfo>>
}
```

### 4. UI - プラットフォーム選択タブ

ChannelAddBottomSheet のレイアウト:
```
┌──────────────────────────┐
│ チャンネルを追加           │
├──────────────────────────┤
│ [Twitch] [YouTube]  ← タブ│
├──────────────────────────┤
│ 🔍 検索...               │
├──────────────────────────┤
│ 検索結果                  │
│  📺 Channel1  [+]        │
│  📺 Channel2  [+]        │
├──────────────────────────┤
│ 追加済みチャンネル         │
│  🟣 TwitchCh  [×]        │
│  🔴 YouTubeCh [×]        │
└──────────────────────────┘
```

- `SingleChoiceSegmentedButtonRow` でタブ切り替え
- 各チャンネルアイテムにプラットフォームアイコン表示

### 5. State/Intent 変更

```kotlin
// UiState に追加
val selectedPlatform: VideoServiceType = VideoServiceType.TWITCH

// Intent に追加
data class SelectPlatform(val platform: VideoServiceType) : TimelineSyncIntent
```

### 6. ViewModel ロジック

プラットフォーム切り替え時:
1. `selectedPlatform` を更新
2. 検索結果（`channelSuggestions`）をクリア
3. 検索クエリが空でない場合 → 選択プラットフォームで自動再検索

toSyncChannel() 修正:
```kotlin
private fun ChannelInfo.toSyncChannel(): SyncChannel = SyncChannel(
    ...
    serviceType = this.serviceType, // ハードコードからChannelInfoのserviceTypeへ
)
```

---

## 技術的な注意点

- YouTube Search API はクォータ消費が大きい（100 units/call）。maxResults=5 を維持
- `ChannelInfo` への `serviceType` 追加はデフォルト値で後方互換性を確保
- 既存の `searchTwitchChannels()` は内部メソッドとして残し、新しい `searchChannels()` から呼び出す
- YouTube チャンネル検索結果の thumbnail は `snippet.thumbnails.default.url` を使用
