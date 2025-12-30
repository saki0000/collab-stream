---
allowed-tools: Read, Write, Edit, MultiEdit, Glob, Grep, Bash(git:*), Bash(./gradlew:*), Bash(mkdir:*), TodoWrite, mcp__serena__*, LSP
description: AIによる実装 - REQUIREMENTS.mdに基づく実装実行
---

# Phase 2: AIによる実装

Phase 2では、REQUIREMENTS.mdとテスト仕様に基づき、AIが完全な実装を実行します。

## 🎯 実行方法

```bash
/phase2
```

## Context

### REQUIREMENTS.md & Tests確認
- REQUIREMENTS location: !`find composeApp/src/commonMain/kotlin -name "REQUIREMENTS.md" | head -5`
- ViewModel tests: !`find composeApp/src/commonTest/kotlin -name "*ViewModelTest.kt" | head -5`
- UseCase tests: !`find shared/src/commonTest/kotlin -name "*UseCaseTest.kt" | head -5`

### Build & Test Status
- Last build: !`./gradlew :shared:build :composeApp:build --dry-run 2>&1 | head -3 || echo "Build check"`
- Test status: !`./gradlew test --dry-run 2>&1 | head -3 || echo "Test check"`

### Git状態
- Current branch: !`git branch --show-current`
- Uncommitted: !`git status --porcelain | wc -l | xargs echo "files"`

---

## Overview

**Phase 2の目的**: REQUIREMENTS.mdとテストから完全実装

実装順序：
1. **Shared Layer** → Domain/Data実装
2. **ComposeApp Layer** → ViewModel/UI実装
3. **Integration** → DI設定、テスト

**重要な原則**:
- REQUIREMENTS.mdを絶対的な仕様書として扱う
- テスト（ViewModelTest/UseCaseTest）から期待する振る舞いを読み取る
- 既存パターン（ADR、類似機能）に準拠
- 進捗はREQUIREMENTS.md Section 4で管理

---

## Phase 2 Process

### Step 1: REQUIREMENTS.md & Tests確認

#### 1.1 REQUIREMENTS.md参照

**確認すべきセクション**:
- Section 1: ユーザーストーリー → Intent/UI動作に反映
- Section 2: ビジネスルール → UseCase/Repository実装に反映
- Section 3: 画面状態遷移 → UiState設計に反映
- **Section 4: Phase 2実装進捗** → 実装タスクのチェックリスト

#### 1.2 Test仕様確認

**ViewModelTest.kt**:
- @DisplayNameから期待する振る舞いを読み取る
- @Nested構造から状態遷移パターンを理解
- TODOコメントを完全実装に置き換え

**UseCaseTest.kt**（該当する場合）:
- ビジネスルールの詳細をテストから読み取る

---

### Step 2: AI実行

#### Method 1: Serena Skill（推奨）

```bash
/serena "Implement feature based on REQUIREMENTS.md at composeApp/feature/{feature_name}/REQUIREMENTS.md"
```

**SerenaがREQUIREMENTS.mdを読んで自動実装**します。

#### Method 2: Direct Prompt（詳細制御が必要な場合）

```markdown
以下のREQUIREMENTSに基づいて、機能を実装してください：

**仕様書**: `composeApp/feature/{feature_name}/REQUIREMENTS.md`
**テスト仕様**:
- ViewModelTest: `composeApp/src/commonTest/.../feature/{feature_name}/{Feature}ViewModelTest.kt`
- UseCaseTest (if applicable): `shared/src/commonTest/.../domain/usecase/{UseCase}Test.kt`

**参照パターン**: `feature/video_playback/` (類似実装)
**準拠ADR**: ADR-001 (Clean Architecture), ADR-002 (MVI Pattern), ADR-003 (4層Component)

**実装順序**:
1. Shared Domain層 (Models, Repository Interface, UseCase, Tests)
2. Shared Data層 (Repository実装, Data Source, Mappers)
3. ComposeApp ViewModel層 (UiState, Intent, ViewModel, Tests)
4. ComposeApp UI層 (Container, Screen, Content, Components - 4層構造)
5. DI設定 (Shared module, ComposeApp module)
6. Build & Test確認

**制約**:
- ViewModelは `composeApp/feature/` 配下
- Domain/Dataは `shared/` 配下
- kotlin.testを使用 (@DisplayName, @Nested)
- ComposeApp → Shared依存のみ（逆向き禁止）

**進捗管理**:
- 各タスク完了時にREQUIREMENTS.md Section 4のチェックボックスを更新してください
```

---

### Step 3: 実装順序（AIが自動判断）

**推奨順序**: Shared → ComposeApp → Integration

