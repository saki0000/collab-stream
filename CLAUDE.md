# CLAUDE.md

このファイルは、このリポジトリでコードを扱う際にClaude Code (claude.ai/code) に対するガイダンスを提供します。

## 重要: アウトプット言語

**すべての応答、説明、コメント、ドキュメントは日本語で出力してください。**

コードやコマンド以外のすべてのテキスト出力は日本語で行ってください。

---

## プロジェクト概要

これはAndroid、iOS、ServerプラットフォームをターゲットとするKotlin Multiplatformプロジェクトです。UIにはCompose Multiplatformを、サーバーサイド開発にはKtorを使用しています。

## コマンド

### ビルドと実行
- **Server**: `./gradlew :server:run` - ポート8080でKtorサーバーを実行
- **Android**: `./gradlew :composeApp:assembleDebug` - Android APKをビルド
- **iOS**: Xcodeで`iosApp/iosApp.xcodeproj`を開いてビルド・実行

### テスト
- **すべてのテスト**: `./gradlew test`
- **共通テスト**: `./gradlew :shared:test`
- **サーバーテスト**: `./gradlew :server:test`

### スクリーンショットテスト
- **スクリーンショット記録**: `./gradlew :composeApp:recordRoborazziDebug`
- **スクリーンショット検証**: `./gradlew :composeApp:verifyRoborazziDebug`
- **差分比較**: `./gradlew :composeApp:compareRoborazziDebug`

### 成果物の確認

UI実装の成果物を確認するには、`/capture-screenshots` skillを使用してください。

```
/capture-screenshots
```

このコマンドは以下を実行します：
1. Roborazziで@Preview付きComposableのスクリーンショットを取得
2. ScreenとComponentを自動分類
3. 結果を`screenshots/`ディレクトリに保存

**保存先:**
- `screenshots/screens/` - Screen（フルスクリーンUI）のスクリーンショット（git管理対象）
- `screenshots/components/` - Component/Content等のスクリーンショット（git管理対象外）

### ビルドタスク
- **クリーン**: `./gradlew clean`
- **すべてビルド**: `./gradlew build`

## 開発ワークフロー

CollabStreamプロジェクトでは、AI（Claude Code）を活用した仕様駆動開発（SDD）ワークフローを採用しています。

### ワークフロー概要

開発は以下の4つのPhaseで構成されます：

- **Phase 0**: Epic定義 & 共通基盤の切り出し（大規模機能のみ）
- **Phase 1**: 仕様・インターフェース定義（合意レビュー）
- **Phase 2**: AIによる実装
- **Phase 3**: 実装レビュー

### 主要ドキュメント

詳細なワークフローガイドラインは以下を参照してください：

- **メインガイド**: `docs/guides/development-workflow.md` - 全体フローの詳細
- **テンプレート**: `docs/design-doc/template/` - Epic/REQUIREMENTS/Design Docテンプレート
- **ナビゲーション**: `docs/screen-navigation.md` - アプリ全体の画面ナビゲーション

### クイックスタート

1. **新機能開発開始時**:
   - 3 Story以上の大規模機能 → Phase 0から開始（Epic作成）
   - 小規模機能 → Phase 1から開始（REQUIREMENTS.md作成）

2. **Phase 1（仕様定義）**:
   - `composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/REQUIREMENTS.md`を作成
   - `composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/screen-transition.md`を作成
   - 必要に応じて`docs/navigation/{module_name}-module.md`を作成
   - インターフェースファースト設計
   - レビュー合意後にGitHub Issue作成

3. **Phase 2（AI実装）**:
   - REQUIREMENTSに基づきAIが実装
   - Serena Skillの活用推奨

   **実装開始前の確認事項**

   **重要**: 実装を開始する前に、以下のスキルが自動実行されることを確認してください：

   - **`/create-worktree`**: Git worktreeを作成して独立した開発環境を準備
   - **`/implement-plan`**: planファイルを解析し適切なエージェントを起動

   これらのスキルは計画承認後（ExitPlanMode後）に自動的に呼び出されます。

4. **Phase 3（レビュー）**:
   - 仕様適合性とADR準拠を確認
   - PR作成と実装記録（`docs/context/{issue}/`）

### テスト規約

- **フレームワーク**: kotlin.test（JUnit 5を直接使用しない）
- **スタイル**: ネストクラス + `@DisplayName`
- **配置場所**:
  - `shared/src/commonTest/`: 共通テスト
  - `shared/src/jvmTest/`: JVM固有テスト
  - `composeApp/src/commonTest/`: UI層テスト

```kotlin
@DisplayName("VideoSync機能のテスト")
class VideoSyncUseCaseTest {

    @Nested
    @DisplayName("同期時刻計算")
    inner class CalculateAbsoluteTime {

        @Test
        @DisplayName("正の時刻オフセットを正しく計算すること")
        fun `should calculate correct absolute time with positive offset`() {
            // Arrange, Act, Assert
        }
    }
}
```

完全なワークフロー詳細は`docs/guides/development-workflow.md`を参照してください。

## アーキテクチャ

### モジュール構造
- **`/shared`** (`/shared/src/commonMain/kotlin`): すべてのプラットフォームで共有されるコアビジネスロジック
- **`/composeApp`** (`/composeApp/src/commonMain/kotlin`): プラットフォーム間で共有されるCompose Multiplatform UIコード
  - `androidMain/`、`iosMain/`等にプラットフォーム固有コード
- **`/server`** (`/server/src/main/kotlin`): Ktorサーバーアプリケーション
- **`/iosApp`** (`/iosApp/iosApp`): iOSアプリケーションエントリーポイントとSwiftUIコード

