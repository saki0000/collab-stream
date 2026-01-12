---
name: implement-plan
description: "実装開始時にplanファイルを解析し、適切なエージェントを自動選択して実装を開始します。"
allowed-tools: Read, Glob, Task, TodoWrite, Bash(ls:*), Bash(find:*)
---

# Implement Plan

Plan mode完了後に、planファイルの内容を解析して適切なエージェントを自動選択・起動し、実装を開始するスキルです。

## 使用方法

Plan mode完了後に実行：

```
/implement-plan
```

## 実行フロー

### Step 1: Planファイルの検索と読み込み

1. `~/.claude/plans/`ディレクトリから最新のplanファイルを検索
2. planファイルの内容を読み込み

### Step 2: キーワード解析とエージェント選択

planの内容から以下のキーワードをスキャンし、適切なエージェントを選択：

| キーワードパターン | 選択エージェント |
|------------------|-----------------|
| UI, Compose, Screen, Component, MVI, ViewModel, @Composable, Layout | `compose-multiplatform-specialist` |
| API, Server, Ktor, REST, Endpoint, Backend, routing, port 8080 | `kotlin-backend-specialist` |
| Domain, Entity, UseCase, Repository, Business, Clean Architecture | `domain-layer-architect` |
| Database, SQL, SQLDelight, Migration, Schema, Query, Room | `database-implementation-specialist` |
| Test, Testing, QA, Coverage, Unit, Integration, @Test | `test-qa-engineer` |
| Library, Dependency, Version, Gradle, libs.versions.toml | `library-integration-specialist` |
| Deploy, CI/CD, Docker, GitHub Actions, Kubernetes | `deployment-engineer` |

### Step 3: 複数レイヤー判定

以下のパターンで複数レイヤーにまたがるタスクを検出：

- `composeApp/` + `shared/` → 複合タスク
- `server/` + `shared/` → 複合タスク
- 3つ以上のレイヤーに言及 → 複合タスク

**複合タスクの場合**: `task-breakdown-specialist`を起動してレイヤー別に分割

### Step 4: エージェント起動

```
選択したエージェントにTaskを発行:
- planファイルの内容を引き渡し
- 実装指示を明確に伝達
```

## エージェント選択の優先度

1. **単一レイヤー明確**: 該当エージェントを直接起動
2. **複数レイヤー**: task-breakdown-specialistで分割
3. **不明確**: ユーザーに確認を求める

## 選択ロジックの詳細

### compose-multiplatform-specialist を選択する条件
- `composeApp/`パスへの言及
- UI/画面関連のキーワード
- MVI/ViewModel/UiState等のアーキテクチャキーワード

### kotlin-backend-specialist を選択する条件
- `server/`パスへの言及
- API/Ktor/REST等のキーワード
- Endpoint/routing等のサーバー関連用語

### domain-layer-architect を選択する条件
- `shared/src/commonMain/kotlin/.../domain/`への言及
- Entity/UseCase/Repository interface等の用語
- ビジネスロジック/ドメインルール等の言及

### database-implementation-specialist を選択する条件
- Database/SQL関連の言及
- SQLDelight/Room等のライブラリ名
- Migration/Schema等の用語

### test-qa-engineer を選択する条件
- テスト作成が主目的
- TDD/テストカバレッジの言及
- `*Test.kt`ファイルの作成指示

### library-integration-specialist を選択する条件
- `libs.versions.toml`の変更
- 新しいライブラリの追加
- 依存関係の更新

### deployment-engineer を選択する条件
- CI/CD設定
- Docker/Kubernetes関連
- GitHub Actions設定

## 出力例

```
📋 Planファイルを解析中...

🔍 検出されたキーワード:
   - composeApp/: UI実装
   - Screen, ViewModel: MVI アーキテクチャ
   - shared/domain: ドメイン層

📊 分析結果:
   - 複数レイヤー（UI + Domain）にまたがるタスクを検出

🤖 選択エージェント: task-breakdown-specialist
   理由: 複数レイヤーにまたがるため、レイヤー別に分割して実装

🚀 エージェントを起動します...
```

## 注意事項

- Planファイルが存在しない場合はエラーメッセージを表示
- エージェント選択に迷う場合はユーザーに確認
- 実装開始前にworktreeの作成を推奨（別ブランチでの作業）

## 関連スキル

- `create-worktree`: 実装前にworktreeを作成
- `cleanup-worktree`: worktree削除
- `commit`: 変更のコミット
- `/pr`: PRの作成
