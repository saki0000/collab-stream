# Epic: {Epic Name}

> **Phase 0**: 大規模機能の分割と共通ドメイン定義
> **目的**: 並行開発時の設計不整合を防ぐ

## メタデータ
- **Epic ID**: {EPIC-XXX}
- **作成日**: {YYYY-MM-DD}
- **担当**: {Name}

---

## 1. Epic概要

### ビジョン
{このEpicで実現したい大きなゴール（1-2段落）}

### 背景・課題
{なぜこのEpicが必要か、解決したい課題は何か}

### ユーザー価値
- {価値1}
- {価値2}

---

## 2. 共通ドメイン定義

複数のStoryで共有する`Entity`（データ構造）と`Repository Interface`だけを定義します。

### Entity（データ構造）

#### {DomainModel1}
```kotlin
// shared/src/commonMain/kotlin/org/example/project/domain/model/

/**
 * {説明}
 * Epic内の複数Storyで共通利用
 */
data class {DomainModel1}(
    val id: String,
    // Shared fields across stories
)
```

#### {DomainModel2}（必要に応じて追加）
```kotlin
data class {DomainModel2}(
    // fields
)
```

### Repository Interface
```kotlin
// shared/src/commonMain/kotlin/org/example/project/domain/repository/

/**
 * {Epic名}の共通Repository
 */
interface {Epic}Repository {
    // Shared methods
    suspend fun {commonMethod}({params}): Result<{Type}>
}
```

**注意**:
- **UseCaseはPhase 1で定義**します（各Storyの`REQUIREMENTS.md`内で定義）
- ここでは、データ構造とRepository Interfaceのみを定義してマージします

---

## 3. Story分割

ユーザー価値単位でStoryを切ります。
例：「動画一覧が見れる」「再生ができる」

### Story 1: {Story Name}
- **Goal**: {1文でのゴール}
- **User Story**: As a {user}, I want to {action}, so that {benefit}
- **依存**: {依存するStory、または "None"}
- **成果物**: `feature/{feature_name}/REQUIREMENTS.md`

### Story 2: {Story Name}
- **Goal**: {1文でのゴール}
- **User Story**: As a {user}, I want to {action}, so that {benefit}
- **依存**: Story 1
- **成果物**: `feature/{feature_name}/REQUIREMENTS.md`

### Story 3: {Story Name}（必要に応じて追加）
- **Goal**: {1文でのゴール}
- **User Story**: As a {user}, I want to {action}, so that {benefit}
- **依存**: Story 1, Story 2
- **成果物**: `feature/{feature_name}/REQUIREMENTS.md`

---

## 4. Story依存関係

```mermaid
graph TD
    A[Story 1: {Name}] --> B[Story 2: {Name}]
    A --> C[Story 3: {Name}]
    B --> D[Story 4: {Name}]
    C --> D
```

**並行開発可能**: Story 2とStory 3は並行して開発可能

---

## 5. User Story進捗管理

**Phase 0完了時に作成し、各Storyの進捗を随時更新します。**

| Story ID | Story名 | Status | 担当 | Phase | PR | 備考 |
|----------|---------|--------|------|-------|-----|------|
| US-1 | {Story 1 Name} | Planning | - | - | - | - |
| US-2 | {Story 2 Name} | Planning | - | - | - | US-1待ち |
| US-3 | {Story 3 Name} | Planning | - | - | - | US-1待ち |
| US-4 | {Story 4 Name} | Planning | - | - | - | US-2, US-3待ち |

**Status**:
- `Planning`: Phase 0完了、Phase 1未着手
- `In Progress`: Phase 1-2実装中
- `Completed`: Phase 3完了、PRマージ済み

**Phase**:
- Phase 0: Epic定義
- Phase 1: 仕様定義
- Phase 2: 実装
- Phase 3: レビュー

**更新タイミング**:
- Phase 1開始時: Status → `In Progress`, Phase → `Phase 1`
- Phase 2開始時: Phase → `Phase 2`
- Phase 3完了時: Status → `Completed`, Phase → `Phase 3`, PR番号記入

---

## 6. 関連ドキュメント

### 参照ADR
- ADR-{Number}: {Title}

### 参照Design Doc（該当する場合）
- `docs/design-doc/{related-design-doc}.md`

### 関連Issue
- #{Issue Number}: {Title}

---

**作成者**: {Name}
**最終更新**: {YYYY-MM-DD}
