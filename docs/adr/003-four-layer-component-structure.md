# ADR 003: 4層Component構造採用

## Status
Accepted

## Context

Compose Multiplatform でのUI Component設計において、以下の課題に対応する必要がある：

- **責任分離**: 状態管理とUI表示の明確な分離
- **再利用性**: プラットフォーム間での Component 共有
- **テスタビリティ**: 各層の独立したテスト実行
- **保守性**: 機能追加時の影響範囲最小化

Component構造として以下を検討：

1. **フラット構造**: 全てのUIを同一レベルで管理
2. **2層構造**: Stateful/Stateless の分離
3. **3層構造**: Screen/Content/Component の分離
4. **4層構造**: Route/Screen/Content/Component の分離

## Decision

**4層Component構造** を採用する。

### 構造
```
Route (Stateful) → Screen (Stateless) → Content (機能単位) → Component (再利用可能)
```

### 各層の責務

1. **Route**: 状態管理、Navigation、副作用実行
2. **Screen**: 画面レイアウト、Content配置
3. **Content**: 機能領域のUI集約
4. **Component**: アトミックな再利用可能UI

### 採用理由

1. **明確な責任分離**: 各層の役割が明確で理解しやすい
2. **状態管理の一元化**: Route でのみ状態を保持
3. **テスタビリティ**: 各層の独立テストが可能
4. **再利用性**: Component/Content の組み合わせ活用
5. **保守性**: 機能追加時の変更箇所が特定しやすい

## Consequences

### Positive
- **責任明確化**: 各層の役割が明確で迷いが少ない
- **テスト容易性**: Stateless Component の単体テスト
- **再利用促進**: Component/Content の横断的活用
- **Navigation分離**: Route での一元的なNavigation管理
- **副作用制御**: LaunchEffect の適切な配置

### Negative
- **初期複雑性**: シンプルなUIでも4層必要
- **学習コスト**: 4層構造の理解が必要
- **ファイル数増加**: 層分離によるファイル管理コスト

### Neutral
- **パフォーマンス**: 適切な実装で層間オーバーヘッドは最小
- **メモリ使用量**: Stateless Component による効率化