# ドキュメント規約

## ディレクトリ構成

```
docs/
├── architecture/        # システムアーキテクチャ
├── design-doc/          # 機能設計ドキュメント
├── navigation/          # モジュールレベルナビゲーション図（Level 2）
├── context/             # Issue 固有の実装コンテキスト
└── guides/              # 開発ガイド

.claude/rules/
└── architecture/        # ADR（Architecture Decision Records）
```

## ナビゲーションドキュメントの3レベル階層

| Level | 場所 | 内容 |
|-------|------|------|
| 1 | `docs/screen-navigation.md` | アプリ全体の画面概要 |
| 2 | `docs/navigation/{module}-module.md` | モジュール内の画面遷移 |
| 3 | `feature/{feature}/screen-transition.md` | 画面内部の詳細な振る舞い |

## 新機能追加時の作成ドキュメント

### Phase 1（仕様定義時）

1. **機能仕様**: `feature/{feature_name}/REQUIREMENTS.md`
2. **画面遷移**: `feature/{feature_name}/screen-transition.md`
3. **（新モジュール時）**: `docs/navigation/{module}-module.md`
4. **アプリ概要更新**: `docs/screen-navigation.md`

### テンプレート

| 用途 | テンプレート |
|------|-------------|
| 機能仕様 | `docs/design-doc/template/requirements-template.md` |
| 画面遷移 | `docs/design-doc/template/screen-transition-template.md` |
| モジュールナビゲーション | `docs/design-doc/template/module-navigation-template.md` |
| Epic | `docs/design-doc/template/epic-template.md` |

## ADR（Architecture Decision Records）

- 保管場所: `.claude/rules/architecture/`
- 形式: `NNN-kebab-case-title.md`
- 構造: Status, Context, Decision, Consequences

## 言語

**すべてのドキュメントは日本語で作成すること。**
