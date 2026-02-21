# CLAUDE.md

## 重要: アウトプット言語

**すべての応答、説明、コメント、ドキュメントは日本語で出力してください。**

---

## プロジェクト概要

Android、iOS、Server をターゲットとする Kotlin Multiplatform プロジェクト。
UI は Compose Multiplatform、サーバーは Ktor を使用。

## コマンド

| コマンド | 説明 |
|---------|------|
| `./gradlew :server:run` | Ktor サーバー起動（ポート 8080） |
| `./gradlew :composeApp:assembleDebug` | Android APK ビルド |
| `./gradlew test` | 全テスト実行 |
| `./gradlew :composeApp:recordRoborazziDebug` | スクリーンショット記録 |
| `/capture-screenshots` | UI スクリーンショット取得 |
| `/phase0` | Epic定義 & 共通基盤の切り出し（大規模機能） |
| `/develop` | 仕様定義 → 実装 → PR作成（統合開発コマンド） |

## モジュール構造

| モジュール | パス | 内容 |
|-----------|------|------|
| shared | `/shared/src/commonMain/kotlin` | ビジネスロジック（Domain/Data） |
| composeApp | `/composeApp/src/commonMain/kotlin` | Compose UI |
| server | `/server/src/main/kotlin` | Ktor サーバー |
| iosApp | `/iosApp/iosApp` | iOS エントリーポイント |

## 詳細ルール

詳細なルールは `.claude/rules/` を参照:

| カテゴリ | ファイル |
|---------|----------|
| 言語 | `.claude/rules/language.md` |
| テスト | `.claude/rules/testing.md` |
| ドキュメント | `.claude/rules/documentation.md` |
| ワークフロー | `.claude/rules/workflow.md` |
| ADR | `.claude/rules/architecture/*.md` |
| Compose | `.claude/rules/compose/*.md` |
| Gradle | `.claude/rules/gradle.md` |
| Domain | `.claude/rules/shared/domain-rules.md` |
| Server | `.claude/rules/server/ktor-rules.md` |

## テンプレート

| 用途 | ファイル |
|------|----------|
| コミット | `.claude/templates/commit-message.md` |
| PR | `.claude/templates/pr-description.md` |
| レビュー | `.claude/templates/code-review-checklist.md` |

## 開発ワークフロー

`/phase0` + `/develop` の2コマンド体制による仕様駆動開発（SDD）を採用。
タスク管理は `implement-context/` 内のmarkdownファイルで行う。
詳細: `.claude/rules/workflow.md` / `docs/guides/development-workflow.md`
