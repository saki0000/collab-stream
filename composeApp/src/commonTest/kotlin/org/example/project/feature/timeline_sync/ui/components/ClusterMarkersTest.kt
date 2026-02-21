package org.example.project.feature.timeline_sync.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.example.project.domain.model.TimestampMarker
import org.example.project.domain.model.VideoComment

/**
 * clusterMarkers() 関数のユニットテスト
 *
 * Specification: feature/timeline_sync/SPECIFICATION.md
 * Story Issue: US-3 (タイムスタンプマーカー表示)
 */
class ClusterMarkersTest {

    /** テスト用のデフォルト動画duration（2時間 = 7200秒） */
    private val defaultDuration = 7200L

    // ========================================
    // エッジケース
    // ========================================

    @Test
    fun `空リストの場合_空リストを返すこと`() {
        // Arrange
        val markers = emptyList<TimestampMarker>()

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = defaultDuration)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `videoDurationSecondsが0以下の場合_空リストを返すこと`() {
        // Arrange
        val markers = listOf(
            createMarker(timestampSeconds = 600L, likeCount = 100),
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = 0L)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `マーカー1つの場合_クラスター1つを返すこと`() {
        // Arrange
        val markers = listOf(
            createMarker(timestampSeconds = 600L, likeCount = 100),
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = defaultDuration)

        // Assert
        assertEquals(1, result.size)
        assertEquals(1, result.first().markers.size)
    }

    @Test
    fun `マーカー1つの場合_centerFractionがduration基準で正しいこと`() {
        // Arrange
        // 600秒 / 7200秒 = 0.0833...
        val markers = listOf(
            createMarker(timestampSeconds = 600L, likeCount = 100),
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = defaultDuration)

        // Assert
        val expectedFraction = 600f / 7200f
        assertEquals(expectedFraction, result.first().centerFraction, absoluteTolerance = 0.001f)
    }

    // ========================================
    // 離れたマーカーの分離
    // ========================================

    @Test
    fun `離れたマーカーは_別クラスターになること`() {
        // Arrange
        // duration: 7200秒
        // マーカー1: 0秒 → fraction = 0.0
        // マーカー2: 3600秒 → fraction = 0.5
        // 差分 = 0.5 > しきい値 0.03 → 別クラスター
        val markers = listOf(
            createMarker(timestampSeconds = 0L, likeCount = 100),
            createMarker(timestampSeconds = 3600L, likeCount = 200),
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = defaultDuration, clusterThreshold = 0.03f)

        // Assert
        assertEquals(2, result.size)
    }

    @Test
    fun `3つの離れたマーカーは_3つの別クラスターになること`() {
        // Arrange
        // duration: 7200秒
        // マーカー1: 0秒    → fraction = 0.0
        // マーカー2: 3000秒 → fraction ≈ 0.417
        // マーカー3: 6000秒 → fraction ≈ 0.833
        // 全て差分 > 0.03 → 3クラスター
        val markers = listOf(
            createMarker(timestampSeconds = 0L, likeCount = 100),
            createMarker(timestampSeconds = 3000L, likeCount = 200),
            createMarker(timestampSeconds = 6000L, likeCount = 300),
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = defaultDuration, clusterThreshold = 0.03f)

        // Assert
        assertEquals(3, result.size)
    }

    // ========================================
    // 近接マーカーのクラスタリング
    // ========================================

    @Test
    fun `近接マーカーは_同じクラスターに集約されること`() {
        // Arrange
        // duration: 7200秒
        // マーカー1: 0秒    → fraction = 0.0 → 別クラスター
        // マーカー2: 3600秒 → fraction = 0.5
        // マーカー3: 3700秒 → fraction ≈ 0.514
        // diff(0.5, 0.514) = 0.014 < しきい値 0.03 → 同クラスター
        val markers = listOf(
            createMarker(timestampSeconds = 0L, likeCount = 50),
            createMarker(timestampSeconds = 3600L, likeCount = 100),
            createMarker(timestampSeconds = 3700L, likeCount = 200),
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = defaultDuration, clusterThreshold = 0.03f)

        // Assert
        assertEquals(2, result.size)
        val largerCluster = result.maxByOrNull { it.markers.size }!!
        assertEquals(2, largerCluster.markers.size)
    }

    @Test
    fun `しきい値ぎりぎり以内のマーカーは_同じクラスターに集約されること`() {
        // Arrange
        // duration: 10000秒
        // マーカー1: 0秒    → fraction = 0.0
        // マーカー2: 5000秒 → fraction = 0.5
        // マーカー3: 5290秒 → fraction = 0.529
        // マーカー4: 10000秒 → fraction = 1.0
        // diff(0.5, 0.529) = 0.029 < しきい値 0.03 → 同クラスター
        val markers = listOf(
            createMarker(timestampSeconds = 0L, likeCount = 10),
            createMarker(timestampSeconds = 5000L, likeCount = 100),
            createMarker(timestampSeconds = 5290L, likeCount = 200),
            createMarker(timestampSeconds = 10000L, likeCount = 10),
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = 10000L, clusterThreshold = 0.03f)

        // Assert
        // 3クラスター: {0}, {5000+5290}, {10000}
        assertEquals(3, result.size)
        val clusteredPair = result.first { it.markers.size == 2 }
        assertEquals(2, clusteredPair.markers.size)
    }

    @Test
    fun `しきい値を超えたマーカーは_別クラスターになること`() {
        // Arrange
        // duration: 10000秒
        // fractions: 0.0, 0.5, 0.5301, 1.0
        // diff(0.5, 0.5301) = 0.0301 > しきい値 0.03 → 別クラスター
        val markers = listOf(
            createMarker(timestampSeconds = 0L, likeCount = 10),
            createMarker(timestampSeconds = 5000L, likeCount = 100),
            createMarker(timestampSeconds = 5301L, likeCount = 200),
            createMarker(timestampSeconds = 10000L, likeCount = 10),
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = 10000L, clusterThreshold = 0.03f)

        // Assert
        // 4クラスター: 全て離れている
        assertEquals(4, result.size)
    }

    // ========================================
    // 代表マーカー選択
    // ========================================

    @Test
    fun `代表マーカーは_いいね数最大のコメントが選択されること`() {
        // Arrange
        // duration: 7200秒
        // マーカー2: 3600秒 → fraction = 0.5
        // マーカー3: 3700秒 → fraction ≈ 0.514
        // diff = 0.014 < 0.03 → 同クラスター
        val markerLow = createMarker(timestampSeconds = 3600L, likeCount = 100)
        val markerHigh = createMarker(timestampSeconds = 3700L, likeCount = 500)
        val markers = listOf(
            createMarker(timestampSeconds = 0L, likeCount = 50), // 別クラスター
            markerLow,
            markerHigh,
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = defaultDuration, clusterThreshold = 0.03f)

        // Assert
        val cluster = result.first { it.markers.size == 2 }
        assertEquals(500, cluster.representativeMarker.comment.likeCount)
        assertEquals(markerHigh, cluster.representativeMarker)
    }

    @Test
    fun `マーカー1つのクラスターでは_そのマーカー自身が代表マーカーになること`() {
        // Arrange
        val marker = createMarker(timestampSeconds = 600L, likeCount = 100)
        val markers = listOf(marker)

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = defaultDuration)

        // Assert
        assertEquals(marker, result.first().representativeMarker)
    }

