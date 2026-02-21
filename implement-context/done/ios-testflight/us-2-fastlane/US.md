# US-2: Fastlane導入

## ゴール

`fastlane beta` コマンド1つでビルド→TestFlightアップロードを自動化する。

## スコープ

1. `iosApp/Gemfile` 作成（fastlane gem）
2. `iosApp/fastlane/Appfile` 作成（`app_identifier: com.collabstream.app`）
3. `iosApp/fastlane/Fastfile` 作成（`beta` / `build_only` lane）
4. `iosApp/fastlane/Matchfile` 作成（証明書管理、Git Storage方式）
5. `docs/guides/fastlane-setup.md` 作成（セットアップ & 利用ガイド）
6. `.gitignore` に Fastlane関連追加

## 依存

- US-1 完了（Bundle ID・署名設定が前提）

## 変更ファイル

| ファイル | 変更内容 |
|---------|---------|
| `iosApp/Gemfile` | 新規作成 |
| `iosApp/fastlane/Appfile` | 新規作成 |
| `iosApp/fastlane/Fastfile` | 新規作成 |
| `iosApp/fastlane/Matchfile` | 新規作成 |
| `docs/guides/fastlane-setup.md` | 新規作成 |
| `.gitignore` | Fastlane関連追加 |

## 受け入れ条件

- [ ] `Gemfile` にfastlane gemが定義されている
- [ ] `Appfile` にBundle IDとチーム情報が設定されている
- [ ] `Fastfile` に `beta` と `build_only` laneが定義されている
- [ ] `Matchfile` にGit Storage方式の設定がある
- [ ] Fastlaneセットアップガイドが作成されている

## 検証方法

```bash
cd iosApp && bundle exec fastlane build_only
# ビルド成功を確認
```
