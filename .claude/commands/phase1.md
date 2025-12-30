---
allowed-tools: Read, Write, Edit, MultiEdit, Glob, Grep, Bash(git:*), Bash(mkdir:*), Bash(gh:*), TodoWrite
description: 仕様・インターフェース定義（合意レビュー） - REQUIREMENTS.mdとテスト骨格の作成
---

# Phase 1: 仕様・インターフェース定義（合意レビュー）

Phase 1では、「何を作るか」を明確にし、AIと人間が共に参照できる詳細仕様を作成します。

**前提**: User Storyは既に定義済み（Phase 0で作成、または小規模機能の場合は直接Phase 1から）

## 🎯 実行方法

```bash
/phase1
```

## Context

### Epic確認（該当する場合）
- Epic定義書: !`ls -la docs/design-doc/epic-*.md 2>/dev/null | tail -5 || echo "Epicなし"`
- 共通ドメイン: !`find shared/src/commonMain/kotlin -type f -name "*.kt" | grep -E "(domain/model|domain/repository)" | head -10`

### 既存パターン参照
- REQUIREMENTS examples: !`find composeApp/src/commonMain/kotlin -name "REQUIREMENTS.md" | head -5`
- ViewModel examples: !`find composeApp/src/commonMain/kotlin -name "*ViewModel.kt" | head -5`

### Git状態
- Current branch: !`git branch --show-current`
- Git status: !`git status --porcelain | head -10 || echo "Clean"`

---

## Overview

**Phase 1の目的**: 「何を作るか」の合意

実装前に以下を明確にします：
- **ユーザーストーリー**: ユーザー操作と期待する動作
- **ビジネスルール**: 機能要件と制約条件
- **画面状態遷移**: Mermaid図で視覚化
- **テスト仕様**: ViewModelTestで振る舞いを定義
- **Phase 2実装進捗管理**: Shared/ComposeApp層の進捗トラッキング（新規）

**重要な原則**:
- 実装の詳細は書かない（Phase 2でAIが推論）
- 「何を作るか」に集中
- テストで仕様を表現（実行可能な仕様書）

---

## Phase 1 Process

### Step 1: REQUIREMENTS.md作成

#### 1.1 配置場所
```
composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/REQUIREMENTS.md
```

#### 1.2 Section 1: ユーザーストーリー

**箇条書きで記述**:

```markdown
## 1. ユーザーストーリー

- ユーザーが画面を開くと、自動的にデータ一覧を読み込む
- 読み込み中はローディングインジケータを表示する
- データ取得に成功した場合、リスト形式で表示する
- データ取得に失敗した場合、エラーメッセージと「再試行」ボタンを表示する
- 「再試行」ボタンを押すと、再度データ取得を試みる
```

**ポイント**:
- ユーザーの操作から記述
- 期待する動作を明確に
- エラーケースも含める

#### 1.3 Section 2: ビジネスルール

**カテゴリ別に整理**:

```markdown
## 2. ビジネスルール

- **データソート**: 更新日時の新しい順
- **取得件数**: 一度の取得は20件まで
- **キャッシュ**: 取得したデータは5分間キャッシュ
- **エラーハンドリング**:
  - ネットワークエラー: 「再試行」ボタン付きエラー画面
  - 認証エラー: ログイン画面へ遷移
  - サーバーエラー: エラーメッセージ表示
```

**ポイント**:
- 数値は具体的に
- エラーハンドリングの詳細
- 制約条件を明記

#### 1.4 Diagram Files作成

**作成するファイル**:
```bash
# 機能ディレクトリに移動
cd composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}

# Screen Transition Diagram（Level 3）を作成
touch screen-transition.md
```

**テンプレートからコピー**:
```bash
# テンプレートをコピー
cp docs/design-doc/template/screen-transition-template.md \
   composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/screen-transition.md
```

**Note**: Level 2（モジュール単位の画面遷移図）は `docs/navigation/{module}-module.md` に配置され、個別feature内には作成されません。

#### 1.5 Diagram Content作成

**Screen Transition Diagram (screen-transition.md - Level 3)**:

1. **基本状態の定義**:
   - Loading → Content → Error → Empty
   - Section 2のビジネスルールから状態を抽出

2. **遷移トリガーの記述**:
   - ユーザー操作（ボタンタップ、入力等）
   - システムイベント（API成功/失敗等）

3. **Nested Statesの追加（必要に応じて）**:
   - Content内の表示モード切替（List/Grid）
   - Loading内のキャッシュ処理

**App Navigation (docs/screen-navigation.md)**:

新機能の場合のみ更新:
```bash
# docs/screen-navigation.md を編集
# 1. Mermaid図に機能ノードを追加
# 2. Feature List Tableに行を追加
# 3. 色分けを適用（feature areaに応じて）
# 4. 詳細図へのリンクを追加（dotted lines）
```

#### 1.6 Section 3: 画面フローと状態遷移

**別ファイルへの参照を記載**:

