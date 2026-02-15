# User Story: コメントAPIプロキシエンドポイント

> **Epic**: コメントタイムスタンプ同期
> **作成日**: 2026-02-15

---

## User Story

開発者として、YouTube `commentThreads.list` APIへのプロキシエンドポイントを構築したい。なぜなら、ADR-005 Phase 2に準拠してAPIキーをサーバー側で管理し、クライアントから安全にコメントを取得できるようにするため。

---

## ゴール

`GET /api/videos/{id}/comments` エンドポイントを追加し、YouTube CommentThreads APIへのプロキシを実現する

---

## 依存

- api-proxy-server US-1: 共通基盤 & サーバー骨格構築（Review中）

---

## スコープ

- `GET /api/videos/{id}/comments` エンドポイント追加
- YouTube `commentThreads.list` へのプロキシリクエスト
- クエリパラメータ: `maxResults`（デフォルト100）, `pageToken`, `order`（relevance/time）
- レスポンスをドメインモデル形状に変換して返却
- コメント無効化時の403ハンドリング（`commentsDisabled`エラー）
- APIキーはサーバー側環境変数で管理

---

## 受け入れ条件

- `GET /api/videos/{videoId}/comments` でYouTubeコメントが取得できる
- `maxResults`, `pageToken`, `order` パラメータが正しく機能する
- コメント無効化された動画で適切なエラーレスポンスが返る
- APIキーがレスポンスに露出しない
- 既存テストが壊れていない

---

## 次のアクション

`/develop` を実行して実装開始
