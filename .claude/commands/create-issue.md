---
allowed-tools: Read, Write, Edit, TodoWrite, Bash(gh:*), Bash(mkdir:*), Bash(date:*)
description: Design doc作成から GitHub issue作成までの一問一答ワークフロー
---

# Design Doc & Issue Creation Workflow

このコマンドは、機能の要件策定からGitHub issue作成まで一貫したワークフローを提供します。

## 実行開始

まず、GitHubリポジトリの確認とテンプレートの存在確認を行います：

- GitHub認証状態: !`gh auth status 2>/dev/null | head -3 || echo "GitHub CLI認証が必要です"`
- リポジトリ確認: !`gh repo view --json name,owner | jq -r '"\(.owner.login)/\(.name)"' 2>/dev/null || echo "GitHubリポジトリが見つかりません"`
- Design docテンプレート: !`ls -la docs/design-doc/template/design-doc-template.md 2>/dev/null || echo "テンプレートが見つかりません"`

## ワークフロー概要

1. **全体像の把握** - 実装したい機能の概要を整理
2. **一問一答による詳細化** - Design docに必要な情報を段階的に収集
3. **Interface設計** - レイヤー別Interface定義と責務明確化
4. **Design doc生成** - Interface設計を含む包括的ドキュメント作成
5. **GitHub issue作成** - 作成したDesign docを元にissueを生成

## Context

### プロジェクト構成
- **Kotlin Multiplatform**: Android, iOS, Web (WASM), Server対応
- **UI Framework**: Compose Multiplatform
- **Server**: Ktor (port 8080)
- **Design Doc保存先**: `docs/design-doc/`
- **テンプレート**: `docs/design-doc/template/design-doc-template.md`

### 現在のプロジェクト状態
- Current branch: !`git branch --show-current`
- Project structure: !`find . -name "*.kt" -type f | head -10`

## Phase 1: 全体像の把握

まず、実装したい機能について教えてください：

### 基本情報
**Q1. 機能名は何ですか？**
> 実装したい機能の名前を教えてください（例: "ユーザー認証機能", "リアルタイムチャット"）

**Q2. この機能の概要を1〜2行で説明してください**
> どのような機能で、何を実現したいかを簡潔に説明してください

**Q3. これはfeatureとmaintenanceどちらに分類されますか？**
> - `feature`: 新機能の追加
> - `maintenance`: 既存機能の改善・リファクタリング

## Phase 2: 要件詳細化

基本情報を元に、design docに必要な詳細情報を収集します：

### 実装背景・課題
**Q4. なぜこの機能が必要なのですか？解決したい課題を教えてください**

**Q5. 現在のシステムの問題点や制限は何ですか？**

### 技術仕様・影響範囲
**Q6. どのプラットフォームに影響しますか？**
> - Android
> - iOS  
> - Web (WASM)
> - Server
> - すべて

**Q7. 既存のどのシステム・コンポーネントに影響しますか？**

**Q8. 想定されるユーザーは誰ですか？**
> エンドユーザー、他の開発チーム、システムなど

### ゴール・成功指標
**Q9. この機能によってユーザーはどのような恩恵を受けますか？**

### 技術アプローチ
**Q11. 技術的なアプローチの概要を教えてください**
> アーキテクチャ、使用技術、設計方針など

**Q12. API設計が必要ですか？その場合、主要なエンドポイントは？**

**Q13. データ保存・永続化は必要ですか？**

### リスク・制約
**Q14. 技術的な懸念点やリスクはありますか？**

**Q15. 対象外とする項目はありますか？**

**Q16. 代替案として検討した方法はありますか？**

## Phase 3: Interface設計

> **必須フェーズ**: すべての機能実装においてInterface設計を行います。

回答内容を元に、レイヤー別のInterfaceを設計し、各責務を明確化します：

### Interface設計質問

**Q17. この機能で主に使用されるデータモデルは何ですか？**
> 例: User, Product, Orderなどのエンティティ

**Q18. どのレイヤーで主な処理が行われますか？**
> - Domain (shared): ビジネスロジック・データ操作
> - Presentation (composeApp): UI表示・ユーザー操作
> - Infrastructure (server): API・外部連携

**Q19. 外部システムやサービスとの連携がありますか？**
> 例: データベース、外部API、ファイルシステムなど

**Q20. ユーザーインターフェースでどのような操作が必要ですか？**
> 例: データ入力、一覧表示、検索、ソートなど

**Q21. バリデーションやエラーハンドリングの要件は？**
> 入力値検証、エラーメッセージ、例外処理など

### Interface設計結果

上記質問の回答を元に、以下の形式でInterface設計を作成します：

```typescript
// Domain Layer Interfaces
interface [Entity]Repository {
  // データ永続化・取得責務
}

interface [Feature]UseCase {
  // ビジネスロジック責務
}

// Infrastructure Layer Interfaces
interface [Feature]ApiController {
  // HTTP処理責動
}

interface [Feature]Validator {
  // 入力値検証責動
}
```

## Phase 4: Design Doc生成

回答とInterface設計を元に包括的なDesign Docを生成します：

1. テンプレートを読み込み
2. 回答内容をテンプレートに適用
3. **Interface設計を必須で含める**
4. `docs/design-doc/[機能名].md` として保存
5. implement-issue.mdでの実装フェーズへの引き継ぎ情報を記載

### Design Doc品質チェック

作成したDesign Docが以下を満たしているか確認：

- ✅ Interface設計セクションが存在する
- ✅ 各レイヤーのInterfaceが定義されている
- ✅ 責務マトリックスが作成されている
- ✅ 依存関係が明確化されている
- ✅ 実装指針が記載されている

## Phase 5: GitHub Issue作成

作成したDesign Docを元にGitHub Issueを作成します：

```bash
# Issue作成コマンド例
gh issue create \
  --title "[Feature/Maintenance] 機能名" \
  --body "$(cat docs/design-doc/[機能名].md)" \
  --label "feature" \
  --label "design-doc" \
  --label "interface-designed"
```

## 注意事項

- **一問一答形式**: 各質問に対して丁寧に答えてください
- **具体性重視**: 曖昧な回答よりも具体的な内容を心がけてください  
- **技術詳細**: 実装に必要な技術的詳細も含めてください
- **レビュー**: Design Doc作成後、Interface設計を含めた内容を確認してからIssue作成に進みます
- **責務分離**: create-issueは設計フェーズのみ、実装はimplement-issue.mdで管理

---

**使用方法**: `/create-issue` を実行すると、Phase 1から順番に質問が開始されます。