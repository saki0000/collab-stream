---
allowed-tools: Read, Write, Edit, MultiEdit, Glob, Grep, Bash(git:*), Bash(mkdir:*), Bash(gh:*), TodoWrite
description: Epic定義 & 共通基盤の切り出し - 大規模機能を複数Storyに分割
---

# Phase 0: Epic定義 & 共通基盤の切り出し

Phase 0では、大規模機能（Epic）を複数のUser Storyに分割し、並行実装時の重複を避けるための最小限の共通ドメインを定義します。

## 🎯 実行方法

```bash
/phase0
```

**注意**: Phase 0は大規模機能（3 Story以上）の場合のみ使用します。小規模機能（1-2 Story）は `/phase1` から直接開始してください。

## Context

### 既存Epic確認
- Existing Epics: !`ls -la docs/design-doc/epic-*.md 2>/dev/null | tail -5 || echo "Epicなし"`
- Epic template: !`ls -la docs/design-doc/template/epic-template.md 2>/dev/null || echo "Template check"`

### Domain Models確認
- Domain models: !`find shared/src/commonMain/kotlin -type f -path "*/domain/model/*.kt" | head -10`
- Repositories: !`find shared/src/commonMain/kotlin -type f -path "*/domain/repository/*.kt" | head -10`

### Git状態
- Current branch: !`git branch --show-current`
- Git status: !`git status --porcelain | head -10 || echo "Clean"`

---

## Overview

**Phase 0の目的**: 並行開発時の設計不整合を防ぐ

- Epic = 複数User Story（3+）のグループ
- 共通ドメイン = 並行実装時の重複を避けるための最小限の定義
- **User Story進捗管理** = Epicレベルでの進捗トラッキング
- 詳細な実装 = Phase 1で各Storyごとに

---

## When to Use Phase 0

### ✅ Phase 0を使用すべき場合

- **3 Story以上の大規模機能**
- **複数モジュールにまたがる機能**
- **共通ドメインモデルが必要な場合**

### ❌ Phase 0をスキップすべき場合

- **1-2 Storyの小規模機能** → `/phase1` から直接開始

---

## Phase 0 Process

### Step 1: Epic Definition

#### 1.1 Epic Overview作成

**配置**: `docs/design-doc/epic-{epic-name}.md`
**テンプレート**: `docs/design-doc/template/epic-template.md`

**Epic概要セクション**:
```markdown
# Epic: {Epic Name}

> **Phase 0**: 大規模機能の分割と共通ドメイン定義

## メタデータ
- **Epic ID**: {EPIC-XXX}
- **作成日**: {YYYY-MM-DD}
- **担当**: {Name}

---

## 1. Epic概要

### ビジョン
{このEpicで実現したい大きなゴール（1-2段落）}

### 背景・課題
{なぜこのEpicが必要か}

### ユーザー価値
- {価値1: ユーザーが得られる具体的なメリット}
- {価値2: ビジネスへの貢献}
```

---

### Step 2: User Story Breakdown

#### 2.1 Story Splitting Strategy

**1 Story = 1-3日の実装規模**

```markdown
## 2. User Story分割

| Story ID | Story名 | User Story | 依存 | 規模 |
|----------|---------|------------|------|------|
| US-1 | User Registration | As a new user, I want to create an account | - | 2日 |
| US-2 | User Login | As a registered user, I want to log in | US-1 | 1日 |
| US-3 | Profile Editing | As a logged-in user, I want to edit my profile | US-2 | 2日 |
```

#### 2.2 Dependency Mapping（Mermaid）

```markdown
## 3. Story依存関係

\`\`\`mermaid
graph TD
    A[US-1: User Registration] --> B[US-2: User Login]
    B --> C[US-3: Profile Editing]
    B --> D[US-4: Account Settings]
\`\`\`

**並行開発可能**: US-3とUS-4は並行して開発可能（US-2完了後）
```

#### 2.3 User Story進捗管理（新規）

**Epic定義書に追加するセクション**:
```markdown
## 4. User Story進捗管理

| Story ID | Story名 | Status | 担当 | Phase | PR | 備考 |
|----------|---------|--------|------|-------|-----|------|
| US-1 | User Registration | Planning | - | - | - | - |
| US-2 | User Login | Planning | - | - | - | US-1待ち |
| US-3 | Profile Editing | Planning | - | - | - | US-2待ち |
| US-4 | Account Settings | Planning | - | - | - | US-2待ち |

**Status**:
- `Planning`: Phase 0完了、Phase 1未着手
- `In Progress`: Phase 1-2実装中
- `Completed`: Phase 3完了、PRマージ済み

**Phase**:
- Phase 0: Epic定義
- Phase 1: 仕様定義
- Phase 2: 実装
- Phase 3: レビュー
```

