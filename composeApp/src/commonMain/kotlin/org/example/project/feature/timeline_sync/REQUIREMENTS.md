# 機能仕様: Timeline Sync - タイムライン基本表示

> **配置場所**: `composeApp/src/commonMain/kotlin/org/example/project/feature/timeline_sync/REQUIREMENTS.md`
> **目的**: AI実装のためのSSoT（Single Source of Truth）
> **Story**: Story 1 of EPIC-002 (Timeline Sync)

---

## 1. ユーザーストーリー

### 画面表示
- ユーザーがタイムライン画面を開くと、ヘッダーに「Timeline Sync」と「{N} CHANNELS ACTIVE」が表示される
- 画面上部に週間カレンダーが横スクロール可能な形式で表示される
- カレンダーの下にチャンネルアバター行が横スクロール可能な形式で表示される
- 中央にSYNC TIME表示（HH:MM:SS形式）が表示される
- メインエリアにタイムラインカードリストが縦スクロール可能な形式で表示される
- 画面下部にボトムナビゲーション（Home / Timeline / Channels / Settings）が表示される

### 日付選択
- デフォルトでは今日の日付が選択されている（青背景でハイライト）
- ユーザーがカレンダーの日付をタップすると、その日のタイムラインに切り替わる
- カレンダーは左右にスワイプして前後の週に移動できる
- 日付は「MON 17」「TUE 18」の形式で表示される

### チャンネル表示
- 各チャンネルはアバター行とタイムラインカードの2箇所で表示される
- **アバター行**:
  - 丸型アバター画像
  - プラットフォームバッジ（YouTube=赤、Twitch=紫）
  - 下部にチャンネル名
  - 右端に「+ Add」ボタン（Story 2で実装、Story 1では非活性）
- **タイムラインカード**:
  - 左側: プラットフォームアイコン + チャンネル名 + 時間範囲（HH:MM - HH:MM）
  - 右側: Open/Waitボタン（Story 4で実装、Story 1では表示のみ）
  - 背景: タイムラインバー（YouTube=赤、Twitch=紫）

### タイムラインバー表示
- ストリームの開始〜終了時刻がバーの位置と幅で視覚的に表現される
- 選択された日付の0:00-24:00を基準にバー位置を計算
- 未開始のストリームは「Starts HH:MM」「{N}M TO START」と表示される

### 同期時刻インジケーター
- タイムライン全体を貫通する縦の青い線で同期時刻を表示
- Story 1では表示のみ（同期時刻の選択・変更はStory 3で実装）
- 初期状態では同期時刻が設定されていない場合は非表示

### 空状態
- チャンネルが登録されていない場合、空状態UIを表示
- 「チャンネルを追加してください」のメッセージを表示
- チャンネル追加ボタンを表示（Story 2で動作実装）

---

## 2. ビジネスルール

### カレンダー
- **表示範囲**: 過去7日〜当日（将来拡張で未来も可能に）
- **デフォルト選択**: 今日の日付
- **形式**: 曜日（3文字英語大文字）+ 日付（数字）
- **週移動**: 左スワイプで次週、右スワイプで前週

### タイムラインバー
- **時間軸**: 選択日の0:00から24:00まで（ローカル時間）
- **バー位置計算**:
  - startTimeが選択日より前の場合 → 0:00から開始
  - endTimeが選択日より後の場合 → 24:00まで表示
  - endTimeがnull（ライブ配信中）の場合 → 現在時刻まで表示
