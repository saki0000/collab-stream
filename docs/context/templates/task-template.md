# {LAYER_NAME} Layer Tasks

> **Issue**: #{ISSUE_NUMBER} - {ISSUE_TITLE}  
> **Layer**: {LAYER_NAME}  
> **Status**: {LAYER_STATUS}  
> **Responsible Agent**: {RESPONSIBLE_AGENT}

## 📊 タスク概要

### 全体進捗
- **完了タスク**: {COMPLETED_TASKS}/{TOTAL_TASKS} ({PROGRESS_PERCENTAGE}%)
- **想定作業時間**: {ESTIMATED_HOURS}時間  
- **実際作業時間**: {ACTUAL_HOURS}時間
- **開始日時**: {START_TIMESTAMP}
- **完了日時**: {COMPLETION_TIMESTAMP}

### 実装戦略
- **並行実装可能**: {PARALLEL_CAPABLE}
- **依存レイヤー**: [{DEPENDENCY_LAYERS}]
- **実装順序**: {IMPLEMENTATION_ORDER}

## ✅ 実装タスク

### コアロジック実装
- [ ] **{TASK_1_NAME}** - {TASK_1_DESCRIPTION}
  - 想定時間: {TASK_1_ESTIMATED_TIME}
  - ファイル: `{TASK_1_FILES}`
  - Status: {TASK_1_STATUS} 
  - 完了日時: {TASK_1_COMPLETED_AT}

- [ ] **{TASK_2_NAME}** - {TASK_2_DESCRIPTION}
  - 想定時間: {TASK_2_ESTIMATED_TIME}
  - ファイル: `{TASK_2_FILES}`
  - Status: {TASK_2_STATUS}
  - 完了日時: {TASK_2_COMPLETED_AT}

- [ ] **{TASK_3_NAME}** - {TASK_3_DESCRIPTION}
  - 想定時間: {TASK_3_ESTIMATED_TIME}
  - ファイル: `{TASK_3_FILES}`
  - Status: {TASK_3_STATUS}
  - 完了日時: {TASK_3_COMPLETED_AT}

### プラットフォーム固有実装
- [ ] **Android固有実装** - {ANDROID_SPECIFIC_REQUIREMENTS}
  - ファイル: `{ANDROID_FILES}`
  - Status: {ANDROID_STATUS}
  - 完了日時: {ANDROID_COMPLETED_AT}

- [ ] **iOS固有実装** - {IOS_SPECIFIC_REQUIREMENTS}
  - ファイル: `{IOS_FILES}`
  - Status: {IOS_STATUS}
  - 完了日時: {IOS_COMPLETED_AT}

- [ ] **Web固有実装** - {WEB_SPECIFIC_REQUIREMENTS}
  - ファイル: `{WEB_FILES}`
  - Status: {WEB_STATUS}
  - 完了日時: {WEB_COMPLETED_AT}

### テスト実装
- [ ] **ユニットテスト作成** - レイヤー固有ロジックのテスト
  - カバレッジ目標: {COVERAGE_TARGET}%
  - テストファイル: `{TEST_FILES}`
  - Status: {UNIT_TEST_STATUS}
  - 完了日時: {UNIT_TEST_COMPLETED_AT}

- [ ] **統合テスト作成** - 他レイヤーとの連携テスト
  - テストケース数: {INTEGRATION_TEST_CASES}
  - Status: {INTEGRATION_TEST_STATUS}
  - 完了日時: {INTEGRATION_TEST_COMPLETED_AT}

## 🏗️ 技術詳細

### 実装要件
```yaml
technical_requirements:
  frameworks: [{REQUIRED_FRAMEWORKS}]
  dependencies: [{REQUIRED_DEPENDENCIES}]
  patterns: [{IMPLEMENTATION_PATTERNS}]
  apis: [{API_INTEGRATIONS}]
```

### アーキテクチャ設計
```yaml
architecture:
  entities: [{ENTITIES}]
  repositories: [{REPOSITORIES}] 
  usecases: [{USECASES}]
  components: [{COMPONENTS}]
  services: [{SERVICES}]
```

