# Issue Context: #{ISSUE_NUMBER} - {ISSUE_TITLE}

> **ワークフロー**: GitHub Issue → 実装 → PR作成の管理ファイル  
> **作成日**: {CREATED_DATE}  
> **実装者**: {IMPLEMENTER}

## 🎯 Issue情報

### 基本情報
- **Issue URL**: {ISSUE_URL}
- **Issue番号**: #{ISSUE_NUMBER}
- **タイトル**: {ISSUE_TITLE}
- **ラベル**: {ISSUE_LABELS}
- **担当者**: {ISSUE_ASSIGNEES}
- **マイルストーン**: {ISSUE_MILESTONE}

### Issue内容
```markdown
{ISSUE_BODY}
```

### 受け入れ条件
{ACCEPTANCE_CRITERIA}

## 📊 実装進捗

### 全体ステータス
- **進捗**: {PROGRESS_PERCENTAGE}% ({COMPLETED_TASKS}/{TOTAL_TASKS} tasks completed)
- **現在のPhase**: {CURRENT_PHASE}
- **実装戦略**: {IMPLEMENTATION_STRATEGY}
- **想定作業時間**: {ESTIMATED_HOURS}時間

### フェーズ管理
```yaml
phases:
  analysis:
    status: {ANALYSIS_STATUS}  # ✅ completed / 🔄 in-progress / ❌ pending
    completed_at: {ANALYSIS_COMPLETED_AT}
    
  shared_layer:
    status: {SHARED_STATUS}
    parallel_with: []
    estimated_time: "{SHARED_ESTIMATED_TIME}"
    completed_at: {SHARED_COMPLETED_AT}
    
  compose_layer: 
    status: {COMPOSE_STATUS}
    parallel_with: ["server_layer"]
    estimated_time: "{COMPOSE_ESTIMATED_TIME}"
    completed_at: {COMPOSE_COMPLETED_AT}
    
  server_layer:
    status: {SERVER_STATUS} 
    parallel_with: ["compose_layer"]
    estimated_time: "{SERVER_ESTIMATED_TIME}"
    completed_at: {SERVER_COMPLETED_AT}
    
  integration:
    status: {INTEGRATION_STATUS}
    dependencies: ["shared_layer", "compose_layer", "server_layer"]
    estimated_time: "{INTEGRATION_ESTIMATED_TIME}"
    completed_at: {INTEGRATION_COMPLETED_AT}
```

## 🏗️ 技術分析

### 影響レイヤー
- **Shared Layer**: {SHARED_REQUIRED} - {SHARED_REQUIREMENTS}
- **Compose Layer**: {COMPOSE_REQUIRED} - {COMPOSE_REQUIREMENTS}  
- **Server Layer**: {SERVER_REQUIRED} - {SERVER_REQUIREMENTS}

### プラットフォーム要件
```yaml
platforms:
  android: {ANDROID_REQUIREMENTS}
  ios: {IOS_REQUIREMENTS}
  web: {WEB_REQUIREMENTS}
  server: {SERVER_PLATFORM_REQUIREMENTS}
```

### 技術詳細
- **新規Dependencies**: {NEW_DEPENDENCIES}
- **API Changes**: {API_CHANGES}
- **Database Changes**: {DATABASE_CHANGES}
- **UI Components**: {UI_COMPONENTS}

## 🌳 Git戦略

### ブランチ情報
- **実装ブランチ**: `feature/issue-{ISSUE_NUMBER}-{SANITIZED_TITLE}`
- **ベースブランチ**: `{BASE_BRANCH}`
- **Worktree戦略**: {WORKTREE_STRATEGY}

### Worktree管理
```yaml
worktrees:
  main:
    path: "{MAIN_WORKTREE_PATH}"
    focus: "{MAIN_FOCUS}"
    status: {MAIN_WORKTREE_STATUS}
    
  shared:
    path: "{SHARED_WORKTREE_PATH}" 
    focus: "shared layer implementation"
    status: {SHARED_WORKTREE_STATUS}
    
  compose:
    path: "{COMPOSE_WORKTREE_PATH}"
    focus: "UI layer implementation" 
    status: {COMPOSE_WORKTREE_STATUS}
    
  server:
    path: "{SERVER_WORKTREE_PATH}"
    focus: "API layer implementation"
    status: {SERVER_WORKTREE_STATUS}
```

## 🤖 Agent実行履歴

### 実行済みAgent
```yaml
completed_agents:
  - agent: "task-breakdown-specialist"
    executed_at: "{TASK_BREAKDOWN_EXECUTED_AT}"
    duration: "{TASK_BREAKDOWN_DURATION}"
    output: "tasks/{ISSUE_NUMBER}/ に各レイヤータスク作成"
    
  - agent: "{NEXT_COMPLETED_AGENT}"
    executed_at: "{AGENT_EXECUTED_AT}"
    duration: "{AGENT_DURATION}"
    output: "{AGENT_OUTPUT}"
```

