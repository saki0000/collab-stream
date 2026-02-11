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
4. Claude Code設定ファイル（.claude/settings.local.json）を自動コピー

## 使用方法

```
/create-worktree <feature-name>
```

**例**: `/create-worktree user-authentication`

## 引数がない場合

ARGUMENTSが空、または feature-name が指定されていない場合は、以下の手順で対応すること。

### 1. コンテキストから提案候補を収集

以下の情報源からブランチ名の候補を生成する:

- **implement-context/**: 進行中のUS・Epicからまだworktreeがないものを確認
- **直前の会話コンテキスト**: planモードで議論した機能名がある場合はそれを優先

### 2. AskUserQuestion で質問

- header: `Branch名`
- question: 「作成するworktreeのfeature名を選択または入力してください（ケバブケース推奨）」
- options: 収集した候補を最大3つまでオプションとして提示（labelにケバブケースのfeature名、descriptionにUS/Epic名を記載）
- ユーザーは「その他」から自由入力も可能
- ユーザーが入力/選択した値を `<feature-name>` として使用する

## 実行内容

スクリプト: `bash .claude/skills/create-worktree/scripts/create_worktree.sh <feature-name>`

1. プロジェクトルートに`.worktrees/`ディレクトリを作成（存在しない場合）
2. `git worktree add -b feature/<feature-name> .worktrees/<feature-name> main`を実行
3. 環境ファイルをコピー（存在する場合のみ）:
   - `.env` → `.worktrees/<feature-name>/.env`
   - `server/.env` → `.worktrees/<feature-name>/server/.env`
   - `composeApp/.env` → `.worktrees/<feature-name>/composeApp/.env`

## コピーされるファイル

### 環境ファイル

| 元ファイル | コピー先 |
|-----------|---------|
| `.env` | `.worktrees/<feature-name>/.env` |
| `server/.env` | `.worktrees/<feature-name>/server/.env` |
| `composeApp/.env` | `.worktrees/<feature-name>/composeApp/.env` |

### Claude Code設定

| 元ファイル | コピー先 |
|-----------|---------|
| `.claude/settings.local.json` | `.worktrees/<feature-name>/.claude/settings.local.json` |

**Note**: `settings.local.json`には「accept edits on」などのユーザー固有の許可設定が含まれます。これをコピーすることで、worktreeでも同じ権限設定が適用されます。

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