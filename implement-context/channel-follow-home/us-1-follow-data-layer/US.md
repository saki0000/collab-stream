# User Story: チャンネルフォロー データ層

> **Epic**: チャンネルフォロー & アーカイブHome
> **作成日**: 2026-02-11

---

## User Story

ユーザーとして、よく見るチャンネルをフォローして保存したい。なぜなら、毎回検索する手間を省いて素早くアーカイブにアクセスしたいから。

---

## ゴール

`FollowedChannel` Entityと`ChannelFollowRepository` Interfaceを定義し、Room KMPで永続化する

---

## 依存

- なし（既存Room KMP基盤 `AppDatabase` を利用）

---

## スコープ

- `FollowedChannel` ドメインモデル定義（`shared/domain/model/`）
- `ChannelFollowRepository` インターフェース定義（`shared/domain/repository/`）
- `FollowedChannelEntity` Room Entity定義（`shared/data/local/entity/`）
- `FollowedChannelDao` Room DAO定義（`shared/data/local/`）
- `AppDatabase` にDAO追加（マイグレーション対応）
- `ChannelFollowRepositoryImpl` 実装（`shared/data/repository/`）
- DIモジュール登録（Koin）

---

## 受け入れ条件

- `FollowedChannel` がchannelId、channelName、channelIconUrl、serviceType、followedAtを持つ
- フォロー追加・解除・一覧取得・フォロー状態確認が可能
- フォロー一覧をFlowで監視できる
- Room DAOテストが通過する
- 既存テストが壊れていない
- `./gradlew :shared:compileKotlinJvm` が成功する

---

## 次のアクション

`/develop` を実行して実装開始
