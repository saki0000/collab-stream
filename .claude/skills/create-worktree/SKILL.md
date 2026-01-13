---
name: create-worktree
description: |
  Auto-triggers on: "実装を開始", "計画が承認", "実装して", "コーディング開始", "start implementation", "実装開始"

  MUST BE CALLED when plan is approved. Git worktreeを作成して独立した開発環境を準備。
  ExitPlanMode後に自動呼び出し。使用タイミング: 計画承認後、実装開始時。
allowed-tools: Bash(git:*), Bash(mkdir:*), Bash(cp:*), Bash(chmod:*), Bash(bash:*), Bash(ls:*)
---

# Git Worktree Creator

プランモード完了後に、機能開発用の独立したworktreeを自動作成します。

## 機能

1. `.worktrees/<feature-name>/`ディレクトリにworktreeを作成
2. `feature/<feature-name>`ブランチを新規作成（mainから分岐）
3. 環境ファイル（.env等）を自動コピー

## 使用方法

```
/create-worktree <feature-name>
```

**例**: `/create-worktree user-authentication`

## 実行内容

スクリプト: `bash .claude/skills/create-worktree/scripts/create_worktree.sh <feature-name>`

1. プロジェクトルートに`.worktrees/`ディレクトリを作成（存在しない場合）
2. `git worktree add -b feature/<feature-name> .worktrees/<feature-name> main`を実行
3. 環境ファイルをコピー（存在する場合のみ）:
   - `.env` → `.worktrees/<feature-name>/.env`
   - `server/.env` → `.worktrees/<feature-name>/server/.env`
   - `composeApp/.env` → `.worktrees/<feature-name>/composeApp/.env`

## コピーされる環境ファイル

| 元ファイル | コピー先 |
|-----------|---------|
| `.env` | `.worktrees/<feature-name>/.env` |
| `server/.env` | `.worktrees/<feature-name>/server/.env` |
| `composeApp/.env` | `.worktrees/<feature-name>/composeApp/.env` |

## 作業完了後

開発が完了したら、以下の方法でクリーンアップしてください：

1. `/pr`コマンドでPRを作成
2. `/cleanup-worktree`スキルでworktreeを削除（または手動で`git worktree remove .worktrees/<feature-name>`）

## 注意事項

- feature-nameにはケバブケース（例: `user-auth`, `video-player-fix`）を推奨
- 同じ名前のブランチが既に存在する場合はエラーになります
- worktree内での作業はメインリポジトリとは独立して行えます

## 関連スキル

- `cleanup-worktree`: worktree削除
- `commit`: 変更をコミット
- `/pr`: PRの作成