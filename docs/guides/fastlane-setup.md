# Fastlane セットアップ & 利用ガイド

## 概要

CollabStreamのiOSアプリのビルド・配信を自動化するためのFastlane設定ガイド。
ローカル開発環境でのセットアップからCI/CDでの利用方法まで。

## 前提条件

- Ruby 3.0 以上がインストールされていること
- [Apple Developer Program 登録](./apple-developer-setup.md) が完了していること
- Xcode 16.2 以上がインストールされていること

## 1. 初期セットアップ

### 1.1 Ruby環境の確認

```bash
ruby --version
# ruby 3.x.x が表示されること
```

macOSのシステムRubyではなく、rbenvやasdf経由でのインストールを推奨。

### 1.2 Bundlerのインストール

```bash
gem install bundler
```

### 1.3 Fastlaneのインストール

```bash
cd iosApp
bundle install
```

`Gemfile.lock` が生成される（`.gitignore` で除外済み）。

## 2. Fastlane Match（証明書管理）のセットアップ

### 2.1 証明書リポジトリの作成

GitHubにプライベートリポジトリを作成:
- リポジトリ名: `collabstream-certificates`（例）
- Visibility: **Private**

### 2.2 Matchfile の更新

`iosApp/fastlane/Matchfile` を編集し、`git_url` を設定:

```ruby
git_url("https://github.com/YOUR_ORG/collabstream-certificates.git")
```

### 2.3 Match の初期化

```bash
cd iosApp

# 開発用証明書の作成
bundle exec fastlane match development

# App Store配布用証明書の作成
bundle exec fastlane match appstore
```

初回実行時にパスフレーズの設定を求められる。このパスフレーズはCI環境で `MATCH_PASSWORD` として使用。

## 3. 利用可能なコマンド

### ビルド検証のみ

```bash
cd iosApp
bundle exec fastlane build_only
```

CIでのビルド検証やローカルでの動作確認に使用。署名なしでビルドが通ることを確認。

### TestFlight 配信

```bash
cd iosApp
bundle exec fastlane beta
```

以下を順番に実行:
1. App Store Connect API Key の設定（CI環境のみ）
2. Match で証明書を取得
3. Kotlin Framework のビルド
4. アプリのビルド & 署名
5. TestFlight にアップロード

### ビルド番号を指定して配信

```bash
cd iosApp
bundle exec fastlane beta build_number:42
```

CI環境では `github.run_number` をビルド番号として渡す。

### 証明書の同期

```bash
# 開発用
cd iosApp
bundle exec fastlane sync_certificates_development

# App Store配布用
cd iosApp
bundle exec fastlane sync_certificates_appstore
```

## 4. ディレクトリ構成

```
iosApp/
├── Gemfile                  # Ruby依存関係
├── fastlane/
│   ├── Appfile              # アプリ識別子設定
│   ├── Fastfile             # Lane定義（メイン設定）
│   └── Matchfile            # 証明書管理設定
└── ...
```

## 5. 環境変数一覧

CI/CD環境で必要な環境変数:

| 変数名 | 用途 | 設定場所 |
|-------|------|---------|
| `APP_STORE_CONNECT_API_KEY_ID` | ASC API Key ID | GitHub Secrets |
| `APP_STORE_CONNECT_ISSUER_ID` | ASC Issuer ID | GitHub Secrets |
| `APP_STORE_CONNECT_API_KEY_CONTENT` | ASC API Key 内容（Base64） | GitHub Secrets |
| `MATCH_GIT_BASIC_AUTHORIZATION` | 証明書リポジトリ認証 | GitHub Secrets |
| `MATCH_PASSWORD` | Match パスフレーズ | GitHub Secrets |

## トラブルシューティング

### `bundle install` でエラー

```bash
# Bundlerの再インストール
gem install bundler
bundle install --path vendor/bundle
```

### Match で認証エラー

- 証明書リポジトリへのアクセス権限を確認
- `MATCH_GIT_BASIC_AUTHORIZATION` が正しく設定されているか確認
  - 値: `echo -n "username:personal_access_token" | base64`

### ビルドエラー: Kotlin Framework が見つからない

```bash
# Kotlin Frameworkを先にビルド
cd ..
./gradlew :composeApp:assembleXCFramework
cd iosApp
bundle exec fastlane build_only
```
