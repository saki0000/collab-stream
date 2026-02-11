---
allowed-tools: Read, Write, Edit, MultiEdit, Glob, Grep, Bash(git:*), Bash(./gradlew:*), Bash(mkdir:*), TodoWrite, Task, mcp__serena__*, AskUserQuestion
description: 仕様定義から実装・PR作成までの統合開発コマンド
---

# /develop: 統合開発コマンド

仕様定義（SPECIFICATION.md）から実装・テスト・PR作成までを一貫して行う統合コマンドです。

## Context

### implement-context 確認
- EPIC.md（Kanban）: !`find implement-context -name "EPIC.md" -type f 2>/dev/null | head -10 || echo "Epicなし"`
- US一覧: !`find implement-context -name "US.md" -type f 2>/dev/null | head -20 || echo "implement-context/ にUSなし"`

### 既存仕様・実装確認
- SPECIFICATION一覧: !`find composeApp/src/commonMain/kotlin -name "SPECIFICATION.md" 2>/dev/null | head -10`
- ViewModel一覧: !`find composeApp/src/commonMain/kotlin -name "*ViewModel.kt" 2>/dev/null | head -10`

### ドメイン構造
- Domain models: !`find shared/src/commonMain/kotlin -type f -path "*/domain/model/*.kt" 2>/dev/null | head -10`
- Repositories: !`find shared/src/commonMain/kotlin -type f -path "*/domain/repository/*.kt" 2>/dev/null | head -10`

### Git状態
- Current branch: !`git branch --show-current`
- Git status: !`git status --porcelain | head -10 || echo "Clean"`

---

## 実行ルール

- **EnterPlanMode は使用禁止**: 設計は DESIGN.md / PROGRESS.md で行う
- **AskUserQuestion は Step 1（US選択）のみ**: それ以降は自律的に進行し、PR で確認する
- **各ステップを飛ばさず順番に実行する**

---

## ワークフロー概要

各ステップはKanbanカラムと対応しています。
ステップ完了ごとにEPIC.mdのKanbanカードを次のカラムへ移動します。

```
Backlog → Spec → Design → Dev → Review → Done
```

| ステップ | Kanbanカラム | 成果物 |
|---------|-------------|--------|
| 1. US選択 | Backlog → | US.md 確認 |
| 2. 仕様定義 | → Spec | SPECIFICATION.md |
| 3. 設計 | → Design | DESIGN.md + PROGRESS.md + Worktree |
| 4. 実装 | → Dev | Shared + UI + テスト + DI |
| 5. レビュー・PR | → Review | 全テスト通過 + PR |
| (マージ後) | → Done | - |

---

## Step 0: Kanban確認

EPIC.md が存在する場合、Mermaid Kanban を読み込んで現在の進捗を表示します。

**確認内容**:
- 各USの現在カラム
- 依存関係（blocked なUSがないか）
- 途中再開の場合、前回どのステップまで完了したか

**途中再開**: Kanbanで `Backlog` 以降にあるUSを選択した場合、該当カラムのステップから再開します。

Kanbanが存在しない場合（小規模機能パス）はスキップします。

---

## Step 1: US選択 → `Backlog`

### 1.1 既存USから選択

`implement-context/` 内のUS一覧を確認し、AskUserQuestion で選択肢を提示します。
Kanbanが存在する場合、現在のカラム状態に基づいて選択肢を表示します。

### 1.2 小規模機能パス（新規US作成）

Epicに属さない小規模機能の場合:

1. US.md の内容から機能の概要を把握
2. `implement-context/{us_name}/` ディレクトリ作成
3. `US.md` を作成（テンプレート: `docs/design-doc/template/us-template.md`）

---

## Step 2: 仕様定義 → `Spec`

### 2.1 SPECIFICATION.md 確認・作成

**配置場所**:
```
composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/SPECIFICATION.md
```

- **既存ファイルあり**: 内容を読み込んで確認
- **既存ファイルなし**: 新規作成（テンプレート: `docs/design-doc/template/specification-template.md`）

### 2.2 SPECIFICATION.md の構成（3セクション統合仕様書）

1. **ユーザーストーリー**: ユーザー操作と期待する動作を箇条書き
2. **ビジネスルール**: 機能要件、制約条件をテーブル形式
3. **状態遷移**: Mermaid図で画面内部の状態遷移を表現

### 2.3 ナビゲーション設計（必要な場合）

新しい画面を追加する場合:
1. `docs/screen-navigation.md` を確認・更新
2. 必要に応じて `docs/navigation/{module}-module.md` を作成・更新

### → Kanban更新: カードを `Spec` へ移動

---

## Step 3: 設計 → `Design`

### 3.1 コードベース分析

