---
paths: composeApp/**/*.kt
---

# アクセシビリティ規約

## contentDescription

### 必須

インタラクティブ要素には `contentDescription` を必ず設定:

```kotlin
// ✓ 正しい
IconButton(onClick = onClose) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "閉じる"  // 日本語で説明
    )
}

// ✗ 禁止
IconButton(onClick = onClose) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = null  // アクセシビリティ違反
    )
}
```

### 例外（null 許容）

装飾目的のみのアイコン（テキスト横の補足アイコン）:

```kotlin
// 装飾アイコンは null 許容
Row {
    Icon(
        imageVector = Icons.Default.Info,
        contentDescription = null  // テキストで説明されるため
    )
    Text("詳細情報")
}
```

## semantics

複合コンポーネントは `mergeDescendants` を活用:

```kotlin
Row(
    modifier = Modifier.semantics(mergeDescendants = true) {
        contentDescription = "動画を再生中、残り時間 5:30"
    }
) {
    Icon(Icons.Default.PlayArrow, null)
    Text("再生中")
    Text("5:30")
}
```

## チェックリスト

| 要素 | contentDescription |
|------|-------------------|
| ボタン | 必須（動作を説明） |
| リンク | 必須（遷移先を説明） |
| 入力フィールド | label で代替可 |
| 装飾アイコン | null 許容 |
| 画像 | 必須（内容を説明） |
