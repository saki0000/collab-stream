# テスト規約

## フレームワーク

- **kotlin.test** を使用（JUnit 5 を直接使用しない）
- Kotlin Multiplatform 対応のため

## スタイル

- **バッククォート + 日本語**でテスト名を記述
- **コメントセクション**で論理的にグルーピング
- Given-When-Then または Arrange-Act-Assert パターン

## 配置場所

| ディレクトリ | 用途 |
|-------------|------|
| `shared/src/commonTest/` | 共通テスト（全プラットフォーム） |
| `shared/src/jvmTest/` | JVM 固有テスト |
| `composeApp/src/commonTest/` | UI 層テスト |

## コード例

```kotlin
package org.example.project.feature.video_sync

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * VideoSync機能のテスト
 * Specification: feature/video_sync/SPECIFICATION.md
 * Story Issue: #123
 */
class VideoSyncUseCaseTest {

    // ========================================
    // 同期時刻計算
    // ========================================

    @Test
    fun `同期時刻計算_正の時刻オフセットを正しく計算すること`() {
        // Arrange
        val baseTime = 1000L
        val offset = 500L

        // Act
        val result = calculateAbsoluteTime(baseTime, offset)

        // Assert
        assertEquals(1500L, result)
    }

    @Test
    fun `同期時刻計算_負の時刻オフセットを正しく計算すること`() {
        // Arrange
        val baseTime = 1000L
        val offset = -200L

        // Act
        val result = calculateAbsoluteTime(baseTime, offset)

        // Assert
        assertEquals(800L, result)
    }

    // ========================================
    // エラーケース
    // ========================================

    @Test
    fun `エラーケース_無効な入力でエラーを返すこと`() {
        // TODO: 実装
    }
}
```

## 命名規則

- テストクラス: `{対象クラス}Test`
- テストメソッド: バッククォートで `{コンテキスト}_{期待する振る舞い}` 形式
  - 例: `` `画面を開いた時_ローディング状態になること`() ``
  - 例: `` `データ取得成功時_コンテンツが表示されること`() ``

## カバレッジ目標

- ドメイン層: 80% 以上
- データ層: 70% 以上
- UI 層: 主要なロジックのみ

## 注意事項

### commonTest での制約

`commonTest` は Kotlin Multiplatform の共通テストディレクトリです。
以下のアノテーションは **使用できません**（JUnit 5 固有のため）:

- `@DisplayName` - 代わりにバッククォートでメソッド名を記述
- `@Nested` - 代わりにコメントセクションでグルーピング

### JVM 固有テスト

JUnit 5 固有機能（`@DisplayName`, `@Nested` 等）が必要な場合は `jvmTest` に配置:

```
shared/src/jvmTest/kotlin/...
composeApp/src/test/kotlin/...  # Android/JVM 向け
```