- 類似機能の実装パターンを調査
- 関連するADR（`.claude/rules/architecture/`）を確認
- 既存のDomain Models / Repository を確認
- DI設定（Koin）の構造を確認

### 3.2 DESIGN.md 作成

配置: `implement-context/{epic_name}/{us_name}/DESIGN.md` または `implement-context/{us_name}/DESIGN.md`

内容:
- 変更対象ファイル一覧（具体的なファイル名）
- 実装方針（レイヤー別）
- 既存コードとの関連
- 技術的な注意点

テンプレート: `docs/design-doc/template/design-template.md`

### 3.3 PROGRESS.md 作成（タスク分割）

DESIGN.md で決定した変更対象ファイルに基づき、具体的なタスクに分割します。
テンプレート `docs/design-doc/template/progress-template.md` の `{placeholder}` を実際のファイル名・クラス名で埋めます。

### 3.4 Worktree作成

```bash
/create-worktree {feature_name}
```

### → Kanban更新: カードを `Design` へ移動

---

## Step 4: 実装 → `Dev`

PROGRESS.md のタスクを上から順に実装していきます。

### 4.1 Shared Layer（Domain/Data）

1. Domain Models 実装
2. Repository Interface 定義
3. UseCase 実装（必要な場合）
4. Repository 実装
5. Data Source / Mapper（必要な場合）
6. テスト作成
7. ビルド確認: `./gradlew :shared:build && ./gradlew :shared:test`

### 4.2 ComposeApp Layer（ViewModel/UI）

1. UiState / Intent 定義
2. ViewModel 実装（MVI パターン - ADR-002）
3. UI Components 実装（4層構造 - ADR-003: Route → Screen → Content → Component）
4. Navigation graph への登録
5. ViewModelTest 実装
6. ビルド確認: `./gradlew :composeApp:build && ./gradlew :composeApp:test`

### 4.3 Integration

1. DI 設定（Koin）: Shared module + ComposeApp module
2. 全テスト実行: `./gradlew test`

### 4.4 テスト規約

- **フレームワーク**: kotlin.test（commonTest 配置）
- **テスト名**: バッククォートで日本語記述
  - 形式: `` `{コンテキスト}_{期待する振る舞い}`() ``
- **グルーピング**: コメントセクション（`// ========================================`）
- **パターン**: Arrange-Act-Assert

```kotlin
// ========================================
// 画面を開いた時
// ========================================

@Test
fun `画面を開いた時_ローディング状態になること`() {
    // Arrange
    // Act
    // Assert
}
```

**注意**: `commonTest` では `@DisplayName` や `@Nested` は使用不可（JUnit 5 固有のため）

### 4.5 PROGRESS.md 随時更新

実装完了したタスクのチェックボックスを随時チェックします。

### → Kanban更新: カードを `Dev` へ移動

---

## Step 5: レビュー・PR → `Review`

### 5.1 全体品質確認

```bash
./gradlew test
./gradlew :shared:build
./gradlew :composeApp:build
```

### 5.2 PROGRESS.md 最終確認

全タスクが完了していることを確認。メモ欄に申し送り事項があれば記録。

### 5.3 PR作成

```bash
/pr
```

PR作成時の情報源:
- SPECIFICATION.md: 仕様概要
- DESIGN.md: 実装方針
- PROGRESS.md: 完了タスク

### → Kanban更新: カードを `Review` へ移動（マージ後 `Done` へ）

---

## 準拠するADR

| ADR | 内容 | 適用箇所 |
|-----|------|---------|
| ADR-001 | Android Architecture | レイヤー構造 |
| ADR-002 | MVI パターン | ViewModel / State / Intent |
| ADR-003 | 4層 Component 構造 | Route → Screen → Content → Component |
| ADR-004 | 手動同期方式 | 同期機能の場合 |
| ADR-005 | 段階的APIセキュリティ | API連携の場合 |

---

## Notes

### 途中再開について

`/develop` は各ステップでKanbanを更新するため、途中で中断しても再開時にStep 0で現在位置を把握できます。
Kanban上のカラム位置から、該当ステップに直接ジャンプして続行します。

### よくある質問

**Q1: SPECIFICATION.md がまだない場合は？**
A: Step 2で対話的に仕様を定義します。

**Q2: Epicに属するUSの場合は？**
A: `/phase0` で作成済みの `implement-context/{epic_name}/{us_name}/US.md` を選択します。

**Q3: 小規模な修正の場合は？**
A: `/develop` で新規USを作成し、簡潔なSPECIFICATION.mdを作成して実装します。

**Q4: テストはどこに配置する？**
A: `shared/src/commonTest/` と `composeApp/src/commonTest/` にkotlin.testで記述します。

**Q5: Shared層の変更がない場合は？**
A: Step 4.1をスキップし、4.2から開始します。PROGRESS.mdのShared Layerセクションも省略可。