### ファイル構造
```
{FILE_STRUCTURE}
```

## 🔄 Agent実行履歴

### 実行コマンド
```bash
{EXECUTED_COMMANDS}
```

### Agent出力ログ
```
{AGENT_OUTPUT_LOG}
```

### エラー・警告
```yaml
errors:
  - timestamp: "{ERROR_TIMESTAMP}"
    type: "{ERROR_TYPE}"
    message: "{ERROR_MESSAGE}"
    resolution: "{ERROR_RESOLUTION}"
    retry_count: {ERROR_RETRY_COUNT}
```

## 🧪 品質確認

### ビルド確認
- [ ] **コンパイル成功** - エラー・警告なし
  - Command: `{BUILD_COMMAND}`
  - Status: {BUILD_STATUS}
  - 実行時間: {BUILD_DURATION}
  - ログ: `{BUILD_LOG_PATH}`

### テスト実行
- [ ] **ユニットテスト成功** - 全テストケース通過
  - Command: `{UNIT_TEST_COMMAND}`
  - Status: {UNIT_TEST_EXECUTION_STATUS}
  - 通過率: {UNIT_TEST_PASS_RATE}%
  - カバレッジ: {UNIT_TEST_COVERAGE}%

- [ ] **統合テスト成功** - レイヤー間連携確認
  - Command: `{INTEGRATION_TEST_COMMAND}`
  - Status: {INTEGRATION_TEST_EXECUTION_STATUS}
  - 通過率: {INTEGRATION_TEST_PASS_RATE}%

### コード品質
- [ ] **Lint チェック** - コードスタイル・品質確認
  - Status: {LINT_STATUS}
  - 警告数: {LINT_WARNINGS}
  - エラー数: {LINT_ERRORS}

## 🔗 他レイヤーとの連携

### 提供するAPI/Interface
```kotlin
{PROVIDED_INTERFACES}
```

### 依存するAPI/Interface
```kotlin
{DEPENDENT_INTERFACES}
```

### データフロー
```
{DATA_FLOW_DIAGRAM}
```

## 📝 実装メモ・注意点

### 重要な設計決定
```markdown
{DESIGN_DECISIONS}
```

### 実装時の発見・学習
```markdown
{IMPLEMENTATION_LEARNINGS}
```

### TODO・将来の改善点
```markdown
{FUTURE_IMPROVEMENTS}
```

## 🚀 完了条件

### 必須条件
- [ ] 全実装タスク完了
- [ ] ユニットテスト作成・通過
- [ ] ビルド成功
- [ ] コード品質チェック通過
- [ ] 他レイヤーとの統合確認

### 品質基準
- [ ] テストカバレッジ {COVERAGE_TARGET}% 以上
- [ ] ビルド時間 {BUILD_TIME_LIMIT} 以内
- [ ] メモリ使用量 {MEMORY_LIMIT} 以内
- [ ] Lint警告 {LINT_WARNING_LIMIT} 個以下

## 📊 パフォーマンスメトリクス

### 実装効率
```yaml
metrics:
  estimated_time: {ESTIMATED_TOTAL_TIME}
  actual_time: {ACTUAL_TOTAL_TIME}
  efficiency: {EFFICIENCY_PERCENTAGE}%
  
  task_breakdown:
    implementation: {IMPLEMENTATION_TIME}
    testing: {TESTING_TIME}  
    debugging: {DEBUGGING_TIME}
    refactoring: {REFACTORING_TIME}
```

### 品質メトリクス
```yaml
quality:
  lines_of_code: {TOTAL_LOC}
  test_lines: {TEST_LOC}
  test_ratio: {TEST_TO_CODE_RATIO}
  cyclomatic_complexity: {COMPLEXITY_SCORE}
  maintainability_index: {MAINTAINABILITY_SCORE}
```

---

**最終更新**: {LAST_UPDATED}  
**次のアクション**: {NEXT_ACTION}  
**ブロッカー**: {CURRENT_BLOCKERS}