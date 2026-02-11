# AI×KMP 仕様駆動開発（SDD）運用ガイドライン

## 目次
1. [はじめに](#1-はじめに)
2. [基本原則](#2-基本原則)
3. [Phase 0: Epic定義 & 共通基盤の切り出し](#3-phase-0-epic定義--共通基盤の切り出し)
4. [/develop: 統合開発コマンド](#4-develop-統合開発コマンド)
5. [ツールとテンプレート](#5-ツールとテンプレート)
6. [ベストプラクティス](#6-ベストプラクティス)

---

## 1. はじめに

### 目的
CollabStreamプロジェクトにおけるAI（Claude Code）を活用した効率的な開発ワークフローを定義し、一貫性のある開発プロセスを提供します。

### 対象範囲
- **プラットフォーム**: Kotlin Multiplatform (Android/iOS/Web/Server)
- **UIフレームワーク**: Compose Multiplatform
- **アーキテクチャ**: Android Architecture（Domain層オプショナル）+ MVI Pattern
- **開発規模**: US単位〜Epic単位の機能開発

### コマンド体制

| コマンド | 用途 | 対象 |
|---------|------|------|
| `/phase0` | Epic定義 & 共通基盤の切り出し | 大規模機能（3 US以上） |
| `/develop` | 仕様定義 → 実装 → PR作成 | 全機能（メインコマンド） |

### 前提知識
- **CLAUDE.md**: プロジェクト構造とコマンド
- **ADR 001-005**: アーキテクチャ決定記録（`.claude/rules/architecture/`）

---

## 2. 基本原則

### 2.1 Vertical Slice Architecture
**機能ごとに垂直方向に実装**を行います。レイヤーごとではなく、「ユーザーができること」単位でタスクを切ります。

**実装単位**: `feature/{feature_name}/` ← 1つのVertical Slice
- `composeApp/.../feature/{feature_name}/`: UI層（ViewModel、UiState、Intent、UI Components）
- `shared/domain/`: ビジネスロジック層（Models、Repository Interfaces、UseCases）
- `shared/data/`: データアクセス層（Repository Implementations）

### 2.2 Behavior via ViewModel
アプリの**振る舞い（仕様）はViewModelのテスト**で定義します。

**テスト規約**:
- **フレームワーク**: kotlin.test（commonTest 配置）
- **テスト名**: バッククォートで日本語記述（`` `{コンテキスト}_{期待する振る舞い}`() ``）
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

### 2.3 タスク管理
タスクは `implement-context/` 内のmarkdownファイルで管理します。

```
implement-context/
├── {epic_name}/              # Epic単位
│   ├── EPIC.md               # Epic概要、US一覧、依存関係
│   └── us-{n}-{name}/
│       ├── US.md             # ユーザーストーリー概要
│       ├── DESIGN.md         # 設計メモ（/develop時に作成）
│       └── PROGRESS.md       # タスク管理（/develop時に作成）
└── {us_name}/                # 小規模（Epic不要）
    ├── US.md
    ├── DESIGN.md
    └── PROGRESS.md
```

---

## 3. Phase 0: Epic定義 & 共通基盤の切り出し

### 3.1 目的
**並行開発時の設計不整合を防ぐ**ため、大規模機能（Epic）を複数のUSに分割し、共通ドメインを事前に定義します。

### 3.2 適用タイミング
以下のいずれかに該当する場合、Phase 0から開始します：

- **3 US以上の大規模機能**
- **複数モジュールにまたがる機能**
- **共通ドメインモデルが必要な場合**

小規模機能の場合は、`/develop` から直接開始してください。

### 3.3 成果物
- **EPIC.md**: `implement-context/{epic_name}/EPIC.md`
- **US.md**: `implement-context/{epic_name}/us-{n}-{name}/US.md`（各USごと）
- **SPECIFICATION.md**: `composeApp/.../feature/{feature_name}/SPECIFICATION.md`（各USごと）
- **共通ドメイン実装**: Entity と Repository Interface（最小限）

### 3.4 実行方法

```bash
/phase0
```

**プロセス概要**:
1. EPIC.md 作成（Epic概要、US一覧）
2. 各US の US.md 作成
3. 各US の SPECIFICATION.md 作成
4. 共通ドメインエンティティ・Repository Interface 定義
5. PR作成とマージ

**次のステップ**: 各USで `/develop` を実行

---

## 4. /develop: 統合開発コマンド

### 4.1 目的
仕様定義から実装・テスト・PR作成までを一貫して実行します。

### 4.2 フロー

```
US選択（or 新規作成）+ Worktree作成
    ↓
SPECIFICATION.md 確認・作成
    ↓
DESIGN.md 作成（コードベース分析 → 実装方針）
    ↓
PROGRESS.md 作成（タスク分割）
    ↓
実装（Task エージェントに委譲）
    ↓
エージェント完了後の検証
    ↓
PR作成
```

### 4.3 実行方法

```bash
/develop
```

### 4.4 実装（エージェント委譲）

Step 4 では Task ツールを使用して実装をエージェントに委譲します。
DESIGN.md の変更対象レイヤーに応じて適切な subagent_type を自動選択します。

**エージェントに渡すコンテキスト**:
- SPECIFICATION.md（仕様）、DESIGN.md（設計）、PROGRESS.md（タスク一覧）
- プロジェクト規約の要約（ADR、テスト規約、DI、日時処理等）

**エージェントの実装順序**: Shared → ComposeApp → Integration

1. **Shared Layer** (Domain/Data)
   - Domain Models, Repository Interface, UseCase, Repository Implementation
   - テスト作成（kotlin.test）

2. **ComposeApp Layer** (ViewModel/UI)
   - UiState / Intent 定義
   - ViewModel 実装（MVI パターン - ADR-002）
   - UI Components 実装（4層構造 - ADR-003）
   - ViewModelTest 実装

3. **Integration**
   - DI 設定（Koin）
   - Navigation 設定
   - 全テスト実行

エージェント完了後、PROGRESS.md の全タスク完了を検証し、不足があれば追加 Task を発行します。

### 4.5 仕様のSSOT

SPECIFICATION.md は機能仕様のSSOT（Single Source of Truth）です。

**配置場所**: `composeApp/.../feature/{feature_name}/SPECIFICATION.md`

**構成**: 3セクション統合仕様書
1. **ユーザーストーリー**: ユーザー操作と期待する動作を箇条書き
2. **ビジネスルール**: 機能要件、制約条件をテーブル形式
3. **状態遷移**: Mermaid図で画面内部の状態遷移を表現

---

## 5. ツールとテンプレート

### 5.1 テンプレート参照

| テンプレート | 用途 |
|-------------|------|
| `docs/design-doc/template/epic-template-v2.md` | Epic定義（EPIC.md） |
| `docs/design-doc/template/us-template.md` | ユーザーストーリー（US.md） |
| `docs/design-doc/template/design-template.md` | 設計メモ（DESIGN.md） |
| `docs/design-doc/template/progress-template.md` | 進捗管理（PROGRESS.md） |
| `docs/design-doc/template/specification-template.md` | 機能仕様（SPECIFICATION.md） |

### 5.2 コマンド

| コマンド | 説明 |
|---------|------|
| `/phase0` | Epic定義 & 共通基盤の切り出し |
| `/develop` | 仕様定義 → 実装 → PR作成（統合コマンド） |
| `/commit` | コミット作成 |
| `/pr` | PR作成 |
| `/create-worktree` | Git worktree で独立環境を準備 |
| `/cleanup-worktree` | worktree 削除・クリーンアップ |

### 5.3 ドキュメント使い分け

| ドキュメント | 目的 | タイミング |
|-------------|------|-----------|
| **EPIC.md** | Epic分割と共通ドメイン定義 | Phase 0（大規模機能のみ） |
| **US.md** | ユーザーストーリー概要 | Phase 0 または /develop |
| **SPECIFICATION.md** | 統合仕様書（SSOT） | /develop（必須） |
| **DESIGN.md** | 設計メモ・実装方針 | /develop |
| **PROGRESS.md** | タスク管理・進捗 | /develop |

---

## 6. ベストプラクティス

### 6.1 Phase 0のタイミング

**適用すべき場合**:
- 3 US以上の大規模機能
- 複数モジュールにまたがる機能
- 共通ドメインモデルが必要な場合

**スキップすべき場合**:
- 小規模機能 → `/develop` から直接開始

### 6.2 SPECIFICATION.md のポイント

**シンプルな仕様書を作成**:
- 3セクション統合仕様書（ユーザーストーリー、ビジネスルール、状態遷移）
- 詳細な実装仕様はAIが推論
- 画面内状態遷移図（Mermaid）は必須
- 具体的なコード（疑似コード含む）は書かない

### 6.3 テストのポイント

- **フレームワーク**: kotlin.test を使用
- **配置**: `commonTest` ディレクトリ
- **テスト名**: バッククォートで日本語記述
- **グルーピング**: コメントセクション（`// ========`）
- **`commonTest` での制約**: `@DisplayName` や `@Nested` は使用不可（JUnit 5 固有）

### 6.4 既存パターンの参照

- `feature/video_playback/`: MVIパターンの参考実装
- ADR-001: Android Architecture
- ADR-002: MVIパターン
- ADR-003: 4層Component構造

---

**作成日**: 2025-12-30
**最終更新**: 2026-02-11
**管理**: CollabStream開発チーム
