# ADR 002: MVI パターン採用

## Status
Accepted

## Context

CollabStreamのプレゼンテーション層では複雑な状態管理が必要：

- **2動画同時管理**: Primary + Secondary ストリームの状態
- **同期状態追跡**: リアルタイムな同期ステータスの管理
- **エラーハンドリング**: API障害、ネットワークエラーの処理
- **UI状態管理**: ローディング、コントロール表示状態

状態管理パターンとして以下を検討：

1. **MVI (Model-View-Intent)**: 一方向データフローによる状態管理
2. **MVVM + LiveData**: Android標準の状態管理
3. **Redux Pattern**: 予測可能な状態変更
4. **Compose State**: Compose Multiplatform の標準状態管理

## Decision

**MVI (Model-View-Intent) パターン** を採用する。

### 基本構造
```
View → Intent → ViewModel → State → View
```

### 採用理由

1. **動画同期に最適**: 複雑な状態変化を予測可能な形で管理
2. **一方向データフロー**: 状態変更の追跡が容易
3. **テスタビリティ**: State/Intent の単体テストが簡単
4. **リアクティブ**: StateFlow による自動的なUI更新
5. **デバッグ**: 状態変化履歴の追跡が可能

## Consequences

### Positive
- **予測可能性**: 状態変更パターンが明確
- **保守性**: Intent → State 変換ロジックが集約
- **テスト容易性**: 純粋関数としてのReducer テスト
- **タイムトラベル**: 状態履歴の追跡とロールバック
- **Compose親和性**: State-driven UI との整合性

### Negative
- **学習コスト**: MVI パターンの理解が必要
- **初期実装コスト**: State/Intent 定義の工数
- **ボイラープレート**: Intent ごとの処理定義が必要

### Neutral
- **パフォーマンス**: StateFlow の効率的な状態伝播
- **メモリ使用量**: State オブジェクトの適切な管理で最適化