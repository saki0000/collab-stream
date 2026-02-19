# US-3: GitHub Actions iOS CI/CDパイプライン

## ゴール

PRでiOSビルド検証、mainマージでTestFlight自動配信を実現する。

## スコープ

1. `.github/workflows/ios-ci.yml` — PR時のiOSビルド検証（`macos-15`ランナー）
2. `.github/workflows/ios-deploy-testflight.yml` — mainマージ時のTestFlight配信
3. ビルド番号自動インクリメント（`github.run_number`方式）
4. パスフィルター（`iosApp/**`, `composeApp/**`, `shared/**` 変更時のみ実行）
5. `docs/guides/github-secrets-ios.md` 作成（必要なSecrets一覧と設定手順）

## 依存

- US-2 完了（Fastlane設定が前提）

## 変更ファイル

| ファイル | 変更内容 |
|---------|---------|
| `.github/workflows/ios-ci.yml` | 新規作成 |
| `.github/workflows/ios-deploy-testflight.yml` | 新規作成 |
| `docs/guides/github-secrets-ios.md` | 新規作成 |

## 必要なGitHub Secrets

| Secret名 | 用途 |
|----------|------|
| `MATCH_GIT_BASIC_AUTHORIZATION` | Match証明書リポジトリアクセス |
| `MATCH_PASSWORD` | Match証明書復号パスワード |
| `APP_STORE_CONNECT_API_KEY_ID` | ASC API Key ID |
| `APP_STORE_CONNECT_ISSUER_ID` | ASC Issuer ID |
| `APP_STORE_CONNECT_API_KEY_CONTENT` | ASC API Key (.p8) 内容 |
| `APPLE_TEAM_ID` | Apple Developer Team ID |

## 受け入れ条件

- [ ] PR作成時にiOSビルド検証が自動実行される
- [ ] mainマージ時にTestFlight配信が自動実行される
- [ ] ビルド番号が自動インクリメントされる
- [ ] 関連ファイル変更時のみワークフローが実行される
- [ ] GitHub Secrets設定ガイドが作成されている

## 検証方法

- GitHub ActionsのCI実行ログで確認
