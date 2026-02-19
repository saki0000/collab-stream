package org.example.project.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.example.project.domain.model.TimestampExtractor
import org.example.project.domain.model.TimestampMarker
import org.example.project.domain.model.VideoComment

/**
 * CommentRepositoryImplのテスト。
 * （注: MockEngineはcommonTestで利用できないため、
 * タイムスタンプマーカー生成のロジックテストのみ実施）
 *
 * Epic: コメントタイムスタンプ同期
 * US-2: タイムスタンプ抽出とマーカー生成
 */
class CommentRepositoryImplTest {

    // ========================================
    // タイムスタンプマーカー生成のロジックテスト
    // ========================================

    @Test
    fun `タイムスタンプマーカー生成_コメントから正しくマーカーを生成すること`() {
        // Arrange
        val comments = listOf(
            VideoComment(
                commentId = "comment1",
                authorDisplayName = "User1",
                authorProfileImageUrl = "https://example.com/user1.jpg",
                textContent = "面白いシーンは 1:23 です",
                likeCount = 10,
                publishedAt = "2024-01-01T00:00:00Z",
            ),
            VideoComment(
                commentId = "comment2",
                authorDisplayName = "User2",
                authorProfileImageUrl = "https://example.com/user2.jpg",
                textContent = "5:30 と 10:45 が良かった",
                likeCount = 5,
                publishedAt = "2024-01-01T01:00:00Z",
            ),
        )

        // Act
        val markers = buildTimestampMarkers(comments)

        // Assert
        assertEquals(3, markers.size)

        // 1つ目のマーカー（1:23）
        assertEquals(83L, markers[0].timestampSeconds)
        assertEquals("1:23", markers[0].displayTimestamp)
        assertEquals("comment1", markers[0].comment.commentId)
        assertEquals("User1", markers[0].comment.authorDisplayName)

        // 2つ目のマーカー（5:30）
        assertEquals(330L, markers[1].timestampSeconds)
        assertEquals("5:30", markers[1].displayTimestamp)
        assertEquals("comment2", markers[1].comment.commentId)
        assertEquals("User2", markers[1].comment.authorDisplayName)

        // 3つ目のマーカー（10:45）
        assertEquals(645L, markers[2].timestampSeconds)
        assertEquals("10:45", markers[2].displayTimestamp)
        assertEquals("comment2", markers[2].comment.commentId)
    }

    @Test
    fun `タイムスタンプマーカー生成_タイムスタンプなしのコメントで空リストを返すこと`() {
        // Arrange
        val comments = listOf(
            VideoComment(
                commentId = "comment1",
                authorDisplayName = "User1",
                authorProfileImageUrl = "https://example.com/user1.jpg",
                textContent = "タイムスタンプなし",
                likeCount = 10,
                publishedAt = "2024-01-01T00:00:00Z",
            ),
        )

        // Act
        val markers = buildTimestampMarkers(comments)

        // Assert
        assertTrue(markers.isEmpty())
    }

    @Test
    fun `タイムスタンプマーカー生成_複数コメントから複数マーカーを生成すること`() {
        // Arrange
        val comments = listOf(
            VideoComment(
                commentId = "comment1",
                authorDisplayName = "User1",
                authorProfileImageUrl = "https://example.com/user1.jpg",
                textContent = "0:30 が良い",
                likeCount = 10,
                publishedAt = "2024-01-01T00:00:00Z",
            ),
            VideoComment(
                commentId = "comment2",
                authorDisplayName = "User2",
                authorProfileImageUrl = "https://example.com/user2.jpg",
                textContent = "1:00 も良い",
                likeCount = 5,
                publishedAt = "2024-01-01T01:00:00Z",
            ),
            VideoComment(
                commentId = "comment3",
                authorDisplayName = "User3",
                authorProfileImageUrl = "https://example.com/user3.jpg",
                textContent = "タイムスタンプなし",
                likeCount = 3,
                publishedAt = "2024-01-01T02:00:00Z",
            ),
        )

        // Act
        val markers = buildTimestampMarkers(comments)

        // Assert
        assertEquals(2, markers.size)
        assertEquals(30L, markers[0].timestampSeconds) // 0:30
        assertEquals(60L, markers[1].timestampSeconds) // 1:00
    }

    @Test
    fun `タイムスタンプマーカー生成_1コメント内の複数タイムスタンプから複数マーカーを生成すること`() {
        // Arrange
        val comments = listOf(
            VideoComment(
                commentId = "comment1",
                authorDisplayName = "User1",
                authorProfileImageUrl = "https://example.com/user1.jpg",
                textContent = "1:23 と 5:45 と 10:00 が面白い",
                likeCount = 10,
                publishedAt = "2024-01-01T00:00:00Z",
            ),
        )

        // Act
        val markers = buildTimestampMarkers(comments)

        // Assert
        assertEquals(3, markers.size)
        assertEquals(83L, markers[0].timestampSeconds) // 1:23
        assertEquals(345L, markers[1].timestampSeconds) // 5:45
        assertEquals(600L, markers[2].timestampSeconds) // 10:00

        // すべて同じコメントを参照
        assertEquals("comment1", markers[0].comment.commentId)
        assertEquals("comment1", markers[1].comment.commentId)
        assertEquals("comment1", markers[2].comment.commentId)
    }

    @Test
    fun `タイムスタンプマーカー生成_空のコメントリストで空リストを返すこと`() {
        // Arrange
        val comments = emptyList<VideoComment>()

        // Act
        val markers = buildTimestampMarkers(comments)

        // Assert
        assertTrue(markers.isEmpty())
    }

    // ========================================
    // ヘルパー関数（CommentRepositoryImplの内部ロジックを再現）
    // ========================================

    /**
     * CommentRepositoryImplのbuildTimestampMarkersメソッドを再現。
     * （テストのためにprivateメソッドのロジックを検証）
     */
    private fun buildTimestampMarkers(comments: List<VideoComment>): List<TimestampMarker> {
        return comments.flatMap { comment ->
            val timestamps = TimestampExtractor.extractTimestamps(
                text = comment.textContent,
                videoDurationSeconds = null,
            )

            timestamps.map { extractedTimestamp ->
                TimestampMarker(
                    timestampSeconds = extractedTimestamp.timestampSeconds,
                    displayTimestamp = extractedTimestamp.displayTimestamp,
                    comment = comment,
                )
            }
        }
    }
}