### ディレクトリ構造

#### composeApp（機能ベースアーキテクチャ）
```
composeApp/src/commonMain/kotlin/org/example/project/
├── core/                         # 共有コンポーネント
│   └── di/                       # 依存性注入設定
├── feature/                      # 機能モジュール（機能ごとに1ディレクトリ）
│   ├── video_playback/           # 動画再生機能
│   │   ├── ui/                   # UIコンポーネント（Container、Screen、Content等）
│   │   └── player/               # プレイヤー固有ロジック（State、Controller、Templates）
│   ├── video_search/             # 動画検索機能
│   │   └── ui/                   # 検索UIコンポーネント
│   └── video_sync/               # 動画同期機能
│       └── ui/                   # 同期UIコンポーネント
└── (ルートレベル機能ファイル)      # App.kt、プラットフォームビュー
```

**アーキテクチャパターン**: 各機能は以下を含む：
- `ui/`: Composable UIコンポーネント
- `player/`: プレイヤー固有ロジック（該当する場合）
- ViewModel、Intent、UiStateは機能ルートに配置（MVIパターン）

#### shared（Clean Architecture - DomainとDataレイヤー）
```
shared/src/commonMain/kotlin/org/example/project/
├── core/                         # コアユーティリティ
│   ├── di/                       # 共有DI設定
│   └── util/                     # ユーティリティクラス
├── domain/                       # Domainレイヤー（ビジネスロジック）
│   ├── model/                    # ドメインモデルとエンティティ
│   ├── repository/               # Repositoryインターフェース
│   └── usecase/                  # ユースケース（ビジネスロジック）
├── data/                         # Dataレイヤー（実装）
│   ├── datasource/               # データソース（APIクライアント、ローカルストレージ）
│   ├── repository/               # Repository実装
│   ├── mapper/                   # DataからDomainへのマッパー
│   └── model/                    # APIレスポンスモデル
└── api/                          # 外部APIクライアント
    └── (サービス固有ディレクトリ)  # 例: twitch/、youtube/
```

**アーキテクチャパターン**: レイヤー分離が明確なClean Architecture
- Domainレイヤーが契約を定義（インターフェース、モデル、ユースケース）
- Dataレイヤーが契約を実装（リポジトリ、データソース）
- 依存関係ルール: DataはDomainに依存、逆は決してない

### 主要ファイル
- **サーバー設定**: `shared/src/commonMain/kotlin/org/example/project/Constants.kt` - `SERVER_PORT = 8080`を含む
- **メインCompose UI**: `composeApp/src/commonMain/kotlin/org/example/project/App.kt`
- **動画再生**: `composeApp/src/commonMain/kotlin/org/example/project/feature/video_playback/`
- **サーバールート**: `server/src/main/kotlin/org/example/project/Application.kt`

### プラットフォームターゲット
- **Android**: gradle設定ごとのmin SDKでJetpack Composeを使用
- **iOS**: 静的ライブラリとしてビルドされ、SwiftUIアプリで利用されるフレームワーク
- **Server**: KtorとNettyを使用するJVMターゲット

プロジェクトは、共有コード用の`commonMain`とプラットフォーム固有実装用のプラットフォーム固有ソースセットを持つ標準Kotlin Multiplatform規約に従っています。

## ドキュメント構造

### `/docs` ディレクトリ構成
```
docs/
├── architecture/        # システムアーキテクチャとデザインパターンのドキュメント
├── adr/                 # Architecture Decision Records（ADR） - 番号付き決定ログ
├── design-doc/          # 機能設計ドキュメントとテンプレート
├── navigation/          # モジュールレベルナビゲーション図（Level 2）
└── context/             # GitHub Issue固有の実装コンテキストとタスク分割（issueごとに1ディレクトリ）
```

### ドキュメントガイドライン

- **`architecture/`**: 高レベルシステムアーキテクチャ、プレゼンテーションパターン、コンポーネント設計ガイドライン
- **`adr/`**: ADRフォーマットに従うArchitecture Decision Records（番号付き: `NNN-kebab-case-title.md`）
- **`design-doc/`**: 提供されたテンプレートを使用した機能の詳細設計仕様
- **`navigation/`**: モジュールレベルの画面遷移図（Level 2）
- **`context/`**: Issue固有の実装コンテキスト、設計参照、インターフェース定義、タスク分割を含む

### ナビゲーションドキュメントの3レベル階層

1. **Level 1 - アプリ全体**: `docs/screen-navigation.md` - すべての機能とモジュールの概要
2. **Level 2 - モジュールレベル**: `docs/navigation/{module_name}-module.md` - モジュール内の画面遷移
3. **Level 3 - 画面内部**: `feature/{feature_name}/screen-transition.md` - 画面の詳細な振る舞い

### ドキュメント作成ルール

新機能を追加する際は、以下のドキュメントを作成してください：

1. **Phase 1（仕様定義時）**:
   - `feature/{feature_name}/REQUIREMENTS.md` - 機能仕様
   - `feature/{feature_name}/screen-transition.md` - 画面内部の振る舞い（Level 3）
   - 新しいモジュールの場合: `docs/navigation/{module_name}-module.md` - モジュールレベルナビゲーション（Level 2）
   - `docs/screen-navigation.md`を更新 - アプリ全体の概要（Level 1）

2. **テンプレート使用**:
   - モジュールナビゲーション: `docs/design-doc/template/module-navigation-template.md`
   - 画面遷移: `docs/design-doc/template/screen-transition-template.md`
   - 機能仕様: `docs/design-doc/template/requirements-template.md`

---

**重要**: すべてのドキュメントは日本語で作成してください。Mermaid図のラベルも日本語で記述してください。
