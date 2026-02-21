# User Story: 匿名認証（デバイスID）

> **Epic**: サブスクリプション基盤
> **作成日**: 2026-02-15

---

## User Story

ユーザーとして、アカウント作成なしでアプリを使い始めたい。なぜなら、手軽にサービスを利用でき、必要に応じてサブスクリプションを購入できるようにしたいから。

---

## ゴール

UUID v4ベースのデバイスIDを生成・永続化し、匿名ユーザーとして一意に識別できるようにする。

---

## 依存

- US-1: ドメインモデル & Feature Gate基盤（UserRepositoryインターフェース）

---

## スコープ

- UUID v4によるデバイスID生成
- Room KMPによるデバイスIDの永続化
- `UserRepository` の実装（Android/iOS共通）
- 初回起動時の自動生成フロー

---

## 受け入れ条件

- アプリ初回起動時にUUID v4形式のデバイスIDが自動生成されること
- 2回目以降の起動で同じデバイスIDが返されること
- `UserRepository.getDeviceId()` が正しく動作すること
- `UserRepository.hasDeviceId()` が未生成時false、生成後trueを返すこと

---

## 次のアクション

`/develop` を実行して実装開始
