---
allowed-tools: Read, Write, Edit, MultiEdit, Glob, Grep, Bash(git:*), Bash(mkdir:*), TodoWrite, AskUserQuestion
description: Epic定義 & 共通基盤の切り出し - 大規模機能を複数USに分割
---

# Phase 0: Epic定義 & 共通基盤の切り出し

Phase 0では、大規模機能（Epic）を複数のUser Storyに分割し、並行実装時の重複を避けるための最小限の共通ドメインを定義します。

## Plan Modeでの実行を推奨

Phase 0はEpic・US分割の設計フェーズです。**Plan Mode**での実行を推奨します。

**推奨**: `/phase0`実行前にPlan Modeに入る

## 実行方法

```bash
/phase0
```

**注意**: Phase 0は大規模機能（3 US以上）の場合のみ使用します。小規模機能は `/develop` から直接開始してください。

## Context

### implement-context 確認
- 既存Epic: !`find implement-context -name "EPIC.md" -type f 2>/dev/null | head -10 || echo "Epicなし"`
- 既存US: !`find implement-context -name "US.md" -type f 2>/dev/null | head -10 || echo "USなし"`

### Domain Models確認
- Domain models: !`find shared/src/commonMain/kotlin -type f -path "*/domain/model/*.kt" 2>/dev/null | head -10`
- Repositories: !`find shared/src/commonMain/kotlin -type f -path "*/domain/repository/*.kt" 2>/dev/null | head -10`

### Git状態
- Current branch: !`git branch --show-current`
- Git status: !`git status --porcelain | head -10 || echo "Clean"`

---

## Overview

**Phase 0の目的**: 並行開発時の設計不整合を防ぐ

- Epic = 複数User Story（3+）のグループ → `implement-context/{epic_name}/EPIC.md`
- US = 1-3日の実装単位 → `implement-context/{epic_name}/us-{n}-{name}/US.md`
- 共通ドメイン = 並行実装時の重複を避けるための最小限の定義 → `shared/domain/`
- **進捗管理** = EPIC.md内のMermaid Kanbanで管理
- 詳細な実装 = `/develop` で各USごとに

**SSOT（Single Source of Truth）**: すべての情報は `implement-context/` 内のmarkdownファイルで管理。

---

## When to Use Phase 0

### Phase 0を使用すべき場合

- **3 US以上の大規模機能**
- **複数モジュールにまたがる機能**
- **共通ドメインモデルが必要な場合**

### Phase 0をスキップすべき場合

- **1-2 USの小規模機能** → `/develop` から直接開始

---

## Phase 0 Process

### Step 1: Epic Definition（EPIC.md作成）

#### 1.1 ディレクトリ作成

```bash
mkdir -p implement-context/{epic_name}
```

#### 1.2 EPIC.md作成

テンプレート `docs/design-doc/template/epic-template-v2.md` を参考に、`implement-context/{epic_name}/EPIC.md` を作成します。

**記載内容**:
- Epic概要（ビジョン、背景・課題、ユーザー価値）
- 共通ドメイン（Entity、Repository Interface）
- 開発進捗（Mermaid Kanban）
- 依存関係図（Mermaid）

---

### Step 2: User Story Breakdown（US.md作成）

#### 2.1 Story分割方針

**1 US = 1-3日の実装規模**

#### 2.2 各USのディレクトリとUS.md作成

```bash
mkdir -p implement-context/{epic_name}/us-1-{name}
mkdir -p implement-context/{epic_name}/us-2-{name}
mkdir -p implement-context/{epic_name}/us-3-{name}
```

テンプレート `docs/design-doc/template/us-template.md` を参考に、各 `US.md` を作成します。

#### 2.3 EPIC.md のKanbanを更新

全US作成後、EPIC.md内のMermaid Kanbanに全USカードを追加します。
初期状態では全USを `todo[未着手]` カラムに配置します。

```mermaid
kanban
  backlog[Backlog]
    us1[US-1: {Name}]@{ priority: 'High' }
    us2[US-2: {Name}]
    us3[US-3: {Name}]
  spec[Spec]
  design[Design]
  dev[Dev]
  done[Done]
```

各カラムは `/develop` のステップに対応。詳細は `/develop` を参照。

---

### Step 3: SPECIFICATION.md作成（画面単位）

**画面単位**で SPECIFICATION.md を作成します。USではなく画面が単位です。

#### 3.1 画面単位の原則

