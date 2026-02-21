#!/bin/bash
# safe-gradlew.sh - Gradleビルドの重複実行を防止するラッパースクリプト
#
# flock（カーネルレベルのファイルロック）により、worktree間でのGradle同時実行を防止する。
# - アトミックなロック取得（レースコンディションなし）
# - プロセス終了時にカーネルが自動解放（stale lock なし）
# - CollabStreamプロジェクト専用スコープ（他プロジェクトの誤検出なし）
#
# 前提: brew install util-linux
#
# 使い方:
#   ./scripts/safe-gradlew.sh [--wait] <gradlew引数...>
#
# オプション:
#   --wait  Gradleが実行中の場合、終了を待ってから実行する（デフォルト: スキップ）
#
# 例:
#   ./scripts/safe-gradlew.sh :shared:build :shared:test
#   ./scripts/safe-gradlew.sh --wait :composeApp:build

set -euo pipefail

LOCKFILE="/tmp/collabstream_gradle.lock"
WAIT_MODE=false

if [[ "${1:-}" == "--wait" ]]; then
    WAIT_MODE=true
    shift
fi

if [[ $# -eq 0 ]]; then
    echo "エラー: gradlew の引数を指定してください"
    echo "例: ./scripts/safe-gradlew.sh :shared:build"
    exit 1
fi

# flock コマンドを検出
if command -v flock &>/dev/null; then
    FLOCK_CMD="flock"
elif [[ -x "$(brew --prefix util-linux 2>/dev/null)/bin/flock" ]]; then
    FLOCK_CMD="$(brew --prefix util-linux)/bin/flock"
else
    echo "エラー: flock が見つかりません。以下を実行してください:"
    echo "  brew install util-linux"
    exit 1
fi

# プロジェクトルートを検出（gradlew がある場所まで遡る）
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# worktree 内から実行された場合、カレントディレクトリの gradlew を使う
if [[ -f "./gradlew" ]]; then
    GRADLEW="./gradlew"
elif [[ -f "$PROJECT_ROOT/gradlew" ]]; then
    GRADLEW="$PROJECT_ROOT/gradlew"
else
    echo "エラー: gradlew が見つかりません"
    exit 1
fi

# ロックファイルを作成（存在しなければ）
touch "$LOCKFILE"

if [[ "$WAIT_MODE" == true ]]; then
    # --wait: ロック取得まで待機してから実行
    echo "ロック取得を試行中..."
    exec "$FLOCK_CMD" "$LOCKFILE" "$GRADLEW" "$@"
else
    # デフォルト: ロック取得失敗ならスキップ
    "$FLOCK_CMD" --nonblock "$LOCKFILE" "$GRADLEW" "$@" || {
        EXIT_CODE=$?
        if [[ $EXIT_CODE -eq 1 ]]; then
            echo "Gradle ビルドが実行中のためスキップします。"
            echo "  スキップしたコマンド: ./gradlew $*"
            echo "  待機して実行するには: ./scripts/safe-gradlew.sh --wait $*"
            exit 0
        else
            exit $EXIT_CODE
        fi
    }
fi
