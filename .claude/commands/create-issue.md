---
allowed-tools: Read, Write, Edit, TodoWrite, Task, Bash(gh:*), Bash(mkdir:*), Bash(date:*)
description: Design doc作成から GitHub issue作成までのSerena統合ワークフロー
---

# Design Doc & Issue Creation Workflow

このコマンドは、Serenaを活用した機能の設計分析からGitHub issue作成まで効率的なワークフローを提供します。

## 実行開始

まず、GitHubリポジトリの確認とテンプレートの存在確認を行います：

- GitHub認証状態: !`gh auth status 2>/dev/null | head -3 || echo "GitHub CLI認証が必要です"`
- リポジトリ確認: !`gh repo view --json name,owner | jq -r '"\(.owner.login)/\(.name)"' 2>/dev/null || echo "GitHubリポジトリが見つかりません"`
- Design docテンプレート: !`ls -la docs/design-doc/template/design-doc-template.md 2>/dev/null || echo "テンプレートが見つかりません"`

## ワークフロー概要

1. **機能概要の把握** - 実装したい機能の基本情報を整理
2. **設計アプローチ選択** - 複数の実装方針から最適なアプローチを選択
3. **Serena分析** - 選択した方針を元にコードベース分析と設計詳細化
4. **Design doc生成** - 分析結果を元に包括的ドキュメント作成
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

## Phase 1: 機能概要の把握

実装したい機能の基本情報を提供してください：

### 必要情報
- **機能名**: 実装したい機能の名前（例: "ユーザー認証機能", "リアルタイムチャット"）
- **機能概要**: どのような機能で、何を実現したいかの簡潔な説明（1〜2行）
- **分類**: `feature`（新機能追加）または `maintenance`（既存機能改善）
- **背景・課題**: なぜこの機能が必要なのか、解決したい課題

## Phase 2: 設計アプローチ選択

機能の性質と要件に応じて、以下から最適な設計アプローチを選択してください：

### A. Minimal MVP
**最小限の機能実装**
- 核となる機能のみを実装
- 最短経路での価値提供を重視
- 後の拡張性は考慮せず、シンプルな構造
- **適用例**: プロトタイプ、概念実証、緊急対応

### B. Standard Feature
**標準的な機能実装**
- バランスの取れた機能性と保守性
- 一般的なデザインパターンとベストプラクティスを採用
- 適度な拡張性と再利用性を考慮
- **適用例**: 一般的な業務機能、CRUD操作、標準的なUI

### C. Advanced Integration
**高度な統合機能実装**
- 既存システムとの深い統合
- 複雑なビジネスロジックと多システム連携
- 高い拡張性とカスタマイズ性
- **適用例**: ワークフロー、認証システム、データ統合

### D. Platform Optimized
**プラットフォーム最適化実装**
- 各プラットフォームの特性を活かした個別最適化
- ネイティブAPIとフレームワークの積極活用
- パフォーマンスとユーザー体験を最優先
- **適用例**: メディア処理、ハードウェア連携、高性能UI

### E. Experimental
**実験的アプローチ実装**
- 新技術やアーキテクチャの検証
- 革新的なソリューションの試行
- 学習とイテレーションを前提とした柔軟な設計
- **適用例**: 新技術検証、アーキテクチャ試行、研究開発

**選択の指針**:
- 開発速度重視 → A (Minimal MVP)
- バランス重視 → B (Standard Feature)
- 機能性重視 → C (Advanced Integration)
- 性能重視 → D (Platform Optimized)
- 革新性重視 → E (Experimental)

## Phase 3: Serena分析による設計詳細化

選択した設計アプローチを元に、Serenaを使用してコードベース分析と設計を詳細化します：

### 実行するSerena分析

#### 3.1 コードベース理解
```bash
# 関連する既存コードの分析
/serena "[機能名]に関連する既存実装を分析" -c -v
```

#### 3.2 アーキテクチャ設計
```bash
# 選択したアプローチでの設計分析
/serena "[選択したアプローチ]で[機能名]を設計" -d -r -v
```

#### 3.3 実装方針策定
```bash
# 具体的な実装戦略の策定
/serena "[機能名]の実装戦略を策定" -s -t -c
```

### 分析観点

**既存コード分析**:
- 類似機能の実装パターン調査
- 使用されているライブラリとフレームワーク
- アーキテクチャの一貫性確認

**設計最適化**:
- 選択したアプローチに適した設計パターン
- プラットフォーム固有の考慮事項
- 依存関係とモジュール構成

**実装計画**:
- 開発フェーズの分割
- 優先度とリスク評価
- テスト戦略

**品質保証**:
- エラーハンドリング方針
- パフォーマンス考慮事項
- セキュリティ要件

## Phase 4: Design Doc生成

Serena分析結果を元に包括的なDesign Docを生成します：

### 生成プロセス

1. テンプレートを読み込み
2. 基本情報と選択したアプローチを適用
3. **Serena分析結果を統合**
4. `docs/design-doc/[機能名].md` として保存
5. implement-issue.mdでの実装フェーズへの引き継ぎ情報を記載

### Design Doc品質チェック

作成したDesign Docが以下を満たしているか確認：

- ✅ 選択した設計アプローチが明記されている
- ✅ Serena分析による技術的根拠が含まれている
- ✅ 実装戦略と開発フェーズが定義されている
- ✅ 既存コードとの整合性が確認されている
- ✅ リスクと制約事項が明確化されている
- ✅ 実装の成功指標が定義されている

## Phase 5: GitHub Issue作成

作成したDesign Docを元にGitHub Issueを作成します：

```bash
# Issue作成コマンド例
gh issue create \
  --title "[Feature/Maintenance] 機能名" \
  --body "$(cat docs/design-doc/[機能名].md)" \
  --label "feature" \
```

**ワークフロー状態**:
- `design-doc`: Design doc作成済み
- `serena-analyzed`: Serena分析完了
- `ready-for-implementation`: 実装準備完了

## 注意事項

- **情報集約**: 必要な基本情報をまとめて提供してください
- **アプローチ選択**: 機能の性質と要件に最適な設計アプローチを選択してください
- **Serena活用**: コードベース分析と設計詳細化にSerenaを積極的に活用します
- **設計根拠**: 技術的な選択には分析に基づく根拠を含めます
- **責務分離**: create-issueは設計フェーズのみ、実装はimplement-issue.mdで管理

### Serenaコマンド活用例

```bash
# YouTube再生位置取得機能の場合
/serena "YouTube埋め込み動画の再生位置取得機能を設計" -d -r -c

# 認証システム改善の場合
/serena "既存認証システムの改善方針を策定" -c -v --focus=security

# 新しいUI コンポーネントの場合
/serena "再利用可能なUIコンポーネントを設計" -s -t --focus=frontend
```

---

**使用方法**: `/create-issue` を実行すると、効率的なSerena統合ワークフローが開始されます。