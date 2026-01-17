# 日時処理ルール

## Clock の使用

**`kotlin.time.Clock` を使用すること。`kotlinx.datetime.Clock` は非推奨。**

### 正しいインポート

```kotlin
import kotlin.time.Clock
```

### 正しい使用例

```kotlin
// 現在時刻の取得
val now: Instant = Clock.System.now()

// 今日の日付を取得
val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
```

### 完全修飾名での使用

インポートせずに使う場合:

```kotlin
val now = kotlin.time.Clock.System.now()
```

### 非推奨（使用禁止）

```kotlin
// ❌ 非推奨警告が出る
import kotlinx.datetime.Clock

// ❌ 以下も非推奨
kotlinx.datetime.Clock.System.now()
```

## kotlinx-datetime との関係

- `kotlin.time.Clock` は Kotlin 標準ライブラリ
- `kotlinx.datetime.Clock` は kotlinx-datetime ライブラリの typealias（非推奨）
- `Instant`, `LocalDate`, `TimeZone` などは引き続き `kotlinx.datetime` を使用

### 正しい組み合わせ

```kotlin
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val now: Instant = Clock.System.now()
val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
```