    // ========================================
    // centerFraction の計算
    // ========================================

    @Test
    fun `centerFractionは_クラスター内マーカーの中央位置であること`() {
        // Arrange
        // duration: 10000秒
        // マーカー2: 5000秒 → fraction = 0.5
        // マーカー3: 5200秒 → fraction = 0.52
        // diff(0.5, 0.52) = 0.02 < 0.03 → 同クラスター
        // centerFraction = (0.5 + 0.52) / 2 = 0.51
        val markers = listOf(
            createMarker(timestampSeconds = 0L, likeCount = 10),
            createMarker(timestampSeconds = 5000L, likeCount = 100),
            createMarker(timestampSeconds = 5200L, likeCount = 200),
            createMarker(timestampSeconds = 10000L, likeCount = 10),
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = 10000L, clusterThreshold = 0.03f)

        // Assert
        assertEquals(3, result.size)
        val middleCluster = result.first { it.markers.size == 2 }
        val expectedCenter = (0.5f + 0.52f) / 2f
        assertEquals(expectedCenter, middleCluster.centerFraction, absoluteTolerance = 0.001f)
    }

    @Test
    fun `離れたマーカーのcenterFractionは_それぞれの正確な位置であること`() {
        // Arrange
        // duration: 7200秒
        // マーカー1: 0秒    → fraction = 0.0
        // マーカー2: 3600秒 → fraction = 0.5
        val markers = listOf(
            createMarker(timestampSeconds = 0L, likeCount = 100),
            createMarker(timestampSeconds = 3600L, likeCount = 200),
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = defaultDuration, clusterThreshold = 0.03f)

        // Assert
        assertEquals(2, result.size)
        val sortedResult = result.sortedBy { it.centerFraction }
        assertEquals(0.0f, sortedResult[0].centerFraction, absoluteTolerance = 0.001f)
        assertEquals(0.5f, sortedResult[1].centerFraction, absoluteTolerance = 0.001f)
    }

