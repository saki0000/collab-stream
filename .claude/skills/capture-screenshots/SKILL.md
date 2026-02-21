---
name: capture-screenshots
description: "Roborazziを使用してUIスクリーンショットを取得し、ScreenとComponentを分類して保存します。UI成果物の確認時に使用してください。"
allowed-tools: Bash(./scripts/*), Bash(bash:*), Bash(mkdir:*), Bash(cp:*), Bash(ls:*), Bash(find:*), Read, Glob
---

# Screenshot Capture & Organizer

Roborazziを使用して@Previewアノテーション付きComposable関数のスクリーンショットを取得し、ScreenとComponentを自動分類します。

## 機能

1. Roborazziでスクリーンショットを記録
2. ファイル名パターンに基づいてScreenとComponentを分類
3. 分類結果を報告

## 使用方法

```
/capture-screenshots
```

## 実行内容

スクリプト: `bash .claude/skills/capture-screenshots/scripts/organize_screenshots.sh`

### Step 1: スクリーンショット記録

```bash
./gradlew :composeApp:recordRoborazziDebug
```

### Step 2: 分類・整理

| 分類条件 | 保存先 | git管理 |
|---------|--------|---------|
| ファイル名に`Screen`を含む | `screenshots/screens/` | **対象** |
| それ以外 | `screenshots/components/` | **対象外** |

### Step 3: 結果確認

```bash
ls -la screenshots/screens/
ls -la screenshots/components/
```

## 出力ディレクトリ

```
screenshots/
├── screens/          # git管理対象（Screenのスクリーンショット）
│   └── *.png
└── components/       # .gitignore対象
    └── *.png
```

## 分類ロジック

以下の命名規則に基づいてScreenを識別：

- **Screen**: ファイル名に`Screen`を含む（例: `HomeScreen`, `VideoScreen`）
- **Component**: それ以外（`Container`, `Content`, `Item`, `Card`など）

## 注意事項

- スクリーンショットはDebugビルドで生成されます
- `screenshots/components/`はgitignoreされています
- 初回実行時はビルドに時間がかかる場合があります

## 関連コマンド

- `./gradlew :composeApp:recordRoborazziDebug` - スクリーンショット記録
- `./gradlew :composeApp:verifyRoborazziDebug` - スクリーンショット検証
- `./gradlew :composeApp:compareRoborazziDebug` - 差分比較
