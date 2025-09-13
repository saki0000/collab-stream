---
allowed-tools: Read, Write, Edit, MultiEdit, TodoWrite, Bash(gh:*), Bash(git:*), Bash(mkdir:*), Bash(date:*), Bash(./gradlew:*), Task, mcp__serena__*, Glob, Grep
description: GitHub issue から完全自動実装まで - レイヤー別タスク分割・順次実装・PR作成
---

# Implement Issue: Full Automation Workflow

GitHub issueから実装→テスト→PR作成まで完全自動化するワークフローです。

## 🎯 実行方法

```bash
/implement-issue https://github.com/owner/repo/issues/123
# または
/implement-issue 123  # 現在のリポジトリの場合
```

## 🔄 ワークフロー概要

1. **Design Doc参照**: create-issue.mdで作成されたDesign Docを読み込み
2. **Issue分析**: GitHub CLIでissue詳細取得・要件分析
3. **Interface確認**: Design DocのInterface設計を実装指針として活用
4. **Context作成**: `docs/context/{issue-number}/` にワークスペース作成
5. **タスク分割**: Interface設計に基づいたレイヤー別タスク分解
6. **順次実装**: shared → compose → server の依存関係に従った安全な実装
7. **品質確認**: 各フェーズでユニットテスト・ビルド確認
8. **PR作成**: 既存pr.mdコマンドと連携したPR自動生成

## 📋 実行開始

### 環境確認
- GitHub認証: !`gh auth status 2>/dev/null | head -1 || echo "❌ GitHub CLI認証が必要"`
- Git状態: !`git status --porcelain || echo "Git repository not found"`
- 現在ブランチ: !`git branch --show-current`
- Gradleプロジェクト: !`ls -la gradlew build.gradle.kts 2>/dev/null | head -2 || echo "❌ Gradleプロジェクトが見つかりません"`

### Issue URL解析・取得

**入力されたissue URLまたは番号を解析し、issue詳細を取得します:**

```bash
# Issue詳細取得例
gh issue view 123 --json title,body,labels,assignees,milestone
```

## Phase 1: Design Doc参照 & Issue分析

### 1.0 Design Doc読み込み
> **必須ステップ**: create-issue.mdで作成されたDesign Docを参照

**Design Docの位置確認:**
```bash
# Design Docの存在確認
ls -la docs/design-doc/

# Issue番号からDesign Docを特定
find docs/design-doc/ -name "*.md" | grep -E "(issue[-_]?123|[^\/]*123[^\/]*\.md)"
```

**Interface設計情報の抽出:**
- ✅ Core Interfaces定義
- ✅ レイヤー別責務マトリックス
- ✅ 依存関係マッピング
- ✅ 実装指針

### 1.1 Issue情報拽出
- **Title**: Issue タイトルから機能概要把揥
- **Body**: 詳細要件・受け入れ条件抽出  
- **Labels**: feature/maintenance分類 + interface-designedラベル確認
- **Design Doc連携**: Issue本文内のDesign Doc参照情報

### 1.2 Context workspace作成
```
docs/context/{issue-number}/
├── context.md              # Issue情報 + 全体管理
├── design-doc-ref.md       # Design Doc参照情報
├── interface-impl.md       # Interface実装状態管理
├── tasks.md               # 統一タスク管理（全フェーズ）
```

### 1.3 Interface実装マッピング
**Design DocのInterface設計を実装タスクに変換:**

```markdown
# interface-impl.md 例
## Interface実装状態

### Domain Layer (shared)
- [ ] UserRepository - データ永続化・取得責務
- [ ] UserUseCase - ユーザー関連ビジネスロジック

### Presentation Layer (composeApp)
- [ ] UserScreenViewModel - UI状態管理・ユーザー操作処理
- [ ] UserNavigator - 画面遷移制御

### Infrastructure Layer (server)
- [ ] UserApiController - HTTP リクエスト/レスポンス処理
- [ ] UserValidator - 入力値検証
```

### 1.4 Interface実装プラン作成
**Design DocのInterface設計を基に実装プランを立案:**

