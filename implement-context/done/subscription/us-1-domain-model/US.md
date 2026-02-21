# User Story: ドメインモデル & Feature Gate基盤

> **Epic**: サブスクリプション基盤
> **作成日**: 2026-02-15

---

## User Story

開発者として、サブスクリプション関連のドメインモデルとインターフェースを定義したい。なぜなら、後続のUS（認証・課金・UI・サーバー）が共通の型とインターフェースに依存するから。

---

## ゴール

サブスクリプション基盤の共通ドメインモデルとRepositoryインターフェースをsharedモジュールに定義する。

---

## 依存

- なし（最初に着手するUS）

---

## スコープ

- `SubscriptionTier` enum（FREE / PRO）
- `SubscriptionStatus` data class（現在のサブスクリプション状態）
- `Feature` enum（Pro限定機能識別子、Phase 0では空）
- `FeatureGate` interface（機能利用可否の判定）
- `SubscriptionRepository` interface（課金処理・状態管理）
- `UserRepository` interface（デバイスID管理）

---

## 受け入れ条件

- `./gradlew :shared:compileKotlinMetadata` がエラーなく通ること
- 全モデルに `@Serializable` アノテーションが付与されていること
- Repositoryインターフェースが既存パターン（suspend + Result<T> + Flow）に準拠していること
- UseCase・Repository実装・ViewModel・UIが含まれていないこと

---

## 次のアクション

`/develop` を実行して実装開始
