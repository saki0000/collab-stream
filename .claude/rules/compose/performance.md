---
paths: composeApp/**/*.kt
---

# パフォーマンス最適化

## remember

高コスト計算は `remember` でキャッシュ:

```kotlin
// ✓ 正しい
@Composable
fun VideoList(videos: List<Video>) {
    val filteredVideos = remember(videos) {
        videos.filter { it.isPublished }
    }
    // ...
}

// ✗ 禁止（毎回再計算）
@Composable
fun VideoList(videos: List<Video>) {
    val filteredVideos = videos.filter { it.isPublished }  // 毎回実行
    // ...
}
```

## derivedStateOf

状態から派生する値は `derivedStateOf`:

```kotlin
// ✓ 正しい
@Composable
fun Counter(count: Int) {
    val isEven by remember(count) {
        derivedStateOf { count % 2 == 0 }
    }
    // ...
}
```

## LaunchedEffect

キーは依存する値を指定:

```kotlin
// 特定の値が変化したときに実行
LaunchedEffect(videoId) {
    loadVideo(videoId)
}

// 初回のみ実行
LaunchedEffect(Unit) {
    initialize()
}
```

## Clock 使用禁止

Screen / Content / Component での `Clock.System` 使用を禁止。

| 層 | Clock 使用 | 備考 |
|---|---|---|
| Container | 許可 | 唯一の時刻取得ポイント |
| Screen | 禁止 | 引数で受け取る |
| Content | 禁止 | 引数で受け取る |
| Component | 禁止 | 引数で受け取る |

### 禁止理由

- テスタビリティ低下
- Preview が毎回異なる結果
- 決定論的 UI を保証できない

### 正しいパターン

```kotlin
// Container で取得
@Composable
fun VideoSyncContainer(viewModel: VideoSyncViewModel) {
    val currentTime = remember { Clock.System.now() }

    VideoSyncScreen(
        uiState = uiState,
        currentTime = currentTime  // 引数で渡す
    )
}

// Screen は引数で受け取る
@Composable
fun VideoSyncScreen(
    uiState: VideoSyncUiState,
    currentTime: Instant
) {
    // currentTime を使用
}
```
