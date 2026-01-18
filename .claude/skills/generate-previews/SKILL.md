---
name: generate-previews
description: "UIファイルを解析し、不足している@Preview関数を自動生成します"
allowed-tools: Bash(git diff:*), Read, Glob, Grep, Edit
---

# Preview テスト自動生成

変更されたUIファイルを解析し、不足している@Preview関数を自動生成します。

## 使用方法

```
/generate-previews
```

## 実行内容

### Step 1: 変更されたUIファイルを検出

```bash
git diff --name-only origin/main...HEAD | grep -E 'composeApp/src/commonMain/kotlin/.*/ui/.*\.kt$'
```

### Step 2: 各ファイルを解析

対象ファイルごとに：
1. Screen / Content / Component の Composable 関数を抽出
2. 対応する @Preview 関数が存在するか確認
3. Container は除外（ViewModel依存のため）

### Step 3: 不足Previewを生成

以下のルールに従って生成：

| 要件 | 内容 |
|------|------|
| テーマ | `AppTheme` でラップ |
| 修飾子 | `private` 必須 |
| 状態バリエーション | Loading / Empty / Error / Content |
| 命名規則 | `*Preview()`, `*LoadingPreview()`, `*ErrorPreview()` |
| 時刻 | 固定値使用（`Instant.parse("2024-01-01T12:00:00Z")`） |

### 生成例

```kotlin
@Preview
@Composable
private fun VideoSyncScreenPreview() {
    AppTheme {
        VideoSyncScreen(
            uiState = VideoSyncUiState(
                isLoading = false,
                currentTime = Instant.parse("2024-01-01T12:00:00Z")
            ),
            onIntent = {}
        )
    }
}
```

## 対象ファイルパターン

- `composeApp/src/commonMain/kotlin/**/ui/*Screen.kt`
- `composeApp/src/commonMain/kotlin/**/ui/*Content.kt`
- `composeApp/src/commonMain/kotlin/**/ui/components/*.kt`

## 除外パターン

- `*Container.kt` - ViewModel依存のため
- 既に@Previewが存在するComposable

## 参照

- `.claude/rules/compose/preview-guidelines.md` - Previewガイドライン