- **1画面 = 1 SPECIFICATION.md**（複数USが1画面に関わる場合は統合）
- UI を持たないUS（データ層のみ等）は SPECIFICATION.md を作成しない
- 画面内で状態遷移が複雑な場合は、サブ画面（モーダル等）ごとに分割可

#### 3.2 配置場所

```
composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/SPECIFICATION.md
composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/{sub_feature}/SPECIFICATION.md
```

#### 3.3 既存例

| ファイル | カバー範囲 |
|---------|-----------|
| `feature/timeline_sync/SPECIFICATION.md` | TimelineSync画面（US-1, 3, 4） |
| `feature/timeline_sync/channel_add/SPECIFICATION.md` | チャンネル追加モーダル（US-2, 5） |

→ 画面単位で作成し、ビジネスルールテーブルの「US」列でどのUSに属するか記録。

#### 3.4 構成

テンプレート `docs/design-doc/template/specification-template.md` を参考に作成。

**SPECIFICATION.md の構成（3セクション統合仕様書）**:
1. **ユーザーストーリー**: ユーザー操作と期待する動作を箇条書き
2. **ビジネスルール**: 機能要件、制約条件をテーブル形式（US列でUS番号を記録）
3. **状態遷移**: Mermaid図で画面内部の状態遷移を表現

---

### Step 4: Shared Domain Definition（最小限）

**重要**: Phase 0では並行実装時の重複を避けるための最小限のドメイン定義のみ。詳細は `/develop` で各USごとに。

#### 4.1 Entity定義

**配置**: `shared/src/commonMain/kotlin/org/example/project/domain/model/`

```kotlin
package org.example.project.domain.model

/**
 * {Entity説明}
 * Epic: {Epic Name}
 * 共通利用: US-1, US-2, US-3
 */
data class {Entity}(
    val id: String,
    // 複数USで共通利用するフィールド
)
```

#### 4.2 Repository Interface定義

**配置**: `shared/src/commonMain/kotlin/org/example/project/domain/repository/`

```kotlin
package org.example.project.domain.repository

import org.example.project.domain.model.{Entity}

/**
 * {Repository説明}
 * Epic: {Epic Name}
 */
interface {Repository} {
    // 共通メソッド（骨格のみ、実装は /develop で）
    suspend fun get{Entity}ById(id: String): Result<{Entity}>
}
```

#### 4.3 Phase 0では作成しないもの

- UseCase → `/develop` で各USごとに定義
- Repository Implementation → `/develop` で実装
- ViewModel → `/develop` で各USごとに定義
- UI Components → `/develop` で各USごとに定義

---

### Step 5: PR & Merge（共通ドメインコードのみ）

#### 5.1 Git Commit

```bash
git add implement-context/{epic_name}/
git add shared/src/commonMain/kotlin/org/example/project/domain/
git add composeApp/src/commonMain/kotlin/org/example/project/feature/

git commit -m "$(cat <<'EOF'
feat: {Epic Name} - Phase 0 Epic定義 & 共通ドメイン

Epic: {Epic Name}

成果物:
- EPIC.md + US.md 作成
- SPECIFICATION.md 作成
- Entity: {EntityName}
- Repository Interface: {RepositoryName}

各USは `/develop` から開始可能。

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

#### 5.2 PR作成

```bash
/pr
```

---

## Success Criteria

Phase 0完了の条件：

- [ ] **EPIC.md作成完了**
  - [ ] ビジョン・背景・ユーザー価値記載
  - [ ] 共通ドメイン情報記載
  - [ ] Mermaid Kanbanに全US配置

- [ ] **US.md作成完了**
  - [ ] 全US作成済み（3+ US）
  - [ ] 依存関係明記

- [ ] **SPECIFICATION.md作成完了**
  - [ ] 画面単位でSPECIFICATION.md作成済み（UIを持たないUSは対象外）
  - [ ] 3セクション構成（ユーザーストーリー、ビジネスルール、状態遷移）

- [ ] **Shared Domain実装完了**（最小限）
  - [ ] Entity定義
  - [ ] Repository Interface定義

- [ ] **Phase 0で作成しないもの確認**
  - [ ] UseCaseは含まれていない
  - [ ] Repository実装は含まれていない
  - [ ] ViewModelは含まれていない

- [ ] **PR作成完了**

---

## Next Steps

Phase 0完了後：

1. **US確認** → `implement-context/{epic_name}/` 内のUS一覧を確認
2. **US-1の実装開始** → `/develop` 実行
3. **並行US開始** → 依存関係を確認して並行開発
4. **進捗管理** → EPIC.md のMermaid Kanbanを更新

**各USは `/develop` コマンドで実装します。**
