# テスト規約

## フレームワーク

- **kotlin.test** を使用（JUnit 5 を直接使用しない）
- Kotlin Multiplatform 対応のため

## スタイル

- **ネストクラス** + **@DisplayName** による構造化
- Given-When-Then または Arrange-Act-Assert パターン

## 配置場所

| ディレクトリ | 用途 |
|-------------|------|
| `shared/src/commonTest/` | 共通テスト（全プラットフォーム） |
| `shared/src/jvmTest/` | JVM 固有テスト |
| `composeApp/src/commonTest/` | UI 層テスト |

## コード例

```kotlin
@DisplayName("VideoSync機能のテスト")
class VideoSyncUseCaseTest {

    @Nested
    @DisplayName("同期時刻計算")
    inner class CalculateAbsoluteTime {

        @Test
        @DisplayName("正の時刻オフセットを正しく計算すること")
        fun `should calculate correct absolute time with positive offset`() {
            // Arrange
            val baseTime = 1000L
            val offset = 500L

            // Act
            val result = calculateAbsoluteTime(baseTime, offset)

            // Assert
            assertEquals(1500L, result)
        }
    }
}
```

## 命名規則

- テストクラス: `{対象クラス}Test`
- テストメソッド: バッククォートで日本語説明可
- DisplayName: 日本語で機能を説明

## カバレッジ目標

- ドメイン層: 80% 以上
- データ層: 70% 以上
- UI 層: 主要なロジックのみ
