# 開発ワークフロー

CollabStream では AI（Claude Code）を活用した仕様駆動開発（SDD）を採用。

## Phase 概要

| Phase | 名称 | 内容 |
|-------|------|------|
| 0 | Epic 定義 | 大規模機能（3 Story 以上）の分割 |
| 1 | 仕様定義 | REQUIREMENTS.md、インターフェース定義 |
| 2 | AI 実装 | Claude Code による実装 |
| 3 | レビュー | 仕様適合性、ADR 準拠の確認 |

## クイックスタート

### 新機能開発

- **3 Story 以上** → Phase 0 から（Epic 作成）
- **小規模機能** → Phase 1 から（REQUIREMENTS.md 作成）

### Phase 1: 仕様定義

1. `feature/{feature}/REQUIREMENTS.md` を作成
2. `feature/{feature}/screen-transition.md` を作成
3. 必要に応じて `docs/navigation/{module}-module.md` を作成
4. レビュー合意後に GitHub Issue 作成

### Phase 2: AI 実装

**実装開始前の確認事項**:

1. **`/create-worktree`**: Git worktree で独立環境を準備
2. **`/implement-plan`**: plan ファイルを解析しエージェント起動

これらは ExitPlanMode 後に自動呼び出し。

### Phase 3: レビュー

1. 仕様適合性を確認
2. ADR 準拠を確認
3. PR 作成
4. 実装記録を `docs/context/{issue}/` に保存

## 詳細ガイド

完全なワークフロー詳細: `docs/guides/development-workflow.md`
