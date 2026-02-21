---
allowed-tools: Read, Write, Edit, MultiEdit, Glob, Grep, Bash(git:*), Bash(./scripts/safe-gradlew.sh:*), Bash(mkdir:*), TodoWrite, Task, mcp__serena__*, AskUserQuestion
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
Backlog → Spec → Design → Dev → Done
```

| ステップ | Kanbanカラム | 成果物 |
|---------|-------------|--------|
| 1. US選択 + Worktree作成 | Backlog → | US.md 確認 + Worktree |
| 2. 仕様定義 | → Spec | SPECIFICATION.md |
| 3. 設計 | → Design | DESIGN.md + PROGRESS.md |
| 4. 実装（レイヤー別エージェント委譲） | → Dev | Shared → UI → Server（固定順序） |
| 5. PR作成 | → Done | 全テスト通過 + PR |

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

### 1.2 小規模機能パス（Epicなし）

Epicに属さない小規模機能の場合（`/phase0` で US.md は作成済み）:

1. `implement-context/{us_name}/US.md` の内容から機能の概要を把握
2. Step 1.3 へ進む

### 1.3 Worktree作成

US選択後、feature名が確定した時点でworktreeを作成し、以降の全作業（仕様定義・設計・実装）をworktree内で行います。

```bash
/create-worktree {feature_name}
```

作成後、worktreeディレクトリに移動して Step 2 へ進みます。

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

### → Kanban更新: カードを `Design` へ移動

---

## Step 4: 実装 → `Dev`

**Task ツールを使用して、レイヤーごとに専門エージェントへ固定順序で委譲します。**

### 4.1 実装コンテキストの収集

以下のファイルの内容を読み込みます:
1. **SPECIFICATION.md** — 機能仕様（ユーザーストーリー、ビジネスルール、状態遷移）
2. **DESIGN.md** — 設計方針（変更対象ファイル、実装方針、技術的注意点）
3. **PROGRESS.md** — タスクチェックリスト

### 4.2 変更対象レイヤーの判定

DESIGN.md の「変更対象」セクションから、変更が必要なレイヤーを判定します:

| レイヤー | パス | 専門エージェント |
|---------|------|-----------------|
| Shared | `shared/` | `domain-layer-architect` |
| ComposeApp | `composeApp/` | `compose-multiplatform-specialist` |
| Server | `server/` | `kotlin-backend-specialist` |

### 4.3 順次エージェント起動

変更対象レイヤーに対して、以下の**固定順序**でエージェントを起動します。
変更がないレイヤーはスキップします。各エージェントは前のエージェントの完了を待ってから起動します。

```
1. Shared Layer    → domain-layer-architect
2. ComposeApp Layer → compose-multiplatform-specialist
3. Server Layer     → kotlin-backend-specialist
```

#### プロンプトテンプレート

Task ツールを呼び出します。`{...}` 部分は実際の値で置換します。

````
あなたは CollabStream プロジェクトの実装エージェントです。
**あなたの担当は {レイヤー名} です。** 他のレイヤーは別のエージェントが担当するため、変更しないでください。

## 作業ディレクトリ
{worktree の絶対パス}

## 担当レイヤー
{レイヤー名}（{担当パス}）

## 仕様（SPECIFICATION.md）
{SPECIFICATION.md の全文}

## 設計（DESIGN.md）
{DESIGN.md の全文}

## タスク一覧（PROGRESS.md）
{PROGRESS.md の全文}

あなたの担当レイヤーに関するタスクのみ実装し、完了時に PROGRESS.md のチェックボックスを `[x]` に更新してください。

## プロジェクト規約

### アーキテクチャ
- ADR-001: Android Architecture（Domain層オプショナル、UI Layer → Data Layer）
- ADR-002: MVI パターン（View → Intent → ViewModel → State → View）
- ADR-003: 4層 Component 構造（Route → Screen → Content → Component）
- 詳細: `.claude/rules/architecture/` 配下を参照

### テスト規約
- kotlin.test を使用（commonTest 配置）
- テスト名: バッククォート + 日本語（`` `{コンテキスト}_{期待する振る舞い}` ``）
- コメントセクション（`// ========`）でグルーピング
- Arrange-Act-Assert パターン
- commonTest では `@DisplayName`, `@Nested` 使用不可（JUnit 5 固有）
- 詳細: `.claude/rules/testing.md`