**更新タイミング**:
- Phase 1開始時: Status → `In Progress`, Phase → `Phase 1`
- Phase 2開始時: Phase → `Phase 2`
- Phase 3完了時: Status → `Completed`, Phase → `Phase 3`, PR番号記入

---

### Step 3: Shared Domain Definition（最小限）

**重要**: Phase 0では並行実装時の重複を避けるための最小限のドメイン定義のみ。詳細はPhase 1で各Storyごとに。

#### 3.1 Entity定義（1例のみ）

**配置**: `shared/src/commonMain/kotlin/org/example/project/domain/model/`

```kotlin
// shared/domain/model/User.kt

package org.example.project.domain.model

/**
 * User entity
 * Epic: User Management
 * Shared across: US-1, US-2, US-3, US-4
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val createdAt: Long
)
```

**作成ガイドライン**:
- immutableなdata class
- プラットフォーム非依存
- KDocでEpic参照、共有Story列挙

#### 3.2 What NOT to Implement

**❌ Phase 0では作成しない**:
- UseCase → Phase 1で各Storyごとに定義
- Repository Implementation → Phase 1で実装
- ViewModel → Phase 1で各Storyごとに定義
- UI Components → Phase 1で各Storyごとに定義

---

### Step 4: PR & Merge

#### 4.1 Git Commit

```bash
git add docs/design-doc/epic-{epic-name}.md
git add shared/src/commonMain/kotlin/org/example/project/domain/

git commit -m "feat: Epic {Epic Name} - Phase 0

Epic Definition:
- Epic overview & user story breakdown
- Story dependencies (Mermaid)
- User Story進捗管理テーブル

Shared Domain:
- Entity: {EntityName}
- Repository Interface: {RepositoryName}

各StoryはPhase 1から開始可能。

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

#### 4.2 PR Creation

**PR Title**: `feat: Epic {Epic Name} - Phase 0`

**PR Body**:
```markdown
## Epic Overview
{Epic名}: {簡単な説明}

## Phase 0成果物
- [x] Epic概要（ビジョン、背景、ユーザー価値）
- [x] User Story分割（{N} stories）
- [x] Story依存関係（Mermaid図）
- [x] User Story進捗管理テーブル
- [x] 共通Domain定義（Entity, Repository Interface）

## Next Steps
各StoryのPhase 1（`/phase1`）を順次開始
```

---

## Success Criteria

Phase 0完了の条件：

- [ ] **Epic Definition作成完了**
  - [ ] Epic概要（ビジョン、背景、ユーザー価値）
  - [ ] User Story分割（3+ stories）
  - [ ] Story依存関係（Mermaid図）
  - [ ] **User Story進捗管理テーブル**（新規）

- [ ] **Shared Domain実装完了**（最小限）
  - [ ] Entity定義（1例）
  - [ ] Repository Interface定義（1例）
  - [ ] KDoc記載（Epic参照、使用Story明記）

- [ ] **What NOT to Implement確認**
  - [ ] UseCaseは含まれていない
  - [ ] Repository実装は含まれていない
  - [ ] ViewModelは含まれていない

- [ ] **PR作成 & マージ完了**

---

## Next Steps

Phase 0完了後：

1. **Story 1のPhase 1開始** → `/phase1` コマンド実行
2. **並行Story開始** → 依存関係を確認して並行開発
3. **進捗管理更新** → Epic定義書のUser Story進捗テーブルを随時更新

**各StoryはPhase 1 → Phase 2 → Phase 3のサイクルで実装**します。

---

## Notes

### よくある質問

**Q1: 共通ドメインはどこまで定義すべき？**
A: 並行実装時の重複を避けるための最小限のみ。全てを網羅する必要はありません。

**Q2: User Story進捗管理はいつ更新する？**
A: 各StoryのPhase移行時に更新します（Phase 1開始時、Phase 2開始時、Phase 3完了時）。

**Q3: Repository Interfaceのメソッドは？**
A: 各Storyで使用するメソッドの骨格のみ。実装はPhase 1で。

**Q4: Story分割の粒度は？**
A: 1 Story = 1-3日。大きすぎる場合は分割。

---

**Phase 0完了後、各Storyの `/phase1` から開始してください！**