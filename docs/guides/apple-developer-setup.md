# Apple Developer Program 登録 & App Store Connect 設定ガイド

## 概要

CollabStreamのiOSアプリをTestFlight配信するために必要な、Apple Developer Programへの登録とApp Store Connectでのアプリ登録手順。

## 前提条件

- Apple ID を持っていること
- クレジットカードまたはデビットカードが利用可能なこと

## 1. Apple Developer Program への登録

### 1.1 登録ページにアクセス

1. [Apple Developer Program](https://developer.apple.com/programs/) にアクセス
2. 「Enroll」をクリック
3. Apple ID でサインイン

### 1.2 登録タイプの選択

| タイプ | 費用 | 用途 |
|-------|------|------|
| 個人（Individual） | 年額 $99 USD | 個人開発者 |
| 組織（Organization） | 年額 $99 USD | チーム開発 |

個人開発の場合は「Individual」を選択。

### 1.3 登録完了まで

- 本人確認が必要な場合がある（通常1〜2営業日）
- 登録完了後、メールで通知が届く
- 完了後に [Apple Developer](https://developer.apple.com/) でTeam IDを確認可能

### 1.4 Team ID の確認

1. [Apple Developer - Membership](https://developer.apple.com/account#MembershipDetailsCard) にアクセス
2. 「Team ID」の値をコピー
3. ローカル開発環境に設定（後述）

## 2. App Store Connect でのアプリ登録

### 2.1 Bundle ID の登録

1. [Certificates, Identifiers & Profiles](https://developer.apple.com/account/resources/identifiers/list) にアクセス
2. 「Identifiers」→「+」ボタンをクリック
3. 「App IDs」を選択 → 「Continue」
4. 「App」を選択 → 「Continue」
5. 以下の情報を入力:

| 項目 | 値 |
|------|-----|
| Description | CollabStream |
| Bundle ID | Explicit: `com.collabstream.app` |

6. 必要なCapabilitiesにチェック（後から追加可能）
7. 「Continue」→「Register」

### 2.2 App Store Connect でアプリ作成

1. [App Store Connect](https://appstoreconnect.apple.com/) にアクセス
2. 「マイApp」→「+」→「新規App」
3. 以下の情報を入力:

| 項目 | 値 |
|------|-----|
| プラットフォーム | iOS |
| 名前 | CollabStream |
| プライマリ言語 | 日本語 |
| バンドルID | com.collabstream.app |
| SKU | com-collabstream-app |

4. 「作成」をクリック

### 2.3 App Store Connect API Key の作成（CI/CD用）

1. [App Store Connect - Users and Access - Integrations - App Store Connect API](https://appstoreconnect.apple.com/access/integrations/api) にアクセス
2. 「Team Keys」→「+」ボタン
3. 以下の情報を入力:

| 項目 | 値 |
|------|-----|
| 名前 | CollabStream CI/CD |
| アクセス | App Manager |

4. 「Generate」をクリック
5. **重要**: `.p8` ファイルは1回しかダウンロードできないため、安全な場所に保存
6. 以下の情報をメモ:
   - **Key ID**: キー一覧に表示される
   - **Issuer ID**: ページ上部に表示される

## 3. ローカル開発環境の設定

### 3.1 Config.local.xcconfig の作成

```bash
cd iosApp/Configuration
cp Config.local.xcconfig.template Config.local.xcconfig
```

### 3.2 TEAM_ID の設定

`Config.local.xcconfig` を編集:

```xcconfig
TEAM_ID=XXXXXXXXXX
```

`XXXXXXXXXX` を手順1.4で取得したTeam IDに置き換える。

### 3.3 Xcode での確認

1. `iosApp.xcodeproj` をXcodeで開く
2. ターゲット「iosApp」→「Signing & Capabilities」
3. 「Team」にApple Developer Programのチーム名が表示されていることを確認
4. 「Bundle Identifier」が `com.collabstream.app` であることを確認

## トラブルシューティング

### Team IDが反映されない

- Xcodeを再起動してみる
- `Config.local.xcconfig` のファイルパスが正しいか確認
- `Config.xcconfig` の `#include?` 行が正しいか確認

### 署名エラーが出る

- Apple Developer Programの登録が完了しているか確認
- Xcode → Settings → Accounts でApple IDが追加されているか確認
- Bundle IDがApple Developer Portalに登録されているか確認
