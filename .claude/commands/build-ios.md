---
allowed-tools: Bash(git worktree*), Bash(xcrun*), Bash(xcodebuild*), Bash(cd*), Bash(./scripts/safe-gradlew.sh*), Bash(open*)
description: worktreeを選択してiOSシミュレータにビルド＆インストール
---

## Context

- Git Worktree一覧: !`git worktree list`
- 利用可能なiOSシミュレータ: !`xcrun simctl list devices available | grep -E "iPhone|iPad" | head -15`
- 起動中のシミュレータ: !`xcrun simctl list devices booted`

## Your task

ユーザーがworktreeとシミュレータを選択し、そのworktree上のコードをiOSシミュレータにビルド＆インストールします。

### 手順

1. **最初に確認（AskUserQuestionで同時に確認）**:
   - どのworktreeを使用するか（上記一覧から選択）
   - どのシミュレータを使用するか（利用可能/起動中一覧から選択）

2. **シミュレータ起動**（起動していない場合）:
   ```bash
   xcrun simctl boot "<シミュレータ名>"
   open -a Simulator
   ```

3. **Kotlin Frameworkビルド**:
   ```bash
   cd <選択したworktreeのパス> && ./scripts/safe-gradlew.sh :composeApp:assembleXCFramework
   ```

4. **iOSアプリビルド＆インストール**:
   ```bash
   cd <worktreeパス>/iosApp && xcodebuild -scheme iosApp \
     -project iosApp.xcodeproj \
     -configuration Debug \
     -destination 'platform=iOS Simulator,name=<シミュレータ名>' \
     -derivedDataPath build \
     build

   xcrun simctl install booted build/Build/Products/Debug-iphonesimulator/iosApp.app
   ```

5. **アプリ起動**:
   ```bash
   xcrun simctl launch booted <Bundle ID>
   ```
   ※ Bundle IDは `iosApp/iosApp/Info.plist` から確認

6. **結果報告**: ビルド成功/失敗をユーザーに報告

### 注意事項

- Kotlin Multiplatformの場合、先にFrameworkをビルドする必要があります
- 実機へのインストールはXcodeでの署名設定が必要なため、このスキルではシミュレータのみ対応
- ビルドには時間がかかる場合があります（初回は特に）
- エラーが発生した場合は、エラーメッセージをユーザーに提示してください