```yaml
implementation_plan:
  phase_1_shared:
    interfaces: [UserRepository, UserUseCase]
    dependencies: []
    estimated_time: "30-60分"
    
  phase_2_compose:
    interfaces: [UserScreenViewModel, UserNavigator]
    dependencies: [phase_1_shared]
    estimated_time: "45-90分"
    
  phase_3_server:
    interfaces: [UserApiController, UserValidator]
    dependencies: [phase_1_shared]
    estimated_time: "30-75分"
```

## Phase 2: Task分割 & Agent連携

### 2.1 統一タスク管理ファイル構成

**tasks.md ファイルの構成:**
```markdown
# Tasks Management - Issue #{number}

## 📊 Overall Progress
- Phase 1 (Shared): ✅/🔄/❌
- Phase 2 (Compose): ✅/🔄/❌  
- Phase 3 (Server): ✅/🔄/❌
- Phase 4 (Integration): ✅/🔄/❌
- Total Progress: X% (completed/total tasks)

## Phase 1: Shared Layer
- [ ] Repository/Entity実装
- [ ] expect/actual Platform実装
- [ ] ユニットテスト作成
- [ ] ビルド確認

## Phase 2: Compose Layer
- [ ] Compose UI実装
- [ ] プラットフォーム固有UI
- [ ] UIテスト作成

## Phase 3: Server Layer
- [ ] APIエンドポイント実装
- [ ] サーバーロジック実装
- [ ] APIテスト作成

## Phase 4: Integration
- [ ] 統合ビルド確認
- [ ] 統合テスト実行

## 🔍 Final Checklist
- [ ] All phases completed
- [ ] All tests passing
- [ ] Ready for PR creation
```

### 2.2 実装順序の決定

**順次実装フロー:**
```
1. shared-layer:    共通ロジック・エンティティ実装
2. compose-layer:   shared層依存のUI実装
3. server-layer:    API・サーバーロジック実装
4. integration:     全レイヤー統合テスト
```

## Phase 3: Git Worktree + 実装

### 3.1 ブランチ戦略
**シンプルな単一ブランチ:**
```bash
# メインブランチから実装ブランチ作成
git checkout -b feature/issue-123-implementation

# 各レイヤーを順次実装してコミット
git commit -m "feat: implement shared layer for issue #123"
git commit -m "feat: implement compose UI for issue #123"
git commit -m "feat: implement server API for issue #123"
```

### 3.2 順次Agent実行

**Phase 1: Shared Layer (基盤実装)**
```bash
1. kotlin-backend-specialist呼び出し
2. 共通知識ベース読み込み
3. Repository/Entity/UseCase実装
4. expect/actual Platform実装
5. ユニットテスト作成
6. ビルド&テスト確認
7. コミット作成
```

**Phase 2: Compose Layer (依存UI実装)**
```bash
1. shared層完了確認
2. compose-multiplatform-specialist呼び出し
3. sharedエンティティ依存のUI実装
4. プラットフォーム固有UI実装
5. UIテスト作成
6. ビルド確認
7. コミット作成
```

**Phase 3: Server Layer (独立API実装)**
```bash
1. kotlin-backend-specialist呼び出し
2. Ktor APIエンドポイント実装
3. ビジネスロジック実装
4. APIテスト作成
5. ビルド&テスト確認
6. コミット作成
```

### 3.3 エラー処理戦略

**シンプルなエラー処理:**
```yaml
error_handling:
  phase_failure:
    action: "現在フェーズ停止 + エラーログ記録"
    recovery: "前フェーズから再開可能"
    max_retries: 2
    
  build_failure:
    action: "ビルドエラー詳細表示 + context更新"
    recovery: "エラー修正後同フェーズ再実行"
    
  test_failure:
    action: "テスト失敗詳細表示 + 修正提案"
    recovery: "テスト修正後再テスト"
```

## Phase 4: 統合・品質確認

### 4.1 統合確認
```bash
# 全レイヤー完了確認
├─ shared/compose/server実装完了
├─ 全レイヤーコミット確認
├─ 統合ビルド確認
└─ 統合テスト実行
```

### 4.2 全プラットフォームビルド・テスト
```bash
# プラットフォーム別ビルド確認
./gradlew :shared:build                    # 共通ロジック
./gradlew :composeApp:assembleDebug       # Android
./gradlew :composeApp:wasmJsBrowserDevelopmentRun  # Web確認
./gradlew :server:build                   # Server

# テスト実行
./gradlew test                            # 全テスト実行
./gradlew :shared:test                    # 共通テスト
./gradlew :server:test                    # サーバーテスト
```

