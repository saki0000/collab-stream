# 開発ワークフロー

CollabStream では AI（Claude Code）を活用した仕様駆動開発（SDD）を採用。

## コマンド体制

| コマンド | 用途 | 対象 |
|---------|------|------|
| `/phase0` | Epic定義 & 共通基盤の切り出し | 大規模機能（3 US以上） |
| `/develop` | 仕様定義 → 実装 → PR作成 | 全機能（メインコマンド） |

## クイックスタート

### 大規模機能（3 US以上）

```
/phase0 → Epic定義 & US分割 & SPECIFICATION.md作成
    ↓
/develop → 各USの実装（US選択 → 設計 → 実装 → PR）
```

### 小規模機能

```
/develop → US新規作成 → SPECIFICATION.md作成 → 実装 → PR
```

## タスク管理

タスクは `implement-context/` 内のmarkdownファイルで管理。

```
implement-context/
├── {epic_name}/              # Epic単位
│   ├── EPIC.md               # Epic概要、US一覧、依存関係
│   └── us-{n}-{name}/
│       ├── US.md             # ユーザーストーリー概要
│       ├── DESIGN.md         # 設計メモ
│       └── PROGRESS.md       # タスク管理
└── {us_name}/                # 小規模（Epic不要）
    ├── US.md
    ├── DESIGN.md
    └── PROGRESS.md
```

## 仕様のSSOT

SPECIFICATION.md は機能仕様のSSOT（Single Source of Truth）。

**配置**: `composeApp/.../feature/{feature_name}/SPECIFICATION.md`

**構成**: 3セクション統合仕様書
1. ユーザーストーリー
2. ビジネスルール
3. 状態遷移（Mermaid図）

## 詳細ガイド

完全なワークフロー詳細: `docs/guides/development-workflow.md`
