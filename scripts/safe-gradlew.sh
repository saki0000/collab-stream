#!/bin/bash
# safe-gradlew.sh - Gradleビルドの重複実行を防止するラッパースクリプト
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

# GradleWorkerMain プロセスの存在チェック（実際にビルド中かどうか）
is_gradle_busy() {
    pgrep -f "GradleWorkerMain" > /dev/null 2>&1
}

if is_gradle_busy; then
    if [[ "$WAIT_MODE" == true ]]; then
        echo "Gradle ビルドが実行中です。終了を待機しています..."
        while is_gradle_busy; do
            sleep 5
        done
        echo "Gradle が空きました。ビルドを開始します。"
    else
        echo "Gradle ビルドが実行中のためスキップします。"
        echo "  スキップしたコマンド: ./gradlew $*"
        exit 0
    fi
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

exec "$GRADLEW" "$@"
