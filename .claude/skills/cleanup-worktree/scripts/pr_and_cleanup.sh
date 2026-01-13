#!/bin/bash
# PR And Cleanup Script
# Usage: bash pr_and_cleanup.sh [options]

set -e

# 色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# デフォルト値
PR_ONLY=false
CLEANUP_ONLY=false
PR_TITLE=""
PR_BODY=""
DRAFT=false
FORCE=false

# オプション解析
while [[ $# -gt 0 ]]; do
    case $1 in
        --pr-only)
            PR_ONLY=true
            shift
            ;;
        --cleanup-only)
            CLEANUP_ONLY=true
            shift
            ;;
        --title)
            PR_TITLE="$2"
            shift 2
            ;;
        --body)
            PR_BODY="$2"
            shift 2
            ;;
        --draft)
            DRAFT=true
            shift
            ;;
        --force)
            FORCE=true
            shift
            ;;
        *)
            echo -e "${RED}不明なオプション: $1${NC}"
            exit 1
            ;;
    esac
done

echo -e "${BLUE}=== PR And Cleanup ===${NC}"
echo ""

# 現在のディレクトリ情報を取得
CURRENT_DIR=$(pwd)
GIT_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || echo "")

if [ -z "$GIT_ROOT" ]; then
    echo -e "${RED}エラー: Gitリポジトリ内で実行してください${NC}"
    exit 1
fi

# worktree内かどうか確認
WORKTREE_INFO=$(git worktree list --porcelain | grep -A 2 "worktree $GIT_ROOT" || echo "")
CURRENT_BRANCH=$(git branch --show-current)

echo "現在のディレクトリ: $CURRENT_DIR"
echo "現在のブランチ: $CURRENT_BRANCH"
echo ""

# メインリポジトリのルートを取得
MAIN_ROOT=$(git worktree list | head -1 | awk '{print $1}')

if [ "$GIT_ROOT" = "$MAIN_ROOT" ]; then
    echo -e "${RED}エラー: メインリポジトリ内で実行しています${NC}"
    echo "このスキルはworktree内で実行してください"
    exit 1
fi

# 未コミットの変更チェック
if ! git diff --quiet || ! git diff --cached --quiet; then
    if [ "$FORCE" = true ]; then
        echo -e "${YELLOW}警告: 未コミットの変更がありますが、--forceオプションにより続行します${NC}"
    else
        echo -e "${RED}エラー: 未コミットの変更があります${NC}"
        echo "先にコミットするか、--forceオプションを使用してください（非推奨）"
        git status --short
        exit 1
    fi
fi

# PRを作成（--cleanup-only でない場合）
if [ "$CLEANUP_ONLY" = false ]; then
    echo -e "${YELLOW}1. リモートにプッシュ中...${NC}"
    git push -u origin "$CURRENT_BRANCH"
    echo -e "${GREEN}   プッシュ完了${NC}"
    echo ""

    echo -e "${YELLOW}2. PRを作成中...${NC}"

    # gh pr create コマンドを構築
    GH_CMD="gh pr create"

    if [ -n "$PR_TITLE" ]; then
        GH_CMD="$GH_CMD --title \"$PR_TITLE\""
    fi

    if [ -n "$PR_BODY" ]; then
        GH_CMD="$GH_CMD --body \"$PR_BODY\""
    fi

    if [ "$DRAFT" = true ]; then
        GH_CMD="$GH_CMD --draft"
    fi

    # PRを作成（対話的に）
    if [ -n "$PR_TITLE" ] && [ -n "$PR_BODY" ]; then
        eval "$GH_CMD"
    elif [ "$DRAFT" = true ]; then
        gh pr create --draft
    else
        gh pr create
    fi

    echo -e "${GREEN}   PR作成完了${NC}"
    echo ""
fi

# worktreeを削除（--pr-only でない場合）
if [ "$PR_ONLY" = false ]; then
    echo -e "${YELLOW}3. worktreeを削除中...${NC}"

    WORKTREE_PATH="$GIT_ROOT"

    # メインリポジトリに移動
    cd "$MAIN_ROOT"

    # worktreeを削除
    git worktree remove "$WORKTREE_PATH"
    echo -e "${GREEN}   worktree削除完了${NC}"
    echo ""

    echo -e "${GREEN}=== 完了 ===${NC}"
    echo ""
    echo "メインリポジトリに戻りました: $MAIN_ROOT"
    echo "ブランチ '$CURRENT_BRANCH' はローカル・リモートともに保持されています"
    echo ""
    echo "現在のworktree一覧:"
    git worktree list
else
    echo -e "${GREEN}=== PR作成完了 ===${NC}"
    echo ""
    echo "worktreeは削除されていません（--pr-onlyオプション）"
    echo "手動で削除する場合: git worktree remove $GIT_ROOT"
fi