    // ========================================
    // 同じ位置のマーカー
    // ========================================

    @Test
    fun `同じ位置のマーカーは_1クラスターに集約されること`() {
        // Arrange
        // duration: 7200秒
        // マーカー1,2: 1800秒 → fraction = 0.25（差分 = 0 < 0.03）→ 同クラスター
        val markers = listOf(
            createMarker(timestampSeconds = 1800L, likeCount = 100),
            createMarker(timestampSeconds = 1800L, likeCount = 300),
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = defaultDuration, clusterThreshold = 0.03f)

        // Assert
        assertEquals(1, result.size)
        assertEquals(2, result.first().markers.size)
    }

    @Test
    fun `同じ位置の複数マーカーでは_いいね数最大が代表マーカーになること`() {
        // Arrange
        val markerA = createMarker(timestampSeconds = 1800L, likeCount = 100)
        val markerB = createMarker(timestampSeconds = 1800L, likeCount = 300) // 最大いいね
        val markerC = createMarker(timestampSeconds = 1800L, likeCount = 200)
        val markers = listOf(markerA, markerB, markerC)

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = defaultDuration, clusterThreshold = 0.03f)

        // Assert
        assertEquals(1, result.size)
        assertEquals(300, result.first().representativeMarker.comment.likeCount)
        assertEquals(markerB, result.first().representativeMarker)
    }

    // ========================================
    // ソート順
    // ========================================

    @Test
    fun `マーカーがソートされていない場合でも_timestampSeconds順でクラスタリングされること`() {
        // Arrange
        // 入力は逆順（3600秒 → 1800秒 → 0秒）
        // duration: 7200秒
        // ソート後: 0秒(fraction=0.0), 1800秒(fraction=0.25), 3600秒(fraction=0.5)
        // 全て離れているため3クラスター
        val markers = listOf(
            createMarker(timestampSeconds = 3600L, likeCount = 100),
            createMarker(timestampSeconds = 1800L, likeCount = 200),
            createMarker(timestampSeconds = 0L, likeCount = 300),
        )

        // Act
        val result = clusterMarkers(markers, videoDurationSeconds = defaultDuration, clusterThreshold = 0.03f)

        // Assert
        assertEquals(3, result.size)
        val sortedResult = result.sortedBy { it.centerFraction }
        assertEquals(0.0f, sortedResult[0].centerFraction, absoluteTolerance = 0.001f)
        assertEquals(0.25f, sortedResult[1].centerFraction, absoluteTolerance = 0.001f)
        assertEquals(0.5f, sortedResult[2].centerFraction, absoluteTolerance = 0.001f)
    }

    // ========================================
    // ヘルパーメソッド
    // ========================================

    /** テスト用マーカーを生成するヘルパー関数 */
    private fun createMarker(
        timestampSeconds: Long,
        likeCount: Int,
        commentId: String = "comment_$timestampSeconds",
    ): TimestampMarker = TimestampMarker(
        timestampSeconds = timestampSeconds,
        displayTimestamp = formatTimestamp(timestampSeconds),
        comment = VideoComment(
            commentId = commentId,
            authorDisplayName = "テストユーザー",
            authorProfileImageUrl = "",
            textContent = "$timestampSeconds 秒のコメント",
            likeCount = likeCount,
            publishedAt = "2024-01-01T10:00:00Z",
        ),
    )

    /** 秒数を表示用タイムスタンプに変換するヘルパー */
    private fun formatTimestamp(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            "${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
        } else {
            "${m}:${s.toString().padStart(2, '0')}"
        }
    }
}

/**
 * Float の近似等価検証ヘルパー（kotlinx.test には assertApproxEquals がないため自前で定義）
 */
private fun assertEquals(
    expected: Float,
    actual: Float,
    absoluteTolerance: Float,
) {
    val diff = kotlin.math.abs(expected - actual)
    assertTrue(
        diff <= absoluteTolerance,
        "Expected $expected but was $actual (差分 $diff > 許容値 $absoluteTolerance)",
    )
}
