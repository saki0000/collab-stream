# PR 説明テンプレート

## 形式

```markdown
## Summary
<1-3 bullet points describing what this PR does>

## Changes
- [ ] 変更内容1
- [ ] 変更内容2
- [ ] 変更内容3

## Related Issues
- Closes #<issue_number>

## Test Plan
- [ ] テスト項目1
- [ ] テスト項目2

## Screenshots (if applicable)
<スクリーンショットがあれば添付>

---
🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

## 記入ガイドライン

### Summary

- 3行以内で変更の要点を説明
- 技術的な詳細より「何が変わるか」を重視
- 日本語で記述

### Changes

- 具体的な変更ファイル・機能をリスト
- チェックボックス形式でレビュアーが確認しやすく

### Related Issues

- 関連する GitHub Issue を参照
- `Closes #123` で自動クローズ

### Test Plan

- レビュアーが検証できる手順を記載
- ユニットテスト、手動テストの両方

### Screenshots

- UI 変更がある場合は必須
- Before/After 比較があるとベター

## 例

```markdown
## Summary
- 動画同期機能のUIを実装
- 同期ボタンと状態表示を追加
- MVIパターンで状態管理

## Changes
- [x] SyncButton コンポーネント追加
- [x] VideoSyncScreen に同期UI統合
- [x] VideoSyncViewModel に同期処理追加
- [x] 同期状態の UiState 定義

## Related Issues
- Closes #45

## Test Plan
- [ ] 同期ボタンをタップして両動画が同じ位置になることを確認
- [ ] 同期状態が UI に正しく表示されることを確認
- [ ] ユニットテストが全て通ることを確認

## Screenshots
<同期UI のスクリーンショット>

---
🤖 Generated with [Claude Code](https://claude.com/claude-code)
```
