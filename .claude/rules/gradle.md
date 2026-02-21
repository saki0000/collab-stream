# Gradle 実行ルール

## safe-gradlew.sh の使用

**Gradle タスクの実行には必ず `./scripts/safe-gradlew.sh` を使用すること。`./gradlew` を直接実行しない。**

### 理由
- Gradle ビルドの重複実行を防止
- worktree 環境での適切な gradlew 検出

### 使い方

```bash
# 通常実行（ビルド中ならスキップ）
./scripts/safe-gradlew.sh <タスク>

# 待機モード（ビルド中なら終了を待つ）
./scripts/safe-gradlew.sh --wait <タスク>
```

### 例

```bash
./scripts/safe-gradlew.sh :shared:build
./scripts/safe-gradlew.sh --wait test
./scripts/safe-gradlew.sh :composeApp:assembleDebug
```
