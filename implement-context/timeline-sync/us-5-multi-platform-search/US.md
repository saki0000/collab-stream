# User Story: マルチプラットフォームチャンネル検索

> **Epic**: Timeline Sync
> **作成日**: 2026-01-12
> **移行元**: GitHub Issue #69

---

## User Story

ユーザーとして、YouTubeとTwitchを選択してチャンネル検索したい。なぜなら、両プラットフォームの配信者をタイムラインに追加できるようになるから。

---

## ゴール

チャンネル追加時にプラットフォーム（YouTube/Twitch）を選択し、選択したプラットフォームでチャンネル検索できるようにする

---

## 依存

- US-2: チャンネル追加・管理 ✅（完了 - チャンネル追加UI基盤）

---

## 成果物

### Domain層
- `ChannelInfo` に `serviceType: VideoServiceType` フィールド追加
- `ChannelSearchUseCase` に `searchYouTubeChannels()` メソッド追加

### Data層
- `YouTubeSearchDataSource` に `searchChannels()` メソッド追加
- YouTube Channels API 呼び出し実装

### UI層
- `ChannelAddBottomSheet` にプラットフォーム選択UI追加（タブ）
- `TimelineSyncUiState` に `selectedPlatform: VideoServiceType` 追加
- `TimelineSyncIntent` にプラットフォーム選択 Intent 追加
- `TimelineSyncViewModel` でプラットフォーム切り替えロジック実装

---

## 受け入れ条件

- Twitch / YouTube タブを切り替えられる
- 選択したプラットフォームでチャンネル検索できる
- 検索結果にプラットフォームアイコンが表示される
- 追加したチャンネルが正しい serviceType を持つ

---

## 現在の実装状態

- SPECIFICATION.md: 作成済み（`feature/timeline_sync/channel_add/SPECIFICATION.md` 内 US-5セクション）
- コード: 未実装（ChannelAddBottomSheetにプラットフォーム選択UIなし）

---

## 次のアクション

`/develop` を実行して実装開始
