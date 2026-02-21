---
name: review-ui
description: "Compose Multiplatform UIのデザインレビュー。スクリーンショットとコードからM3準拠・プロジェクト規約の観点でフィードバックを提供します。"
allowed-tools: Bash(./scripts/*), Bash(bash:*), Bash(mkdir:*), Bash(ls:*), Read, Glob, Grep
---

# UI Design Review

Compose Multiplatform で実装されたUIを、スクリーンショット + コードの両面からレビューします。

## 使用方法

```
/review-ui
```

引数なしで実行すると、現在ブランチで変更されたUIファイルを自動検出してレビューします。

## 実行フロー

```
1. 変更UIファイル検出
2. @Preview 不足チェック → /generate-previews 提案
3. スクリーンショット取得（screenshots/review/）
4. スクリーンショット確認 + コードレビュー
5. フィードバックレポート出力
```

## Step 1: 変更UIファイルの検出

現在ブランチで変更されたUIファイルを検出します。

```bash
git diff --name-only origin/main...HEAD -- 'composeApp/src/commonMain/kotlin/**/ui/**/*.kt' || true
```

変更ファイルがない場合はレビュー対象なしとして終了します。

## Step 2: @Preview 不足チェック

変更された各UIファイルについて:

1. Screen / Content / Component の Composable 関数を抽出
2. 対応する `@Preview` 関数が存在するか確認
3. 不足がある場合、ユーザーに `/generate-previews` の実行を提案

**Container は除外**（ViewModel依存のため Preview 不要）

## Step 3: スクリーンショット取得

### 3.1 出力ディレクトリ準備

```bash
mkdir -p screenshots/review
```

`screenshots/review/` は `.gitignore` で管理外とする。

### 3.2 Roborazzi でスクリーンショット記録

```bash
./gradlew :composeApp:recordRoborazziDebug --quiet
```

### 3.3 変更対象のスクリーンショットを抽出

変更されたファイルに対応するスクリーンショットのみを `screenshots/review/` にコピーします。

対応ルール:
- ファイル名パターンでマッチング（例: `TimelineSyncScreen.kt` → `*TimelineSyncScreen*Preview*.png`）
- マッチしない場合は全スクリーンショットをコピー

## Step 4: デザインレビュー

スクリーンショット（Read ツールで画像確認）とコードの両面から、以下の観点でレビューします。

### 4.1 Material Design 3 準拠

| チェック項目 | 詳細 |
|------------|------|
| カラーシステム | `MaterialTheme.colorScheme.*` のみ使用。ハードコード色禁止 |
| タイポグラフィ | `MaterialTheme.typography.*` の使用。カスタムフォントサイズ最小化 |
| コンポーネント | M3標準コンポーネント優先（`Button`, `Card`, `TopAppBar` 等） |
| スペーシング | 一貫した余白体系（4dp/8dp/16dp グリッド） |
| エレベーション | `tonalElevation` / `shadowElevation` の適切な使用 |
| Shape | `MaterialTheme.shapes.*` の使用 |

### 4.2 プロジェクト規約準拠

| チェック項目 | 参照ルール |
|------------|-----------|
| 4層構造 | ADR-003: Route → Screen → Content → Component |
| カラー使用 | `.claude/rules/compose/color-usage.md` |
| アクセシビリティ | `.claude/rules/compose/accessibility.md`（contentDescription 必須） |
| パフォーマンス | `.claude/rules/compose/performance.md`（remember, derivedStateOf） |
| Preview | `.claude/rules/compose/preview-guidelines.md`（全状態カバー） |

### 4.3 UI/UX 品質

| チェック項目 | 詳細 |
|------------|------|
| 状態カバレッジ | Loading / Empty / Error / Content が全て実装されているか |
| 視覚的階層 | 情報の優先度が視覚的に表現されているか |
| タッチターゲット | 最小 48dp を確保しているか |
| レイアウト一貫性 | 画面間で統一されたレイアウトパターンか |
| ダークモード | colorScheme 経由で自動対応しているか |

### 4.4 SPECIFICATION.md との整合

対象機能の `SPECIFICATION.md` が存在する場合:
- ユーザーストーリーに記述された操作が UI 上で可能か
- 状態遷移図の全状態が UI で表現されているか
- ビジネスルールが UI に反映されているか

## Step 5: フィードバックレポート

以下の形式でレポートを出力します。

```markdown
## UI Design Review Report

### 対象ファイル
- `path/to/Screen.kt`
- `path/to/Content.kt`

### スクリーンショット確認
（各スクリーンショットに対するコメント）

### M3 準拠
- ✅ カラーシステム: colorScheme 経由で使用
- ⚠️ スペーシング: 一部で非標準余白（12dp）を使用
- ❌ タイポグラフィ: カスタムフォントサイズ直指定あり

### プロジェクト規約
- ✅ 4層構造: Screen → Content → Component の分離OK
- ✅ アクセシビリティ: contentDescription 設定済み
- ⚠️ Preview: Error 状態の Preview が不足

### UI/UX 品質
- ✅ 状態カバレッジ: 4状態すべて実装
- ⚠️ タッチターゲット: IconButton のサイズ確認推奨

### SPECIFICATION.md 整合
- ✅ 全ユーザーストーリーの操作が UI 上で可能
- ⚠️ 状態遷移「エラー → リトライ」の UI パスが不明確

### 総合判定
🟢 LGTM / 🟡 軽微な修正推奨 / 🔴 要修正

### 改善提案（優先度順）
1. ...
2. ...
```

## 出力ディレクトリ

```
screenshots/
├── screens/          # git管理対象（既存）
├── components/       # .gitignore対象（既存）
└── review/           # .gitignore対象（レビュー用、一時的）
```

## /develop との連携

`/develop` Step 4（実装）の ComposeApp エージェント完了後に使用:

```
compose-multiplatform-specialist 完了
  → /generate-previews（不足Preview生成）
  → /review-ui（本スキル）
  → フィードバック反映
```

## 注意事項

- スクリーンショットは Debug ビルドで生成されます
- `screenshots/review/` はレビュー完了後に削除して構いません
- 初回実行時はビルドに時間がかかる場合があります