### 次の実行予定Agent
```yaml
next_agents:
  immediate: "{IMMEDIATE_NEXT_AGENT}"
  parallel_ready: ["{PARALLEL_AGENT_1}", "{PARALLEL_AGENT_2}"]
  waiting: ["{WAITING_AGENT}"]
```

### Agent実行コマンド履歴
```bash
# 実行されたコマンド
{EXECUTED_COMMANDS}
```

## 📝 実装詳細

### 作成されたファイル
```yaml
shared_layer:
  entities: [{SHARED_ENTITIES}]
  repositories: [{SHARED_REPOSITORIES}]
  usecases: [{SHARED_USECASES}]
  tests: [{SHARED_TESTS}]
  
compose_layer:
  screens: [{COMPOSE_SCREENS}]
  components: [{COMPOSE_COMPONENTS}]
  viewmodels: [{COMPOSE_VIEWMODELS}]
  tests: [{COMPOSE_TESTS}]
  
server_layer:
  routes: [{SERVER_ROUTES}]
  plugins: [{SERVER_PLUGINS}]
  handlers: [{SERVER_HANDLERS}]
  tests: [{SERVER_TESTS}]
```

### コミット履歴
```
{COMMIT_HISTORY}
```

## 🧪 テスト状況

### テスト実行結果
```yaml
test_results:
  shared:
    total: {SHARED_TOTAL_TESTS}
    passed: {SHARED_PASSED_TESTS}
    failed: {SHARED_FAILED_TESTS}
    coverage: "{SHARED_COVERAGE}%"
    
  compose:
    total: {COMPOSE_TOTAL_TESTS}
    passed: {COMPOSE_PASSED_TESTS} 
    failed: {COMPOSE_FAILED_TESTS}
    coverage: "{COMPOSE_COVERAGE}%"
    
  server:
    total: {SERVER_TOTAL_TESTS}
    passed: {SERVER_PASSED_TESTS}
    failed: {SERVER_FAILED_TESTS}
    coverage: "{SERVER_COVERAGE}%"
    
  integration:
    total: {INTEGRATION_TOTAL_TESTS}
    passed: {INTEGRATION_PASSED_TESTS}
    failed: {INTEGRATION_FAILED_TESTS}
```

### ビルド状況
```yaml
build_status:
  shared: {SHARED_BUILD_STATUS}        # ✅ success / ❌ failed / 🔄 building
  compose: {COMPOSE_BUILD_STATUS}
  server: {SERVER_BUILD_STATUS}
  integration: {INTEGRATION_BUILD_STATUS}
  
last_build_command: "{LAST_BUILD_COMMAND}"
last_build_time: "{LAST_BUILD_TIME}"
```

## ⚠️ 問題・エラーログ

### 実装時エラー
```yaml
errors:
  - timestamp: "{ERROR_TIMESTAMP}"
    phase: "{ERROR_PHASE}"
    agent: "{ERROR_AGENT}"
    error: "{ERROR_MESSAGE}"
    resolution: "{ERROR_RESOLUTION}"
    retry_count: {ERROR_RETRY_COUNT}
```

### 既知の制約・注意点
```markdown
{KNOWN_CONSTRAINTS}
```

## 🚀 PR作成準備

### PR作成条件チェック
```yaml
pr_readiness:
  all_tasks_completed: {ALL_TASKS_COMPLETED}
  all_tests_passing: {ALL_TESTS_PASSING}
  all_builds_successful: {ALL_BUILDS_SUCCESSFUL}
  no_compilation_errors: {NO_COMPILATION_ERRORS}
  worktrees_merged: {WORKTREES_MERGED}
  
ready_for_pr: {READY_FOR_PR}  # true/false
```

### PR情報
- **予定PR番号**: {EXPECTED_PR_NUMBER}
- **PR Title**: `{PR_TITLE}`
- **PR Body**: 
```markdown
{PR_BODY}
```

## 📈 メトリクス

### 実装効率
- **総作業時間**: {TOTAL_WORK_TIME}
- **Agent実行時間**: {AGENT_EXECUTION_TIME} 
- **手動介入時間**: {MANUAL_INTERVENTION_TIME}
- **並行度**: {PARALLELISM_ACHIEVED}

### 品質メトリクス
- **全体テストカバレッジ**: {OVERALL_COVERAGE}%
- **コミット数**: {COMMIT_COUNT}
- **ファイル変更数**: {FILES_CHANGED}
- **追加行数**: {LINES_ADDED}
- **削除行数**: {LINES_DELETED}

---

**最終更新**: {LAST_UPDATED}  
**次のアクション**: {NEXT_ACTION}  
**担当Agent**: {RESPONSIBLE_AGENT}