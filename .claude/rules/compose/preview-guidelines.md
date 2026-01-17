---
paths: composeApp/**/*.kt
---

# Preview 必須ガイドライン

すべての Screen / Content / Component には Preview を必ず作成すること。

## 必須レベル

| 層 | Preview 必須 | 理由 |
|---|---|---|
| Container | 不要 | ViewModel に依存 |
| Screen | 必須 | 全体レイアウト確認 |
| Content | 必須 | 機能単位 UI 確認 |
| Component | 必須 | 再利用部品確認 |

## 必須要件

1. **AppTheme でラップ**: `MaterialTheme` 直接使用は禁止
2. **private 修飾子**: 外部公開しない
3. **複数状態対応**: Loading / Empty / Error / Content
4. **命名規則**: `*Preview()` / `*LoadingPreview()` / `*ErrorPreview()`
5. **固定値使用**: 時刻等は `Instant.parse("2024-01-01T12:00:00Z")` など

## コード例

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

@Preview
@Composable
private fun VideoSyncScreenLoadingPreview() {
    AppTheme {
        VideoSyncScreen(
            uiState = VideoSyncUiState(isLoading = true),
            onIntent = {}
        )
    }
}
```

## 禁止事項

- Container に Preview を追加（ViewModel 依存のため）
- `MaterialTheme { }` 直接使用（`AppTheme` を使う）
- Preview のない Screen/Content/Component の作成