### 日時処理
- `kotlin.time.Clock` を使用（`kotlinx.datetime.Clock` は非推奨）

### DI
- Koin を使用

### 言語
- コード（変数名・関数名）: 英語
- コメント・ドキュメント: 日本語

### その他の規約ファイル（必要に応じて参照）
- Compose: `.claude/rules/compose/`
- Domain: `.claude/rules/shared/domain-rules.md`
- Server: `.claude/rules/server/ktor-rules.md`

## 実装手順

{レイヤー別の実装手順（下記参照）}

完了時にビルド確認コマンドを実行し、エラーがあれば修正してください:
{ビルドコマンド（下記参照）}
````

#### レイヤー別の実装手順とビルドコマンド

**Shared Layer**（`domain-layer-architect`）:
- 実装手順: Domain Models → Repository Interface → UseCase（必要時） → Repository実装 → テスト
- ビルド: `./scripts/safe-gradlew.sh :shared:build && ./scripts/safe-gradlew.sh :shared:test`

**ComposeApp Layer**（`compose-multiplatform-specialist`）:
- 実装手順: UiState/Intent → ViewModel（MVI） → UI Components（Route → Screen → Content → Component） → Navigation → ViewModelTest
- ビルド: `./scripts/safe-gradlew.sh :composeApp:build && ./scripts/safe-gradlew.sh :composeApp:test`

**Server Layer**（`kotlin-backend-specialist`）:
- 実装手順: Routes → Service → Module設定 → テスト
- ビルド: `./scripts/safe-gradlew.sh :server:build && ./scripts/safe-gradlew.sh :server:test`

### 4.4 UIデザインレビュー（ComposeApp 変更時のみ）

ComposeApp レイヤーの変更がある場合、統合確認の前にUIデザインレビューを実施します。

#### 4.4.1 Preview 生成

変更されたUIファイルに `@Preview` が不足している場合、自動生成します:

```bash
/generate-previews
```

#### 4.4.2 スクリーンショット取得 & レビュー

```bash
/review-ui
```

以下の観点でレビューを実施:
- **M3 準拠**: カラーシステム、タイポグラフィ、コンポーネント、スペーシング
- **プロジェクト規約**: 4層構造、アクセシビリティ、Preview カバレッジ
- **UI/UX 品質**: 状態カバレッジ、タッチターゲット、ダークモード対応
- **SPECIFICATION.md 整合**: ユーザーストーリー・状態遷移との一致

#### 4.4.3 フィードバック反映

レビュー結果が「🔴 要修正」の場合、`compose-multiplatform-specialist` エージェントを再起動してフィードバックを反映します。
「🟡 軽微な修正推奨」の場合は自身で修正します。
「🟢 LGTM」の場合は次のステップへ進みます。

### 4.5 統合確認

全エージェント完了後、以下の統合確認を行います:

1. **DI設定（Koin）**: Shared module + ComposeApp module の結合が必要な場合、自身で設定を追加
2. **全テスト実行**: `./scripts/safe-gradlew.sh --wait test`
3. PROGRESS.md を読み込み、全タスクにチェックが入っているか確認
4. 未完了タスクがある場合は該当レイヤーの専門エージェントに追加 Task を発行して補完

### → Kanban更新: カードを `Dev` へ移動

---

## Step 5: PR作成 → `Done`

### 5.1 全体品質確認

```bash
./scripts/safe-gradlew.sh --wait test
./scripts/safe-gradlew.sh --wait :shared:build
./scripts/safe-gradlew.sh --wait :composeApp:build
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

### → Kanban更新: カードを `Done` へ移動

### 5.4 Epic完了チェック & アーカイブ

Epicに属するUSの場合、Kanban更新後に以下を確認します:

1. EPIC.md の Kanban を読み込み、**全USが `Done` カラムにあるか**確認
2. 全USが Done の場合:
   - `implement-context/done/` ディレクトリを作成（存在しない場合）
   - Epic ディレクトリを移動: `implement-context/{epic_name}/` → `implement-context/done/{epic_name}/`
3. 未完了のUSがある場合: スキップ（移動しない）

**対象**: Epic配下のUSのみ。小規模機能パス（Epicなし）は対象外。

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
A: エージェントが自動判定してShared Layerをスキップします。PROGRESS.mdのShared Layerセクションも省略可。
