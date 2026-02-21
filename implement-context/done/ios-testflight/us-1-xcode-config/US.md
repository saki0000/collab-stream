# US-1: Xcodeプロジェクト設定 & ローカル手動配信

## ゴール

Bundle ID・署名・バージョニングを本番用に設定し、Xcode Archiveで手動TestFlight配信可能にする。

## スコープ

1. `Config.xcconfig` 更新: `PRODUCT_BUNDLE_IDENTIFIER=com.collabstream.app`
2. `Config.local.xcconfig` パターン導入（TEAM_IDを個人環境で管理、`.gitignore`追加）
3. xcschemeを `xcshareddata/xcschemes/` に移動（CI対応）
4. `docs/guides/apple-developer-setup.md` 作成（Apple Developer登録 & App Store Connectアプリ登録手順）
5. `docs/guides/ios-manual-testflight.md` 作成（Xcode Archive → TestFlight手順）

## 変更ファイル

| ファイル | 変更内容 |
|---------|---------|
| `iosApp/Configuration/Config.xcconfig` | Bundle ID変更、TEAM_ID参照方式変更 |
| `iosApp/Configuration/Config.local.xcconfig.template` | 新規作成 |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | xcscheme共有設定 |
| `.gitignore` | `Config.local.xcconfig` 追加 |
| `docs/guides/apple-developer-setup.md` | 新規作成 |
| `docs/guides/ios-manual-testflight.md` | 新規作成 |

## 受け入れ条件

- [ ] Bundle IDが `com.collabstream.app` に変更されている
- [ ] TEAM_IDが `Config.local.xcconfig` で管理されている
- [ ] `Config.local.xcconfig` が `.gitignore` に追加されている
- [ ] xcschemeが `xcshareddata/xcschemes/` に存在する
- [ ] Apple Developer登録ガイドが作成されている
- [ ] 手動TestFlight配信ガイドが作成されている

## 検証方法

```bash
xcodebuild -scheme iosApp -showBuildSettings | grep PRODUCT_BUNDLE_IDENTIFIER
# 期待値: com.collabstream.app
```
