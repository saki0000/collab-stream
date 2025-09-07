---
allowed-tools: Read, Write, Edit, MultiEdit, TodoWrite, Bash(gh:*), Bash(git:*), Bash(mkdir:*), Bash(date:*), Bash(./gradlew:*), Task, mcp__serena__*, Glob, Grep
description: GitHub issue から完全自動実装まで - レイヤー別タスク分割・並行実装・PR作成
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

1. **Issue分析**: GitHub CLIでissue詳細取得・要件分析
2. **Context作成**: `docs/context/{issue-number}/` にワークスペース作成
3. **タスク分割**: レイヤー別（shared/compose/server）にタスク分解
4. **並行実装**: Git worktreeを活用した効率的開発
5. **品質確認**: ユニットテスト作成・ビルド確認・テスト実行
6. **PR作成**: 既存pr.mdコマンドと連携したPR自動生成

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

## Phase 1: Issue分析 & Context作成

### 1.1 Issue情報抽出
- **Title**: Issue タイトルから機能概要把握
- **Body**: 詳細要件・受け入れ条件抽出  
- **Labels**: feature/maintenance分類
- **Technical requirements**: Kotlin Multiplatform要件分析

### 1.2 Context workspace作成
```
docs/context/{issue-number}/
├── context.md              # Issue情報 + 全体管理
├── analysis.md             # 要件分析結果
├── workflow-state.json     # Agent連携状態管理
├── tasks/
│   ├── shared-layer.md     # 共通ロジック層タスク
│   ├── compose-layer.md    # UI層タスク
│   ├── server-layer.md     # サーバー層タスク
│   └── integration.md      # 統合・テスト
└── implementation/
    ├── commits.md          # コミット履歴
    ├── errors.md           # エラーログ
    └── verification.md     # 検証結果
```

### 1.3 Requirements analysis
**task-breakdown-specialist agent呼び出し:**
- Issue内容からKotlin Multiplatform要件抽出
- レイヤー分割判定（shared/compose/server）
- プラットフォーム固有要件分析（Android/iOS/Web/Server）
- 依存関係マッピング・並行実装可能性判定

## Phase 2: Task分割 & Agent連携

### 2.1 レイヤー別タスク分割

```yaml
# shared-layer (共通ロジック)
tasks:
  - commonMain: ビジネスロジック実装
  - data-layer: Repository・Entity定義
  - platform-specific: expect/actual実装
  - unit-tests: レイヤーテスト作成

# compose-layer (UI)  
tasks:
  - commonMain: 共通UI実装
  - platform-ui: プラットフォーム固有UI
  - navigation: 画面遷移実装
  - ui-tests: UIテスト作成

# server-layer (API)
tasks:
  - routing: API エンドポイント定義
  - business-logic: サーバーロジック
  - data-access: DB・外部API連携
  - api-tests: APIテスト作成
```

### 2.2 依存関係 & 並行実装判定

**自動判定ロジック:**
```
if (shared独立 && server独立):
    create_parallel_worktrees(["shared", "server"])
    sequential_dependency(["shared", "compose"])
    
elif (全レイヤー独立):
    create_parallel_worktrees(["shared", "compose", "server"])
    
else:
    sequential_implementation(["shared", "compose", "server"])
```

## Phase 3: Git Worktree + 実装

### 3.1 ブランチ戦略
**Trunk-based development:**
```bash
# メインブランチから実装ブランチ作成
git checkout -b feature/issue-123-implementation

# 並行実装の場合はworktree活用
git worktree add ../CollabStream-shared feature/issue-123-implementation
git worktree add ../CollabStream-server feature/issue-123-implementation
```

### 3.2 Agent連携実装

**各レイヤーでのAgent実行:**

```bash
# Shared Layer Implementation
├─ kotlin-backend-specialist呼び出し
├─ 共通知識ベース (kotlin-multiplatform-patterns.md) 読み込み
├─ Repository/Entity実装
├─ expect/actual Platform実装
├─ ユニットテスト作成
├─ ビルド確認 (./gradlew :shared:build)
├─ テスト実行 (./gradlew :shared:test)
└─ タスク完了 → context更新

# Compose Layer Implementation  
├─ compose-multiplatform-specialist呼び出し
├─ UI知識ベース読み込み
├─ shared層依存関係確認
├─ Compose UI実装
├─ プラットフォーム固有UI実装
├─ UIテスト作成
├─ ビルド確認 (./gradlew :composeApp:build)
└─ タスク完了 → context更新

# Server Layer Implementation
├─ kotlin-backend-specialist呼び出し  
├─ Ktor API実装
├─ ルーティング定義
├─ サーバーロジック実装
├─ APIテスト作成
├─ ビルド確認 (./gradlew :server:build)
├─ テスト実行 (./gradlew :server:test)
└─ タスク完了 → context更新
```

### 3.3 エラー処理・リトライ戦略

**自動エラー処理:**
```yaml
error_handling:
  compilation_error:
    max_retries: 3
    action: "詳細ログ出力 + context記録"
    fallback: "ユーザー確認待機"
  
  test_failure:
    max_retries: 2  
    action: "テスト修正試行"
    fallback: "手動介入要請"
    
  worktree_conflict:
    action: "自動マージ試行"
    fallback: "競合解決ガイド提示"
```

## Phase 4: 統合・品質確認

### 4.1 Worktreeマージ・統合
```bash
# 並行worktreeの場合
├─ 各worktreeの完了確認
├─ メインworktreeにマージ  
├─ 競合解決（必要に応じて）
├─ 統合ビルド確認
└─ worktree cleanup
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

### workflow-state.json更新例
```json
{
  "issue_number": 123,
  "current_phase": "compose-layer",
  "completed_agents": ["task-breakdown-specialist", "kotlin-backend-specialist"],
  "next_agent": "compose-multiplatform-specialist",
  "worktrees": {
    "shared": {"status": "completed", "path": "../CollabStream-shared"},
    "server": {"status": "in-progress", "path": "../CollabStream-server"}
  },
  "context_data": {
    "dependencies": ["Room", "Ktor", "Compose Navigation"],
    "platforms": ["android", "ios", "wasmJs", "jvm"],
    "test_strategy": "unit_per_layer",
    "api_endpoints": ["/api/users", "/api/sessions"]
  },
  "handoff_data": {
    "shared_entities": ["User", "Session"],
    "api_contracts": [...],
    "ui_requirements": [...]
  },
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
✅ レイヤー分割完了: [shared, compose, server]
🔄 並行実装判定: shared・server並行可能
✅ Worktree作成: shared・server並行実装開始
🔄 kotlin-backend-specialist (shared) 実行中...
🔄 kotlin-backend-specialist (server) 実行中...  
✅ shared層実装完了 + ユニットテスト
✅ server層実装完了 + APIテスト
🔄 compose-multiplatform-specialist 実行中...
✅ UI層実装完了 + UIテスト
✅ 全プラットフォームビルド成功
✅ 全テスト成功
🔄 PR作成中...
✅ PR作成完了: https://github.com/user/CollabStream/pull/456
```

## ⚠️ 注意事項

- **大規模Issue**: 複雑すぎる場合は手動介入を推奨
- **依存関係**: 外部ライブラリが必要な場合は事前確認
- **Platform固有**: iOS・Web固有要件は詳細指定が必要
- **テスト戦略**: 各レイヤーでユニットテスト必須作成

---

**開発者向け**: このワークフローはKotlin Multiplatformプロジェクトに最適化されており、shared/compose/serverの3層アーキテクチャを前提としています。