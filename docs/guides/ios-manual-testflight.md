# Xcode Archive → TestFlight 手動配信ガイド

## 概要

Xcodeを使ってCollabStreamのiOSアプリをArchiveし、手動でTestFlightにアップロードする手順。
CI/CD（Fastlane + GitHub Actions）が未設定の場合や、ローカルで確認したい場合に使用。

## 前提条件

- [Apple Developer Program 登録](./apple-developer-setup.md) が完了していること
- App Store Connect にアプリが登録済みであること
- `Config.local.xcconfig` に TEAM_ID が設定済みであること
- Xcode 16.2 以上がインストールされていること

## 1. ビルド前の準備

### 1.1 バージョン番号の確認

`iosApp/Configuration/Config.xcconfig` を確認:

```xcconfig
CURRENT_PROJECT_VERSION=1    # ビルド番号（TestFlightアップロードごとに増やす）
MARKETING_VERSION=1.0        # ユーザー向けバージョン
```

**注意**: TestFlightへのアップロードでは、同じ `MARKETING_VERSION` でも `CURRENT_PROJECT_VERSION` が前回より大きい必要がある。

### 1.2 署名設定の確認

Xcodeでプロジェクトを開き確認:

1. ターゲット「iosApp」→「Signing & Capabilities」
2. 「Automatically manage signing」にチェックが入っていること
3. 「Team」が正しいApple Developerアカウントであること

## 2. Archive の作成

### 2.1 Xcodeでの操作

1. Xcodeで `iosApp.xcodeproj` を開く
2. ビルド先デバイスを **「Any iOS Device (arm64)」** に変更
   - シミュレータが選択されているとArchiveできない
3. メニュー → **Product** → **Archive**
4. Kotlin Frameworkのコンパイルを含むため、初回は時間がかかる（5〜10分程度）

### 2.2 コマンドラインでの操作（オプション）

```bash
cd iosApp

# Kotlin Frameworkのビルド
cd .. && ./gradlew :composeApp:assembleXCFramework && cd iosApp

# Archive作成
xcodebuild archive \
  -project iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Release \
  -archivePath build/CollabStream.xcarchive \
  -destination "generic/platform=iOS"
```

## 3. TestFlight へのアップロード

### 3.1 Xcode Organizer からアップロード

1. Archive完了後、Organizer ウィンドウが自動的に開く
   - 手動で開く場合: メニュー → **Window** → **Organizer**
2. 作成されたArchiveを選択
3. **「Distribute App」** をクリック
4. **「TestFlight & App Store」** を選択 → **「Distribute」**
5. オプション設定（デフォルトのままでOK）:
   - Upload your app's symbols: チェック
   - Manage Version and Build Number: チェック
6. **「Upload」** をクリック
7. アップロード完了を待つ

### 3.2 App Store Connect での確認

1. [App Store Connect](https://appstoreconnect.apple.com/) にアクセス
2. 「マイApp」→「CollabStream」→「TestFlight」タブ
3. アップロードしたビルドが表示される
4. Apple による自動レビュー（通常数分〜数時間）を待つ

## 4. テスターへの配信

### 4.1 内部テスター（最大100人）

1. TestFlight タブ → 「内部テスト」
2. 「+」→ テスターのApple IDメールアドレスを追加
3. ビルドを選択して「テスト開始」

### 4.2 外部テスター（最大10,000人）

1. TestFlight タブ → 「外部テスト」→ グループを作成
2. テスターを追加
3. ビルドを選択 → Betaアプリレビューを申請
4. レビュー通過後、テスターに通知が届く

## トラブルシューティング

### Archive が無効（グレーアウト）

- ビルド先が「Any iOS Device」になっているか確認
- シミュレータが選択されていないか確認

### アップロード時にエラー

| エラー | 原因 | 対処 |
|-------|------|------|
| Invalid Bundle ID | Bundle IDがApp Store Connectと不一致 | Apple Developer PortalでBundle IDを登録 |
| Invalid Code Signing | 署名証明書の問題 | Xcode → Settings → Accounts で証明書を更新 |
| Redundant Binary Upload | 同じビルド番号が既にアップロード済み | `CURRENT_PROJECT_VERSION` を増やす |

### Kotlin Framework のビルドエラー

```bash
# クリーンビルド
cd .. && ./gradlew clean && cd iosApp
# Xcodeのクリーンビルド: Product → Clean Build Folder (Shift+Cmd+K)
```
