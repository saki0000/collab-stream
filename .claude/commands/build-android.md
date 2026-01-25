---
allowed-tools: Bash(git worktree*), Bash(adb*), Bash(cd*), Bash(./gradlew*)
description: worktreeを選択してAndroidエミュレータ/実機にビルド＆インストール
---

## Context

- Git Worktree一覧: !`git worktree list`
- 接続中のAndroidデバイス: !`adb devices -l`

## Your task

ユーザーがworktreeとデバイスを選択し、そのworktree上のコードをAndroidデバイスにビルド＆インストールします。

### 手順

1. **最初に確認（AskUserQuestionで同時に確認）**:
   - どのworktreeを使用するか（上記一覧から選択）
   - どのデバイスにインストールするか（接続中デバイス一覧から選択）

2. **デバイス接続確認**:
   - 選択されたデバイスが接続されていない場合はエラーを報告
   - デバイスが0台の場合: エミュレータ起動または実機接続を促して終了

3. **ビルド＆インストール実行**:
   - デバイスが1台のみの場合:
     ```bash
     cd <選択したworktreeのパス> && ./gradlew :composeApp:installDebug
     ```
   - 複数台から特定デバイスを指定する場合:
     ```bash
     cd <worktreeパス> && ./gradlew :composeApp:assembleDebug
     adb -s <DEVICE_ID> install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
     ```

4. **結果報告**: ビルド成功/失敗をユーザーに報告

### 注意事項

- worktreeのパスは `git worktree list` の出力から取得してください
- DEVICE_IDは `adb devices` の出力の1列目（例: emulator-5554, 1A2B3C4D）
- ビルドには時間がかかる場合があります（初回は特に）
- エラーが発生した場合は、エラーメッセージをユーザーに提示してください