- **バーの色**:
  - YouTube: 赤 (#FF0000)
  - Twitch: 紫 (#9146FF)
- **未開始ストリーム**:
  - 破線/グレーでバーを表示
  - 「Starts HH:MM」でストリーム開始時刻を表示
  - 「{N}M TO START」で残り時間を表示

### チャンネルデータ
- **使用モデル**: Phase 0で定義した`SyncChannel`
- **最大チャンネル数**: 10（将来拡張時の制限）
- **ストリーム**: 各チャンネルには0または1つの`SelectedStreamInfo`

### アクティブチャンネル数
- ストリームが選択されているチャンネルの数をカウント
- ヘッダーに「{N} CHANNELS ACTIVE」として表示
- 緑のドットアイコンを併せて表示

### Open/Waitボタン
- **READY状態**: 「Open」ボタン（外部リンクアイコン付き）を表示
- **WAITING状態**: 「Wait」ボタン（ロックアイコン付き、非活性）を表示
- **Story 1スコープ**: ボタンは表示のみ、タップ動作はStory 4で実装

### 同期時刻インジケーター
- **表示**: 縦の青い線（#0288D1）
- **位置**: SYNC TIMEの時刻に対応する位置
- **初期状態**: null（インジケーター非表示）
- **Story 1スコープ**: 表示のみ、選択・変更はStory 3で実装

### エラー処理
- **ネットワークエラー**: 「再試行」ボタン付きエラー画面を表示
- **データなし**: 空状態UIを表示

---

## 3. 画面フローと状態遷移

機能の詳細な振る舞いと状態遷移については、以下を参照してください。

### 画面内の振る舞い（Level 3）
画面の状態（Loading, Content, Error等）とユーザーアクション:
- **Screen Transition**: [screen-transition.md](./screen-transition.md)

### アプリ全体のインデックス（Level 1）
この機能が全体のどこに位置するか:
- **App Navigation**: [/docs/screen-navigation.md](/docs/screen-navigation.md)

### モジュールナビゲーション（Level 2）
Timeline機能モジュール内の画面遷移:
- **Module Navigation**: [/docs/navigation/timeline-module.md](/docs/navigation/timeline-module.md)

---

## 4. Phase 2実装進捗

**Phase 1完了時に作成し、Phase 2実装中に随時更新します。**

**最終更新**: 2026-01-12

### Shared Layer
- [x] TimelineSyncRepository実装（既存インターフェース使用）
- [x] Build成功（`./gradlew :shared:build`）

### ComposeApp Layer
- [x] TimelineSyncUiState実装
- [x] TimelineSyncIntent実装
- [x] TimelineSyncViewModel実装（MVI pattern）
- [x] UI Components実装（4層構造）
  - [x] TimelineSyncScreen（Screen層）
  - [x] TimelineSyncContainer（Container層）
  - [x] TimelineSyncContent（Content層）
  - [x] WeekCalendar（コンポーネント）
  - [x] ChannelAvatarRow（コンポーネント）
  - [x] SyncTimeDisplay（コンポーネント）
  - [x] TimelineCard（コンポーネント）
  - [x] TimelineBar（コンポーネント）
  - [x] TimelineSyncHeader（コンポーネント）※追加
- [x] ViewModel Tests実装
- [x] DI設定（Koin）
- [x] Build成功（`./gradlew :composeApp:build`）
- [x] 全テスト成功（`./gradlew test`）
- [ ] Phase 3レビュー準備完了

**更新タイミング**:
- Phase 2開始時: このセクションを参照
- Phase 2実装中: 各タスク完了時にチェックボックスを更新
- Phase 3開始時: 全チェック完了を確認

---

## 補足

### 使用するドメインモデル（Phase 0で定義済み）
- `SyncChannel` - チャンネル + 選択ストリーム + 同期状態
- `SelectedStreamInfo` - ストリーム情報（id, title, startTime, endTime, duration）
- `SyncStatus` - 同期状態（NOT_SYNCED, WAITING, READY, OPENED）
- `TimelineSyncRepository` - getChannelVideos()
- `VideoServiceType` - YOUTUBE / TWITCH

### Story 1スコープ外（将来のStory）
- チャンネル追加・削除（Story 2）
- 同期時刻の選択・変更（Story 3）
- 外部アプリ連携（Story 4）

### UIデザイン参照
- GitHub Issue #32 コメント: UIモックアップ画像

### 参照
- **類似機能**: `feature/video_playback/`（MVIパターンの参考）
- **参照ADR**:
  - ADR-002（MVIパターン）
  - ADR-003（4層コンポーネント構造）

---

**作成者**: Claude Code
**作成日**: 2026-01-12
**関連Issue**: #32
**Epic**: Timeline Sync (EPIC-002)