```markdown
## 3. 画面フローと状態遷移

機能の詳細な振る舞いと状態遷移については、以下を参照してください。

### 画面内の振る舞い（Level 3）
画面の状態（Loading, Content, Error等）とユーザーアクション:
- **Screen Transition**: [screen-transition.md](./screen-transition.md)

### アプリ全体のインデックス（Level 1）
この機能が全体のどこに位置するか:
- **App Navigation**: [/docs/screen-navigation.md](/docs/screen-navigation.md)
```

**ポイント**:
- Level 2（モジュール単位の画面遷移）は `docs/navigation/{module}-module.md` で管理
- Level 3（画面内の振る舞い）は各feature内の screen-transition.md で定義
- REQUIREMENTS.mdからは参照リンクのみ

#### 1.7 Section 4: Phase 2実装進捗（新規）

**REQUIREMENTS.mdに追加する新セクション**:

```markdown
## 4. Phase 2実装進捗

**最終更新**: YYYY-MM-DD

### Shared Layer
- [ ] Domain Models実装
- [ ] Repository Interface実装
- [ ] UseCase実装
- [ ] Domain Tests実装
- [ ] Build成功（`./gradlew :shared:build`）

### ComposeApp Layer
- [ ] ViewModel実装（MVI pattern）
- [ ] UI Components実装（4層構造）
- [ ] ViewModel Tests実装
- [ ] DI設定（Koin）
- [ ] Build成功（`./gradlew :composeApp:build`）
- [ ] 全テスト成功（`./gradlew test`）
- [ ] Phase 3レビュー準備完了
```

**更新タイミング**:
- Phase 2開始時: このセクションを作成
- Phase 2実装中: 各タスク完了時にチェックボックスを更新
- Phase 3開始時: 全チェック完了を確認

---

### Step 2: ViewModelTest作成（必須）

#### 2.1 配置場所
```
composeApp/src/commonTest/kotlin/org/example/project/feature/{feature_name}/{Feature}ViewModelTest.kt
```

#### 2.2 テスト構造（簡潔版）

**テンプレート**:
```kotlin
package org.example.project.feature.{feature_name}

import kotlin.test.Test
import kotlin.test.DisplayName

/**
 * {機能名}画面の振る舞い仕様
 * Specification: feature/{feature_name}/REQUIREMENTS.md
 */
@DisplayName("{機能名}画面の振る舞い仕様")
class {Feature}ViewModelTest {

    @Nested
    @DisplayName("画面を開いた時")
    inner class OnInitialize {

        @Test
        @DisplayName("まずはローディング状態になること")
        fun startsWithLoading() {
            // TODO: Phase 2でAI実装
        }

        @Nested
        @DisplayName("データ取得に成功した場合")
        inner class OnSuccess {
            @Test
            @DisplayName("コンテンツが表示されること")
            fun showContent() {
                // TODO: Phase 2でAI実装
            }
        }

        @Nested
        @DisplayName("データ取得に失敗した場合")
        inner class OnFailure {
            @Test
            @DisplayName("エラー状態になること")
            fun showsError() {
                // TODO: Phase 2でAI実装
            }
        }
    }

    @Nested
    @DisplayName("再試行ボタンを押した時")
    inner class OnRetry {
        @Test
        @DisplayName("再度データ取得を試みること")
        fun retriesDataFetch() {
            // TODO: Phase 2でAI実装
        }
    }
}
```

**テスト構造の設計ポイント**:
- @DisplayName: 日本語で仕様を記述
- @Nested: 階層的に整理
- TODOコメント: Phase 2でAIが実装
- REQUIREMENTS.mdの状態遷移と対応

---

### Step 3: UseCaseTest作成（任意）

**作成する条件**:
- 複雑なビジネスルールがある場合
- ドメインロジックのテストが必要な場合

#### 3.1 配置場所
```
shared/src/commonTest/kotlin/org/example/project/domain/usecase/{UseCase}Test.kt
```

#### 3.2 テンプレート（簡潔版）

```kotlin
package org.example.project.domain.usecase

import kotlin.test.Test
import kotlin.test.DisplayName

@DisplayName("{機能名}のビジネスルール")
class {UseCase}Test {

    @Test
    @DisplayName("{ビジネスルール説明}")
    fun testBusinessRule() {
        // TODO: Phase 2でAI実装
    }
}
```

---

### Step 4: Interface Skeleton（オプション）

**作成する判断基準**:
- ✅ 複雑な状態管理が必要
- ✅ 複数のIntent（ユーザー操作）がある
- ❌ シンプルな画面（Loading → Content → Error のみ）→ Phase 2でAI生成

#### 作成する場合のファイルリスト

```
composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/
├── {Feature}ViewModel.kt          # ViewModel骨格
├── {Feature}UiState.kt            # State定義
└── {Feature}Intent.kt             # Intent定義
```

**Phase 2でAIが自動生成できる場合はスキップ推奨**

---

### Step 5: Directory Structure

#### 5.1 基本構造

