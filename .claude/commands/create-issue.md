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
3. **Design doc生成** - テンプレートベースでドキュメント作成
4. **GitHub issue作成** - 作成したdesign docを元にissueを生成

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

**Q10. 成功を測定する指標はありますか？**
> 数値目標、KPI、メトリクスなど

### 技術アプローチ
**Q11. 技術的なアプローチの概要を教えてください**
> アーキテクチャ、使用技術、設計方針など

**Q12. API設計が必要ですか？その場合、主要なエンドポイントは？**

**Q13. データ保存・永続化は必要ですか？**

### リスク・制約
**Q14. 技術的な懸念点やリスクはありますか？**

**Q15. 対象外とする項目はありますか？**

**Q16. 代替案として検討した方法はありますか？**

## Phase 3: Design Doc生成

回答を元にdesign docを生成します：

1. テンプレートを読み込み
2. 回答内容をテンプレートに適用
3. `docs/design-doc/[機能名].md` として保存

## Phase 4: GitHub Issue作成

作成したdesign docを元にGitHub issueを作成します：

```bash
# Issue作成コマンド例
gh issue create \
  --title "[Feature/Maintenance] 機能名" \
  --body "$(cat docs/design-doc/[機能名].md)" \
  --label "feature" \
  --label "design-doc"
```

## 注意事項

- **一問一答形式**: 各質問に対して丁寧に答えてください
- **具体性重視**: 曖昧な回答よりも具体的な内容を心がけてください  
- **技術詳細**: 実装に必要な技術的詳細も含めてください
- **レビュー**: design doc作成後、内容を確認してから issue作成に進みます

---

**使用方法**: `/create-issue` を実行すると、Phase 1から順番に質問が開始されます。