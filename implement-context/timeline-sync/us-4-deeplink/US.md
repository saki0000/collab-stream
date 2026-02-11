# User Story: 外部アプリ連携（DeepLink）

> **Epic**: Timeline Sync
> **作成日**: 2026-01-12
> **移行元**: GitHub Issue #54

---

## User Story

ユーザーとして、ボタン一つで各配信を指定時刻から視聴開始したい。なぜなら、計算された再生位置で各プラットフォームの公式アプリを開けると、同期視聴がスムーズになるから。

---

## ゴール

計算された再生位置で各プラットフォームの公式アプリを開く

---

## 依存

- US-1: タイムライン基本表示 ✅（完了）
- US-2: チャンネル追加・管理 ✅（完了）
- US-3: 同期時刻計算と表示 ✅（完了）

---

## 成果物

- Open/Waitボタンの実装（現在placeholder）
- expect/actualパターンによるDeepLink実装
- プラットフォーム固有のDeepLink処理（Android/iOS）
- 外部アプリ未インストール時のフォールバック処理

---

## 技術詳細

- **YouTube DeepLink**: `youtube://watch?v={ID}&t={SECONDS}`
- **YouTube フォールバック**: `https://www.youtube.com/watch?v={ID}&t={SECONDS}s`
- **Twitch DeepLink**: `twitch://video/{ID}?t={SECONDS}s`
- **Twitch フォールバック**: `https://www.twitch.tv/videos/{VIDEO_ID}?t={SECONDS}s`
- expect/actualパターンでプラットフォーム固有実装を分離
- SyncStatus.READY時のみOpenボタンを有効化
- アプリ未インストール時はWebブラウザにフォールバック

---

## 現在の実装状態

- SPECIFICATION.md: 作成済み（`feature/timeline_sync/SPECIFICATION.md` 内 Story 4セクション）
- コード骨格: `NavigateToExternalApp` Intent・SideEffect定義済み（placeholder）
- TimelineCard: Open/Waitボタンの枠組みあり（`/* Story 4 */` コメント）

---

## 次のアクション

`/develop` を実行して実装開始
