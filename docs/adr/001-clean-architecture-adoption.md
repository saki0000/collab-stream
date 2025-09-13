# ADR 001: Android Architecture採用

## Status
Accepted

## Context

CollabStreamは動画同期サービスとして以下の要件を満たす必要がある：

- **API統合**: YouTube API、Twitch API との連携
- **同期ロジック**: URL パラメータ変更による簡単な時間同期
- **マルチプラットフォーム**: Android、iOS、Web、Server での動作
- **将来拡張**: アカウント機能、履歴機能の追加予定

アーキテクチャパターンとして以下の選択肢を検討した：

1. **Android Architecture (Domain層オプショナル)**: UI Layer ← (Domain Layer) ← Data Layer
2. **Clean Architecture**: 3層構造による責任分離
3. **Hexagonal Architecture**: Ports & Adapters による依存性管理
4. **Event-Driven Architecture**: イベントベースの状態管理

## Decision

**Android Architecture（Domain層オプショナル）** を採用する。

### 構造
```
UI Layer → Data Layer
(Domain Layer は必要時に追加)
```

### 採用理由

1. **適切な複雑さ**: Google曰く"Many apps aren't complex enough to justify having a domain layer"
2. **シンプルなロジック**: URL パラメータ変更のみの同期処理
3. **過剰設計回避**: 必要以上の抽象化を避ける
4. **Android標準**: Google推奨アーキテクチャに準拠
5. **段階的拡張**: 必要時にDomain層を後から追加可能

## Consequences

### Positive
- **シンプル構造**: 2層のみで理解しやすい
- **実装効率**: 過剰な抽象化を避けて開発速度向上
- **Google準拠**: Android公式推奨アーキテクチャ
- **段階的拡張**: 複雑になったらDomain層を後から追加
- **低学習コスト**: Android開発者には馴染みのある構造

### Negative
- **ビジネスロジック分散**: UI LayerとData Layerに散らばる可能性
- **将来移行**: 複雑化時のDomain層追加でリファクタリング
- **テスト複雑**: Repository直接テストが必要

### Neutral
- **適用範囲**: CollabStreamの規模には最適
- **技術的負債**: 適切に実装すれば問題なし