```
composeApp/src/commonMain/kotlin/.../feature/{feature_name}/
  ├── REQUIREMENTS.md                # ✅ Phase 1で作成（4セクション）
  └── ui/                            # UI Components（Phase 2で作成）

composeApp/src/commonTest/kotlin/.../feature/{feature_name}/
  └── {Feature}ViewModelTest.kt     # ✅ Phase 1で作成（空のテスト）

shared/src/commonTest/kotlin/.../domain/usecase/
  └── {UseCase}Test.kt               # ⚪ Phase 1で作成（任意）
```

#### 5.2 ディレクトリ作成

```bash
mkdir -p composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/ui
mkdir -p composeApp/src/commonTest/kotlin/org/example/project/feature/{feature_name}
```

---

### Step 6: Phase 1 Review & PR

#### 6.1 レビュー観点

**「何を作るか」の合意**:
- [ ] REQUIREMENTS.mdが明確か（4セクション）
- [ ] ViewModelTestで仕様が表現されているか
- [ ] Screen Transition Diagram作成完了（screen-transition.md - Level 3）
- [ ] App Navigation更新完了（screen-navigation.md - Level 1、新機能の場合）
- [ ] Mermaid図が振る舞いと状態遷移を正しく表現しているか
- [ ] Phase 2実装進捗セクションが追加されているか
- [ ] エラーケースが含まれているか

**Note**: Level 2（モジュール単位の画面遷移）は個別featureではなく `docs/navigation/{module}-module.md` で管理されます。

#### 6.2 PR作成

**ブランチ**:
```bash
git checkout -b feature/{feature_name}-requirements
```

**PR Title**: `feat: {機能名} - Phase 1 仕様定義`

**PR Body**:
```markdown
## Phase 1: 仕様・インターフェース定義

### 成果物
- [x] REQUIREMENTS.md（4セクション）
- [x] ViewModelTest.kt（空のテスト骨格）
- [x] Mermaid状態遷移図
- [x] Phase 2実装進捗セクション

### 次のステップ
承認後、Phase 2（`/phase2`）でAI実装開始
```

**PR作成コマンド**:
```bash
git add composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/
git add composeApp/src/commonTest/kotlin/org/example/project/feature/{feature_name}/

git commit -m "feat: {機能名} - Phase 1 仕様定義

- REQUIREMENTS.md作成（4セクション）
- ViewModelTest.kt作成（空のテスト骨格）
- Mermaid状態遷移図追加
- Phase 2実装進捗セクション追加

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

gh pr create --title "feat: {機能名} - Phase 1 仕様定義" --body "（上記内容）"
```

#### 6.3 GitHub Issue作成

**Issue Title**: `[Phase 2] {機能名} - 実装タスク`

**Issue Body**:
```markdown
## 概要
{機能の簡単な説明}

## 仕様書
- **REQUIREMENTS.MD**: `composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/REQUIREMENTS.md`
- **Phase 1 PR**: #{PR_NUMBER}

## 実装タスク
Phase 2（`/phase2`）で実装。進捗はREQUIREMENTS.md Section 4で管理。

## 参照ADR
- ADR-001: Clean Architecture
- ADR-002: MVIパターン
- ADR-003: 4層Component

## 次のアクション
```bash
/phase2
```
```

---

## Success Criteria

Phase 1完了の条件：

- [ ] **REQUIREMENTS.md作成完了**（4セクション）
  - [ ] Section 1: ユーザーストーリー
  - [ ] Section 2: ビジネスルール
  - [ ] Section 3: 画面状態遷移（Mermaid図）
  - [ ] Section 4: Phase 2実装進捗（新規）

- [ ] **ViewModelTest.kt作成完了**（必須）
  - [ ] @DisplayName + @Nested 構造
  - [ ] 空のテストメソッド（TODOコメント付き）
  - [ ] REQUIREMENTS.mdと整合性あり

- [ ] **UseCaseTest.kt作成完了**（該当する場合）
  - [ ] ビジネスルールをテストで表現

- [ ] **レビュー完了**
  - [ ] Phase 1 PR作成
  - [ ] レビュー承認
  - [ ] Phase 2 Issue作成

---

## Next Steps

Phase 1完了後：

1. **PR承認待ち** → Phase 1ブランチをマージまたは保持
2. **Phase 2実装** → `/phase2` コマンド実行
3. **進捗管理** → REQUIREMENTS.md Section 4のチェックボックスを更新

**Phase 2で実現すること**:
- Shared Layer実装（Domain/Data）
- ComposeApp Layer実装（ViewModel/UI）
- テスト実装（空のテストを完全実装）
- DI設定（Koin）

---

## Notes

### よくある質問

**Q1: Interface骨格は必ず作成すべき？**
A: いいえ。シンプルな画面の場合、Phase 2でAIが推論できます。

**Q2: REQUIREMENTS.mdにコード例を含めるべき？**
A: いいえ。「何を作るか」に集中し、実装の詳細は書きません。

**Q3: ViewModelTestはどこまで詳細に書くべき？**
A: Phase 1では空のテストメソッド（TODOコメント付き）のみです。

**Q4: Phase 2実装進捗セクションの更新頻度は？**
A: Phase 2実装中に各タスク完了時に更新します。

---

**Phase 1完了後、次は `/phase2` コマンドでAI実装を開始してください。**