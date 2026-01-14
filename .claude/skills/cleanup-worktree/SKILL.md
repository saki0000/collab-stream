---
name: cleanup-worktree
description: "worktree削除・クリーンアップ時に使用。ローカル・リモートブランチは保持されます。"
allowed-tools: Bash(git:*), Bash(cd:*), Bash(pwd:*), Bash(bash:*)
---

# Worktree Cleanup

開発完了後にworktreeをクリーンアップする自動化スキルです。PR作成は`/pr`コマンドを使用してください。

## 機能

1. 現在のworktreeディレクトリとブランチを検出
2. 未コミットの変更がないことを確認
3. worktreeを削除
4. メインリポジトリに戻る

**重要**: ローカル・リモートブランチは削除されません（保持されます）

## 使用方法

worktreeディレクトリ内で実行：

```
/cleanup-worktree
```

## オプション

スクリプトを直接実行する場合、以下のオプションが使用可能：

| オプション | 説明 |
|-----------|------|
| `--force` | 未コミットの変更を無視（非推奨） |

**例**:
```bash
bash .claude/skills/cleanup-worktree/scripts/pr_and_cleanup.sh --cleanup-only
```

## 前提条件

- worktreeディレクトリ内で実行すること
- すべての変更がコミット済みであること

## 実行フロー

```
1. worktreeディレクトリとブランチを検出
2. 未コミットの変更をチェック
3. git worktree remove でworktreeを削除
4. メインリポジトリルートに移動
```

## 注意事項

- ブランチを削除する場合は、別途手動で行ってください
- `--force`オプションは未コミットの変更を失う可能性があるため非推奨です

## 関連スキル

- `create-worktree`: worktreeの作成
- `commit`: 変更をコミット
- `/pr`: PRの作成