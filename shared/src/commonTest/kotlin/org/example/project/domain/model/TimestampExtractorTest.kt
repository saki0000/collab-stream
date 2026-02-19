package org.example.project.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TimestampExtractorのテスト。
 *
 * Epic: コメントタイムスタンプ同期
 * US-2: タイムスタンプ抽出とマーカー生成
 */
class TimestampExtractorTest {

    // ========================================
    // M:SS 形式の抽出
    // ========================================

    @Test
    fun `M_SS形式_1桁の分と2桁の秒を正しく抽出すること`() {
        // Arrange
        val text = "この動画の 5:30 が面白い"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertEquals(1, result.size)
        assertEquals(330L, result[0].timestampSeconds) // 5 * 60 + 30
        assertEquals("5:30", result[0].displayTimestamp)
    }

    // ========================================
    // MM:SS 形式の抽出
    // ========================================

    @Test
    fun `MM_SS形式_2桁の分と2桁の秒を正しく抽出すること`() {
        // Arrange
        val text = "ここ 10:45 が最高"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertEquals(1, result.size)
        assertEquals(645L, result[0].timestampSeconds) // 10 * 60 + 45
        assertEquals("10:45", result[0].displayTimestamp)
    }

    // ========================================
    // H:MM:SS 形式の抽出
    // ========================================

    @Test
    fun `H_MM_SS形式_1桁の時と2桁の分と秒を正しく抽出すること`() {
        // Arrange
        val text = "1:23:45 から始まる"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertEquals(1, result.size)
        assertEquals(5025L, result[0].timestampSeconds) // 1 * 3600 + 23 * 60 + 45
        assertEquals("1:23:45", result[0].displayTimestamp)
    }

    // ========================================
    // HH:MM:SS 形式の抽出
    // ========================================

    @Test
    fun `HH_MM_SS形式_2桁の時と2桁の分と秒を正しく抽出すること`() {
        // Arrange
        val text = "配信は 12:34:56 まで続いた"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertEquals(1, result.size)
        assertEquals(45296L, result[0].timestampSeconds) // 12 * 3600 + 34 * 60 + 56
        assertEquals("12:34:56", result[0].displayTimestamp)
    }

    // ========================================
    // 複数タイムスタンプの抽出
    // ========================================

    @Test
    fun `複数タイムスタンプ_テキスト内の複数のタイムスタンプを全て抽出すること`() {
        // Arrange
        val text = "1:23 と 5:45:30 と 10:00 が面白い"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertEquals(3, result.size)
        assertEquals(83L, result[0].timestampSeconds) // 1:23
        assertEquals(20730L, result[1].timestampSeconds) // 5:45:30
        assertEquals(600L, result[2].timestampSeconds) // 10:00
    }

    // ========================================
    // 無効な秒の除外
    // ========================================

    @Test
    fun `無効な秒_60秒以上のタイムスタンプを除外すること`() {
        // Arrange
        val text = "5:60 は無効だが 5:59 は有効"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertEquals(1, result.size)
        assertEquals(359L, result[0].timestampSeconds) // 5:59 のみ
        assertEquals("5:59", result[0].displayTimestamp)
    }

    @Test
    fun `無効な秒_H_MM_SS形式で60秒以上を除外すること`() {
        // Arrange
        val text = "1:23:60 は無効"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertTrue(result.isEmpty())
    }

    // ========================================
    // 無効な分の除外（H:MM:SS形式）
    // ========================================

    @Test
    fun `無効な分_H_MM_SS形式で60分以上を除外すること`() {
        // Arrange
        val text = "1:60:30 は無効だが 1:59:30 は有効"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertEquals(1, result.size)
        assertEquals(7170L, result[0].timestampSeconds) // 1:59:30 のみ
        assertEquals("1:59:30", result[0].displayTimestamp)
    }

    // ========================================
    // 誤検出防止
    // ========================================

    @Test
    fun `誤検出防止_M_S形式（秒1桁）を除外すること`() {
        // Arrange
        val text = "5:3 は日付かもしれないので除外"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `誤検出防止_数字が連続している場合を除外すること`() {
        // Arrange
        val text = "192.168.1.1:8080 はIPアドレス"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `誤検出防止_日付形式を除外すること`() {
        // Arrange
        val text = "2024/5:30 は日付"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertTrue(result.isEmpty())
    }

    // ========================================
    // 動画の長さチェック
    // ========================================

    @Test
    fun `動画の長さチェック_動画の長さを超えるタイムスタンプを除外すること`() {
        // Arrange
        val text = "5:30 と 10:00 と 20:00"
        val videoDuration = 900L // 15分 = 900秒

        // Act
        val result = TimestampExtractor.extractTimestamps(text, videoDuration)

        // Assert
        assertEquals(2, result.size)
        assertEquals(330L, result[0].timestampSeconds) // 5:30
        assertEquals(600L, result[1].timestampSeconds) // 10:00
        // 20:00 (1200秒) は除外される
    }

    @Test
    fun `動画の長さチェック_動画の長さと同じタイムスタンプは含むこと`() {
        // Arrange
        val text = "10:00 がラスト"
        val videoDuration = 600L // 10分 = 600秒

        // Act
        val result = TimestampExtractor.extractTimestamps(text, videoDuration)

        // Assert
        assertEquals(1, result.size)
        assertEquals(600L, result[0].timestampSeconds)
    }

    // ========================================
    // toSeconds メソッド
    // ========================================

    @Test
    fun `toSeconds_時分秒を正しく秒数に変換すること`() {
        // Arrange & Act & Assert
        assertEquals(0L, TimestampExtractor.toSeconds(0, 0, 0))
        assertEquals(30L, TimestampExtractor.toSeconds(0, 0, 30))
        assertEquals(90L, TimestampExtractor.toSeconds(0, 1, 30))
        assertEquals(3661L, TimestampExtractor.toSeconds(1, 1, 1))
        assertEquals(45296L, TimestampExtractor.toSeconds(12, 34, 56))
    }

    // ========================================
    // エッジケース
    // ========================================

    @Test
    fun `エッジケース_空文字列で空リストを返すこと`() {
        // Arrange
        val text = ""

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `エッジケース_タイムスタンプが含まれないテキストで空リストを返すこと`() {
        // Arrange
        val text = "これはただのテキストです"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `エッジケース_0_00を正しく抽出すること`() {
        // Arrange
        val text = "0:00 からスタート"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertEquals(1, result.size)
        assertEquals(0L, result[0].timestampSeconds)
        assertEquals("0:00", result[0].displayTimestamp)
    }

    @Test
    fun `エッジケース_0_00_00を正しく抽出すること`() {
        // Arrange
        val text = "0:00:00 から"

        // Act
        val result = TimestampExtractor.extractTimestamps(text)

        // Assert
        assertEquals(1, result.size)
        assertEquals(0L, result[0].timestampSeconds)
        assertEquals("0:00:00", result[0].displayTimestamp)
    }
}
