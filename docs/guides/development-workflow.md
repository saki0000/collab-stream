# AI×KMP 仕様駆動開発（SDD）運用ガイドライン

## 目次
1. [はじめに](#1-はじめに)
2. [基本原則](#2-基本原則)
3. [Phase 0: Epic定義 & 共通盤の切り出し](#3-phase-0-epic定義--共通盤の切り出し)
4. [Phase 1: 仕様・インターフェース定義](#4-phase-1-仕様インターフェース定義)
5. [Phase 2: AIによる実装](#5-phase-2-aiによる実装)
6. [Phase 3: 実装レビュー](#6-phase-3-実装レビュー)
7. [ツールとテンプレート](#7-ツールとテンプレート)
8. [ベストプラクティス](#8-ベストプラクティス)

---

## 1. はじめに

### 目的
CollabStreamプロジェクトにおけるAI（Claude Code）を活用した効率的な開発ワークフローを定義し、一貫性のある開発プロセスを提供します。

### 対象範囲
- **プラットフォーム**: Kotlin Multiplatform (Android/iOS/Web/Server)
- **UIフレームワーク**: Compose Multiplatform
- **アーキテクチャ**: Clean Architecture + MVI Pattern
- **開発規模**: Story単位〜Epic単位の機能開発

### 前提知識
本ガイドラインを使用する前に、以下のドキュメントを理解していることを前提とします：

- **CLAUDE.md**: プロジェクト構造とコマンド
- **ADR 001-005**: アーキテクチャ決定記録（`.claude/rules/architecture/`）
- **docs/architecture/**: システムアーキテクチャとデザインパターン

---

## 2. 基本原則

### 2.1 Vertical Slice Architecture
**機能ごとに垂直方向に実装**を行います。レイヤーごとではなく、「ユーザーができること」単位でタスクを切ります。

**利点**:
- ユーザー価値を早期に提供
- 機能単位での並行開発が可能
- テストとレビューが明確

**実装単位**: feature/{feature_name}/ ← 1つのVertical Slice
- composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/: UI層（ViewModel、UiState、Intent、UI Components）
- shared/domain/: ビジネスロジック層（Models、Repository Interfaces、UseCases）
- shared/data/: データアクセス層（Repository Implementations）

### 2.2 Behavior via ViewModel
アプリの**振る舞い（仕様）はViewModelのテスト**で定義します。

**詳細**: ADR-002（MVIパターン採用）を参照してください。

**重要**:
- Phase 1では空のテストクラス・メソッドのみを作成
- Phase 2でAIがSPECIFICATION.mdに基づいて実装を追加
- MVIパターンの詳細構造はADR-002を参照

### 2.3 Two-Phase Review
「何を作るか（合意）」と「どう作ったか（実装）」のレビューを分けます。

**Phase 1レビュー**: 仕様合意
- SPECIFICATION.mdの内容レビュー
- 実装コストの見積もり

**Phase 3レビュー**: 実装品質
- 仕様適合性確認
- ADR準拠確認
- テストカバレッジ確認

---

## 3. Phase 0: Epic定義 & 共通盤の切り出し

### 3.1 目的
**並行開発時の設計不整合を防ぐ**ため、大規模機能（Epic）を複数のStoryに分割し、共通ドメインを事前に定義します。

### 3.2 適用タイミング
以下のいずれかに該当する場合、Phase 0から開始します：

- **3 Story以上の大規模機能**
- **複数モジュールにまたがる機能**
- **共通ドメインモデルが必要な場合**

小規模機能（1-2 Story）の場合は、Phase 1から直接開始してください。

### 3.3 成果物
- **Epic定義書**: `docs/design-doc/epic-{epic-name}.md`
- **共通ドメイン実装**: `shared/src/commonMain/kotlin/org/example/project/domain/model/`, `shared/src/commonMain/kotlin/org/example/project/domain/repository/`（EntityとRepository Interfaceのみ）

### 3.4 実行方法

```bash
/phase0
```

**詳細**: `/phase0` コマンドを参照してください。

**プロセス概要**:
1. Epic概要の記述
2. 共通ドメインエンティティの抽出
3. Story分割（1 Story = 1-3日）
4. 依存関係の可視化（Mermaid図）
5. PR作成とマージ

**次のステップ**: 各Storyで Phase 1へ

---

## 4. Phase 1: 仕様・インターフェース定義

### 4.1 目的
AIと人間が共に参照できる詳細仕様を作成し、実装前に合意を得ます。

### 4.2 成果物

#### SPECIFICATION.md（必須）
**配置場所**: `composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}/SPECIFICATION.md`
**内容**（3セクション統合仕様書）:
1. **ユーザーストーリー**: ユーザー操作と期待する動作を箇条書き
2. **ビジネスルール**: 機能要件、制約条件を箇条書き
3. **画面内状態遷移**: Mermaid図で画面内部の詳細な振る舞いを表現

#### テストファイル骨格（必須）
- **ViewModelTest.kt**: 空のテストクラス・メソッド（TODOコメント付き）
- **UseCaseTest.kt**: 複雑なビジネスルールがある場合のみ

**重要**: 詳細な実装仕様（State/Intent定義、DI設定など）はPhase 2でAIが推論します。SPECIFICATION.mdはあくまで「何を作るか」を明確にする統合仕様書です。

### 4.3 実行方法

```bash
/phase1
```

**詳細**: `/phase1` コマンドを参照してください。

**プロセス概要**:
1. SPECIFICATION.md作成（3セクション統合仕様書）
2. ViewModelTest.kt作成（空のテスト）
3. UseCaseTest.kt作成（任意）
4. レビュー依頼 → 合意
5. GitHub Issue作成

**次のステップ**: Phase 2へ

---

## 5. Phase 2: AIによる実装

### 5.1 目的
SPECIFICATION.mdに基づき、AIが実装を実行します。

### 5.2 実装順序
**推奨順序**: Shared → ComposeApp → Integration

1. **Shared Layer** (Domain/Data)
2. **ComposeApp Layer** (UI)
3. **Integration** (DI setup)

### 5.3 実行方法

```bash
/phase2
```

**詳細**: `/phase2` コマンドを参照してください。

**プロセス概要**:
1. Shared Domain層の実装
2. Shared Data層の実装
3. ComposeApp ViewModel/UI実装
4. テスト実装（kotlin.test）
5. DI設定（Koin）
6. ビルド・テスト検証

**AI実行方法**:
- Serena Skill: `/serena "Implement based on SPECIFICATION.md..."`
- 直接プロンプト: `/phase2` コマンドに詳細なテンプレートあり

**次のステップ**: Phase 3へ

---

## 6. Phase 3: 実装レビュー

### 6.1 目的
実装品質を担保し、仕様との適合性を確認します。

### 6.2 レビュー観点
1. **仕様適合性**: SPECIFICATION.mdとの一致確認
2. **アーキテクチャ準拠**: ADR-001〜005準拠確認
3. **テストカバレッジ**: Domain層、ViewModel層のテスト確認
4. **コード品質**: Kotlinコーディング規約準拠
5. **ビルド・テスト**: `./gradlew clean build` と `./gradlew test` の成功

### 6.3 成果物
- **PR作成**: `feature/issue-{number}-{sanitized-title}` ブランチから
- **Issue Context作成**: `docs/context/{issue-number}/context.md`

### 6.4 実行方法

```bash
/phase3
```

**詳細**: `/phase3` コマンドを参照してください。

**プロセス概要**:
1. 仕様適合性レビュー
2. アーキテクチャ準拠レビュー
3. テストカバレッジレビュー
4. コード品質レビュー
5. ビルド・テスト最終検証
6. PR作成
7. Issue Context作成

**次のステップ**: PRマージ、Issue Close

---

## 7. ツールとテンプレート

### 7.1 テンプレート参照

| テンプレート | 用途 | Phase |
|-------------|------|-------|
| `docs/design-doc/template/epic-template.md` | Epic定義書 | Phase 0 |
| `docs/design-doc/template/specification-template.md` | SPECIFICATION.md | Phase 1 |
| `docs/design-doc/template/design-doc-template.md` | Design Doc（補完） | Phase 0-1 |
| `docs/context/templates/issue-context-template.md` | Issue Context | Phase 3 |

### 7.2 カスタムコマンド

| コマンド | 説明 | 詳細行数 |
|---------|------|---------|
| `/phase0` | Epic定義 & 共通盤の切り出し | 150-180行 |
| `/phase1` | 仕様・インターフェース定義 | 200-250行 |
| `/phase2` | AIによる実装 | 250-300行 |
| `/phase3` | 実装レビュー | 180-220行 |

**場所**: `.claude/commands/phase0.md` 〜 `phase3.md`

### 7.3 ドキュメント使い分け

| ドキュメント | 目的 | タイミング |
|-------------|------|-----------|
| **Epic定義書** | Epic分割と共通ドメイン定義 | Phase 0（大規模機能のみ） |
| **SPECIFICATION.md** | 統合仕様書（AI実装のSSoT） | Phase 1（必須） |
| **Design Doc** | 高レベル設計書（複数機能の統合設計） | Phase 0-1（オプション） |
| **Issue Context** | 実装記録 | Phase 3（必須） |

---

## 8. ベストプラクティス

### 8.1 Phase 0のタイミング

**適用すべき場合**:
- 3 Story以上の大規模機能
- 複数モジュールにまたがる機能
- 共通ドメインモデルが必要な場合

**スキップすべき場合**:
- 1-2 Storyの小規模機能
- 単一モジュール内で完結する機能

### 8.2 Phase 1のポイント

**シンプルな仕様書を作成**:
- SPECIFICATION.mdは3セクション統合仕様書（ユーザーストーリー、ビジネスルール、画面内状態遷移）
- 詳細な実装仕様はPhase 2でAIが推論
- 画面内状態遷移図（Mermaid）は必須

**テストで仕様を定義**:
- ViewModelTest.ktは必須（空のテストクラス）
- @DisplayNameで日本語で仕様を記述
- @Nestedで階層的に構造化

**既存パターンを参照**:
- `feature/video_playback/`: MVIパターンの参考実装
- ADR-002: MVIパターンの公式定義

### 8.3 Phase 2のポイント

**SPECIFICATIONとテストから仕様を読み取る**:
- SPECIFICATION.md（ユーザーストーリー、ビジネスルール、画面内状態遷移）
- ViewModelTest.kt（空のテストから期待する振る舞いを推論）

**既存コードスタイルに準拠**:
- 既存のViewModel実装を参照（例: `feature/video_playback/`）
- ADR-002のMVIパターンに準拠

### 8.4 Phase 3のポイント

**仕様との乖離を早期発見**:
- Phase 1のSPECIFICATIONと照らし合わせ
- 受け入れ条件が全て満たされているか確認

**ドキュメント更新**:
- Issue Contextを作成
- 実装中に発見した制約や注意点を記録

---

## 補足: 既存ドキュメントとの関係

### Design Docとの関係
- **Design Doc**: 高レベル設計書（全体アーキテクチャ、複数機能の統合設計）
- **SPECIFICATION**: 統合仕様書（Phase 1のSSoT、AI実装の直接的な指針）
- **使い分け**: Epic規模の大規模機能はDesign Doc、Story規模はSPECIFICATION必須

### Issue Contextとの関係
- **Issue Context**: Phase 3（実装後）の記録
- **SPECIFICATION**: Phase 1（実装前）の仕様
- **統合**: Issue ContextからSPECIFICATIONを参照、実装記録として保存

### ADRとの整合性
- ADRは `.claude/rules/architecture/` で管理
- SPECIFICATION内に「参照ADR」セクションを必ず記載
- ADR-002（MVIパターン）への準拠を明記
- 新規ADR作成が必要な場合は、Phase 1レビュー時に提案

---

**作成日**: 2025-12-30
**最終更新**: 2025-12-30
**管理**: CollabStream開発チーム