AIは以下を自動的に判断・実装します：
- Domain Models設計
- Repository Interface定義
- UseCase実装
- ViewModel実装（MVI pattern）
- UiState/Intent設計
- UI Components実装（4層構造）
- DI設定（Koin）
- Test実装（空テストを完全実装）

**詳細はAIに任せる** - アーキテクチャパターンは既知のため、指示不要

---

### Step 4: Build & Test

#### 4.1 Layer-by-Layer Build

```bash
# Shared layer build
./gradlew :shared:build

# Shared tests
./gradlew :shared:test

# ComposeApp build
./gradlew :composeApp:build

# ComposeApp tests
./gradlew :composeApp:test

# All tests
./gradlew test
```

#### 4.2 Platform-Specific Builds（任意）

```bash
# Android
./gradlew :composeApp:assembleDebug

# Web (WASM)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun --continuous

# Server
./gradlew :server:run
```

---

### Step 5: Progress Update

**REQUIREMENTS.md Section 4を更新**:

実装完了時に各チェックボックスを更新：

```markdown
### Shared Layer
- [x] Domain Models実装
- [x] Repository Interface実装
- [x] UseCase実装
- [x] Domain Tests実装
- [x] Build成功（`./gradlew :shared:build`）

### ComposeApp Layer
- [x] ViewModel実装（MVI pattern）
- [x] UI Components実装（4層構造）
- [x] ViewModel Tests実装
- [x] DI設定（Koin）
- [x] Build成功（`./gradlew :composeApp:build`）
- [x] 全テスト成功（`./gradlew test`）
- [x] Phase 3レビュー準備完了
```

---

## Success Criteria

Phase 2完了の条件：

- [ ] **Shared Domain Layer完了**
  - [ ] Domain Models実装
  - [ ] Repository Interfaces実装
  - [ ] UseCases実装
  - [ ] Domain tests passing

- [ ] **Shared Data Layer完了**
  - [ ] Repository実装
  - [ ] Data Source実装
  - [ ] Data Models & Mappers実装

- [ ] **ComposeApp ViewModel Layer完了**
  - [ ] UiState実装
  - [ ] Intent実装
  - [ ] ViewModel実装（MVI pattern）
  - [ ] ViewModel tests passing

- [ ] **ComposeApp UI Layer完了**
  - [ ] Container実装
  - [ ] Screen実装
  - [ ] State-specific Content実装
  - [ ] Components実装（4層構造）

- [ ] **DI設定完了**
  - [ ] Shared module（Repository, UseCase）
  - [ ] ComposeApp module（ViewModel）
  - [ ] 全テスト成功（`./gradlew test`）

- [ ] **Build成功**
  - [ ] `./gradlew :shared:build` 成功
  - [ ] `./gradlew :composeApp:build` 成功

- [ ] **進捗管理更新**
  - [ ] REQUIREMENTS.md Section 4全チェック完了

---

## Next Steps

Phase 2完了後：

1. **コードレビュー準備** → コミット、PR作成準備
2. **Phase 3実装レビュー** → `/phase3` コマンド実行
3. **仕様適合性確認** → REQUIREMENTS.mdとの一致確認

**Phase 3で実施すること**:
- 仕様適合性レビュー
- アーキテクチャ準拠確認
- テストカバレッジ確認
- コード品質確認
- PR作成
- Issue Context作成

---

## Notes

### よくある質問

**Q1: エラーハンドリングはどこで行う？**
A: Repository層で `runCatching` + `Result<T>`、ViewModel層でError状態に変換します。

**Q2: テストのMockはどう作る？**
A: `shared/src/commonTest/kotlin` にMock/Fakeを配置。`MockK`ライブラリ使用可能。

**Q3: ViewModelのcoroutineテストは？**
A: `kotlinx-coroutines-test`の`runTest`と`advanceUntilIdle()`を使用します。

**Q4: Koin DIが解決できない場合は？**
A: Module登録順序を確認。Shared modules → ComposeApp modulesの順。

**Q5: REQUIREMENTS.md Section 4の更新タイミングは？**
A: 各タスク完了時に随時更新します。Phase 3開始前に全チェック完了を確認。

### トラブルシューティング

**問題: テストが失敗する**
→ REQUIREMENTS.mdとテスト仕様の不整合を確認。Mock/Fakeの挙動を確認。

**問題: ビルドエラー**
→ 依存関係の方向を確認（ComposeApp → Shared のみ）。import文を確認。

**問題: ViewModelのStateFlow更新が反映されない**
→ `viewModelScope.launch`で非同期処理。`advanceUntilIdle()`でテスト。

**問題: DI解決失敗**
→ Module登録を確認。Koin DSLの構文確認（`single`, `factory`, `viewModel`）。

---

**Phase 2完了後、次は `/phase3` コマンドで実装レビューを開始してください。**