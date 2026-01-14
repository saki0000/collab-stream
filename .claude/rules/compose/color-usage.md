---
paths: composeApp/**/*.kt
---

# カラー使用ルール

`MaterialTheme.colorScheme.*` のみを使用すること。

## 禁止

| パターン | 理由 |
|---------|------|
| `Color(0xFF...)` | ダークモード非対応 |
| `Color.Red`, `Color.White` | テーマ不統一 |
| `OrangePrimary`, `DarkContainer` | colorScheme 経由で使用すべき |

## 許可

| パターン | 用途 |
|---------|------|
| `MaterialTheme.colorScheme.primary` | テーマカラー |
| `MaterialTheme.colorScheme.surface` | 背景色 |
| `MaterialTheme.colorScheme.onSurface` | テキスト色 |

## 例外

| パターン | 用途 |
|---------|------|
| `getPlatformColor()` | プラットフォーム固有カラー |
| `colorScheme.primary.copy(alpha = 0.5f)` | アルファ調整 |
| `Color.Transparent` | 透明色 |

## コード例

```kotlin
// ✓ 正しい
Text(
    text = "Hello",
    color = MaterialTheme.colorScheme.onSurface
)

Box(
    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
)

// ✗ 禁止
Text(
    text = "Hello",
    color = Color.Black  // ダークモードで見えなくなる
)

Box(
    modifier = Modifier.background(Color(0xFFFFFFFF))  // ハードコード禁止
)
```