### 4.3 品質チェック
- **コミットフック**: ktlint自動実行（既存設定活用）
- **テストカバレッジ**: 各レイヤーのユニットテスト網羅性確認
- **ビルド成功**: 全プラットフォームでのビルド成功確認

## Phase 5: PR作成

### 5.1 実装完了確認
```yaml
completion_check:
  - all_tasks_completed: ✅
  - all_tests_passing: ✅  
  - all_builds_successful: ✅
  - no_compilation_errors: ✅
  - context_updated: ✅
```

### 5.2 PR自動作成
**既存pr.mdコマンドとの連携:**
```bash
# PR作成
├─ ブランチをリモートにpush
├─ Issue内容からPRタイトル・説明生成
├─ 実装詳細をcontext情報から抽出
├─ PR作成 (gh pr create)
└─ Issue自動クローズ設定
```

## 📝 Context管理詳細

### 🎯 統一タスク管理の利点

**今までの問題:**
- 4つのタスクファイルを個別に確認が必要
- 全体進捗を把握しづらい
- 最終確認時の確認漯れリスク

**改善後のメリット:**
- ✅ **一元管理**: 全タスクを1ファイルで管理
- ✅ **進捗可視化**: リアルタイムで全体進捗を把握
- ✅ **簡単な最終確認**: Final Checklistで一括確認
- ✅ **依存関係明確**: 各フェーズの前提条件を表示

### workflow-state.json更新例
```json
{
  "issue_number": 123,
  "current_phase": "compose-layer",
  "completed_phases": ["shared-layer"],
  "next_phase": "server-layer",
  "context_data": {
    "dependencies": ["Room", "Ktor", "Compose Navigation"],
    "platforms": ["android", "ios", "wasmJs", "jvm"],
    "test_strategy": "unit_per_layer"
  },
  "implementation_artifacts": {
    "shared_entities": ["User", "Session"],
    "api_contracts": ["UserRepository", "SessionService"],
    "ui_components": ["LoginScreen", "DashboardScreen"]
  },
  "commits": [
    "feat: implement shared layer for issue #123",
    "feat: implement compose UI for issue #123"
  ],
  "error_log": []
}
```

## 🚀 使用例

```bash
# 基本使用
/implement-issue https://github.com/user/CollabStream/issues/123

# 実行ログ例:
✅ Issue #123: "Add user authentication feature" 取得完了
✅ Context workspace作成: docs/context/123/
🔄 task-breakdown-specialist 実行中...
✅ レイヤー分割完了: [shared → compose → server]
✅ 順次実装フロー決定: 依存関係に従って安全に実行
🔄 Phase 1: kotlin-backend-specialist (shared) 実行中...
✅ shared層実装完了 + ユニットテスト + コミット
🔄 Phase 2: compose-multiplatform-specialist 実行中...
✅ compose層実装完了 + UIテスト + コミット
🔄 Phase 3: kotlin-backend-specialist (server) 実行中...
✅ server層実装完了 + APIテスト + コミット
✅ 全プラットフォーム統合ビルド成功
✅ 統合テスト成功
🔄 PR作成中...
✅ PR作成完了: https://github.com/user/CollabStream/pull/456
```

## ⚠️ 注意事項

- **大規模Issue**: 複雑すぎる場合は手動介入を推奨
- **依存関係**: 外部ライブラリが必要な場合は事前確認
- **Platform固有**: iOS・Web固有要件は詳細指定が必要
- **順次実行**: 各フェーズの完了を確認してから次へ進むため安全

---

**開発者向け**: このワークフローはKotlin Multiplatformプロジェクトに最適化された順次実行アプローチで、shared/compose/serverの3層アーキテクチャを前提とした安全で信頼性の高い実装です。

## ✨ 新機能: 統一タスク管理

このアップデートでは、従来の分散したタスク管理から、**一元化されたタスク管理システム**に移行しました。

- 🔄 **Before**: 4つのタスクファイルを個別管理
- ✅ **After**: 1つの`tasks.md`で全フェーズを統一管理

こにより、最終確認が大幅に簡略化され、実装品質と信頼性が向上しました。