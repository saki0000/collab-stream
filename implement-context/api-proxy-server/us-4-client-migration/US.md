# User Story: クライアント側Repository移行

> **Epic**: APIプロキシサーバー移行
> **作成日**: 2026-02-11

---

## User Story

開発者として、クライアントのRepository実装を直接API呼び出しからサーバーAPI経由に移行したい。なぜなら、APIキーをクライアントから除去してセキュリティを向上させたいから。

---

## ゴール

クライアント側のRepository実装をサーバーAPI呼び出しに書き換え、BuildKonfigからAPIキー定義を除去する

---

## 依存

- US-2: 動画詳細 & チャンネル動画APIエンドポイント
- US-3: 検索APIエンドポイント

---

## スコープ

- 既存Repository実装のサーバーAPI呼び出しへの書き換え
- サーバーAPIクライアント（Ktor Client）の設定
- BuildKonfigからYouTube / Twitch APIキー定義を除去
- サーバーURLの設定（環境別: 開発 / 本番）
- 既存のドメインモデルとのマッピング確認

---

## 受け入れ条件

- 全ての動画取得・検索機能がサーバーAPI経由で動作する
- BuildKonfigにYouTube / Twitch APIキーが含まれていない
- 開発環境（localhost）と本番環境（Cloud Run）の切り替えが可能
- 既存のUI・機能が移行前と同様に動作する（回帰テスト通過）
- エラー時のUI表示が適切に機能する

---

## 次のアクション

`/develop` を実行して実装開始
