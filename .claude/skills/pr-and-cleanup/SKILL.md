---
name: pr-and-cleanup
description: "PRを作成し、worktreeを自動でクリーンアップします。ローカル・リモートブランチは保持されます。"
allowed-tools: Bash(git:*), Bash(gh:*), Bash(cd:*), Bash(pwd:*), Bash(bash:*)
---

# PR And Cleanup

開発完了後にPRを作成し、worktreeをクリーンアップする自動化スキルです。

## 機能

1. 現在のworktreeディレクトリとブランチを検出
2. 未コミットの変更がないことを確認
3. リモートにブランチをプッシュ
4. PRを作成（タイトル・本文を対話的に入力）
5. worktreeを削除
6. メインリポジトリに戻る

**重要**: ローカル・リモートブランチは削除されません（保持されます）

## 使用方法

worktreeディレクトリ内で実行：

```
/pr-and-cleanup
```

## オプション

スクリプトを直接実行する場合、以下のオプションが使用可能：

| オプション | 説明 |
|-----------|------|
| `--pr-only` | PRのみ作成（worktree削除なし） |
| `--cleanup-only` | クリーンアップのみ（PRは既に作成済みの場合） |
| `--title "タイトル"` | PRタイトルを事前指定 |
| `--body "本文"` | PR本文を事前指定 |
| `--draft` | ドラフトPRとして作成 |
| `--force` | 未コミットの変更を無視（非推奨） |

**例**:
```bash
bash .claude/skills/pr-and-cleanup/scripts/pr_and_cleanup.sh --draft --title "feat: ユーザー認証を追加"
```

## 前提条件

- worktreeディレクトリ内で実行すること
- すべての変更がコミット済みであること
- gh CLI がインストール・認証済みであること
- リモートブランチがプッシュ済み、または自動プッシュが有効であること

## 実行フロー

```
1. worktreeディレクトリとブランチを検出
2. 未コミットの変更をチェック
3. リモートにブランチをプッシュ
4. gh pr create でPRを作成
5. git worktree remove でworktreeを削除
6. メインリポジトリルートに移動
```

## 注意事項

- PRマージ後にブランチを削除する場合は、別途手動で行ってください
- `--force`オプションは未コミットの変更を失う可能性があるため非推奨です

## 関連スキル

- `create-worktree`: worktreeの作成
- `commit`: 変更をコミット
- `pr`: PRの作成（worktree以外の通常ブランチ用）