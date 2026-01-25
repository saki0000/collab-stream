#!/bin/bash
# Git Worktree Creator Script
# Usage: bash create_worktree.sh <feature-name>

set -e

# 色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 引数チェック
if [ -z "$1" ]; then
    echo -e "${RED}エラー: feature-nameを指定してください${NC}"
    echo "使用方法: bash create_worktree.sh <feature-name>"
    echo "例: bash create_worktree.sh user-authentication"
    exit 1
fi

FEATURE_NAME="$1"
BRANCH_NAME="feature/${FEATURE_NAME}"
WORKTREE_PATH=".worktrees/${FEATURE_NAME}"

# プロジェクトルートに移動（gitルートを基準）
PROJECT_ROOT=$(git rev-parse --show-toplevel)
cd "$PROJECT_ROOT"

echo -e "${BLUE}=== Git Worktree Creator ===${NC}"
echo ""

# ブランチが既に存在するかチェック
if git show-ref --verify --quiet "refs/heads/${BRANCH_NAME}"; then
    echo -e "${RED}エラー: ブランチ '${BRANCH_NAME}' は既に存在します${NC}"
    echo "別の名前を指定するか、既存のブランチを削除してください"
    exit 1
fi

# worktreeが既に存在するかチェック
if [ -d "$WORKTREE_PATH" ]; then
    echo -e "${RED}エラー: worktree '${WORKTREE_PATH}' は既に存在します${NC}"
    echo "別の名前を指定するか、既存のworktreeを削除してください"
    echo "削除コマンド: git worktree remove ${WORKTREE_PATH}"
    exit 1
fi

# .worktreesディレクトリ作成
echo -e "${YELLOW}1. .worktreesディレクトリを確認中...${NC}"
if [ ! -d ".worktrees" ]; then
    mkdir -p .worktrees
    echo "   .worktrees/ を作成しました"
else
    echo "   .worktrees/ は既に存在します"
fi

# worktree作成
echo ""
echo -e "${YELLOW}2. worktreeを作成中...${NC}"
echo "   パス: ${WORKTREE_PATH}"
echo "   ブランチ: ${BRANCH_NAME}"
git worktree add -b "${BRANCH_NAME}" "${WORKTREE_PATH}" main
echo -e "   ${GREEN}worktreeを作成しました${NC}"

# 環境ファイルのコピー
echo ""
echo -e "${YELLOW}3. 環境ファイルをコピー中...${NC}"

copy_env_file() {
    local src="$1"
    local dest="$2"
    if [ -f "$src" ]; then
        # 宛先ディレクトリが存在しない場合は作成
        local dest_dir=$(dirname "$dest")
        if [ ! -d "$dest_dir" ]; then
            mkdir -p "$dest_dir"
        fi
        cp "$src" "$dest"
        echo -e "   ${GREEN}コピー完了${NC}: $src -> $dest"
    else
        echo "   スキップ: $src (存在しません)"
    fi
}

# ルートの.env
copy_env_file ".env" "${WORKTREE_PATH}/.env"

# server/.env
copy_env_file "server/.env" "${WORKTREE_PATH}/server/.env"

# composeApp/.env
copy_env_file "composeApp/.env" "${WORKTREE_PATH}/composeApp/.env"

# Claude Code設定ファイルのコピー
echo ""
echo -e "${YELLOW}4. Claude Code設定をコピー中...${NC}"

# .claude/settings.local.json をコピー（ユーザー固有の許可設定）
if [ -f ".claude/settings.local.json" ]; then
    mkdir -p "${WORKTREE_PATH}/.claude"
    cp ".claude/settings.local.json" "${WORKTREE_PATH}/.claude/settings.local.json"
    echo -e "   ${GREEN}コピー完了${NC}: .claude/settings.local.json"
else
    echo "   スキップ: .claude/settings.local.json (存在しません)"
fi

# 完了メッセージ
echo ""
echo -e "${GREEN}=== 完了 ===${NC}"
echo ""
echo "worktreeが作成されました:"
echo "  パス: ${WORKTREE_PATH}"
echo "  ブランチ: ${BRANCH_NAME}"
echo ""
echo "次のステップ:"
echo "  1. cd ${WORKTREE_PATH}"
echo "  2. 開発作業を行う"
echo "  3. 変更をコミット"
echo "  4. /pr-and-cleanup でPR作成とクリーンアップ"
echo ""
echo "現在のworktree一覧:"
git worktree list