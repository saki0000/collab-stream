package org.example.project.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import org.example.project.data.model.CommentItem
import org.example.project.data.model.CommentSnippet
import org.example.project.data.model.CommentThreadItem
import org.example.project.data.model.CommentThreadSnippet

/**
 * YouTubeCommentMapperのテスト。
 *
 * Epic: コメントタイムスタンプ同期
 * US-1: Comment API Proxy
 */
class YouTubeCommentMapperTest {

    // ========================================
    // 単一コメント変換
    // ========================================

    @Test
    fun `単一コメント変換_完全なCommentThreadItemをVideoCommentに正しく変換すること`() {
        // Arrange
        val commentThreadItem = CommentThreadItem(
            kind = "youtube#commentThread",
            id = "thread-id-1",
            snippet = CommentThreadSnippet(
                videoId = "video-123",
                topLevelComment = CommentItem(
                    kind = "youtube#comment",
                    id = "comment-id-1",
                    snippet = CommentSnippet(
                        textDisplay = "Great moment at 1:23:45!",
                        authorDisplayName = "John Doe",
                        authorProfileImageUrl = "https://example.com/profile.jpg",
                        likeCount = 42,
                        publishedAt = "2023-01-15T10:00:00Z",
                    ),
                ),
                totalReplyCount = 3,
            ),
        )

        // Act
        val result = YouTubeCommentMapper.toDomainModel(commentThreadItem)

        // Assert
        assertEquals("comment-id-1", result.commentId)
        assertEquals("John Doe", result.authorDisplayName)
        assertEquals("https://example.com/profile.jpg", result.authorProfileImageUrl)
        assertEquals("Great moment at 1:23:45!", result.textContent)
        assertEquals(42, result.likeCount)
        assertEquals("2023-01-15T10:00:00Z", result.publishedAt)
    }

    @Test
    fun `単一コメント変換_いいね数が0のコメントを正しく変換すること`() {
        // Arrange
        val commentThreadItem = CommentThreadItem(
            kind = "youtube#commentThread",
            id = "thread-id-2",
            snippet = CommentThreadSnippet(
                videoId = "video-123",
                topLevelComment = CommentItem(
                    kind = "youtube#comment",
                    id = "comment-id-2",
                    snippet = CommentSnippet(
                        textDisplay = "Check 12:34",
                        authorDisplayName = "Jane Smith",
                        authorProfileImageUrl = "https://example.com/jane.jpg",
                        likeCount = 0,
                        publishedAt = "2023-02-20T15:30:00Z",
                    ),
                ),
                totalReplyCount = 0,
            ),
        )

        // Act
        val result = YouTubeCommentMapper.toDomainModel(commentThreadItem)

        // Assert
        assertEquals("comment-id-2", result.commentId)
        assertEquals("Jane Smith", result.authorDisplayName)
        assertEquals("Check 12:34", result.textContent)
        assertEquals(0, result.likeCount)
    }

    @Test
    fun `単一コメント変換_長いコメントテキストを正しく変換すること`() {
        // Arrange
        val longText = "This is a very long comment with timestamp 1:30:00. " +
            "It contains multiple sentences and describes a detailed moment in the video."
        val commentThreadItem = CommentThreadItem(
            kind = "youtube#commentThread",
            id = "thread-id-3",
            snippet = CommentThreadSnippet(
                videoId = "video-123",
                topLevelComment = CommentItem(
                    kind = "youtube#comment",
                    id = "comment-id-3",
                    snippet = CommentSnippet(
                        textDisplay = longText,
                        authorDisplayName = "Long Comment Author",
                        authorProfileImageUrl = "https://example.com/author.jpg",
                        likeCount = 100,
                        publishedAt = "2023-03-10T08:00:00Z",
                    ),
                ),
                totalReplyCount = 10,
            ),
        )

        // Act
        val result = YouTubeCommentMapper.toDomainModel(commentThreadItem)

        // Assert
        assertEquals("comment-id-3", result.commentId)
        assertEquals(longText, result.textContent)
        assertEquals(100, result.likeCount)
    }

    // ========================================
    // 複数コメント変換
    // ========================================

    @Test
    fun `複数コメント変換_複数のCommentThreadItemをVideoCommentリストに正しく変換すること`() {
        // Arrange
        val items = listOf(
            CommentThreadItem(
                kind = "youtube#commentThread",
                id = "thread-1",
                snippet = CommentThreadSnippet(
                    videoId = "video-123",
                    topLevelComment = CommentItem(
                        kind = "youtube#comment",
                        id = "comment-1",
                        snippet = CommentSnippet(
                            textDisplay = "Comment 1 at 1:00",
                            authorDisplayName = "Author 1",
                            authorProfileImageUrl = "https://example.com/1.jpg",
                            likeCount = 10,
                            publishedAt = "2023-01-01T10:00:00Z",
                        ),
                    ),
                    totalReplyCount = 1,
                ),
            ),
            CommentThreadItem(
                kind = "youtube#commentThread",
                id = "thread-2",
                snippet = CommentThreadSnippet(
                    videoId = "video-123",
                    topLevelComment = CommentItem(
                        kind = "youtube#comment",
                        id = "comment-2",
                        snippet = CommentSnippet(
                            textDisplay = "Comment 2 at 2:00",
                            authorDisplayName = "Author 2",
                            authorProfileImageUrl = "https://example.com/2.jpg",
                            likeCount = 20,
                            publishedAt = "2023-01-02T11:00:00Z",
                        ),
                    ),
                    totalReplyCount = 0,
                ),
            ),
            CommentThreadItem(
                kind = "youtube#commentThread",
                id = "thread-3",
                snippet = CommentThreadSnippet(
                    videoId = "video-123",
                    topLevelComment = CommentItem(
                        kind = "youtube#comment",
                        id = "comment-3",
                        snippet = CommentSnippet(
                            textDisplay = "Comment 3 at 3:00",
                            authorDisplayName = "Author 3",
                            authorProfileImageUrl = "https://example.com/3.jpg",
                            likeCount = 30,
                            publishedAt = "2023-01-03T12:00:00Z",
                        ),
                    ),
                    totalReplyCount = 5,
                ),
            ),
        )

        // Act
        val result = YouTubeCommentMapper.toDomainModelList(items)

        // Assert
        assertEquals(3, result.size)
        assertEquals("comment-1", result[0].commentId)
        assertEquals("Author 1", result[0].authorDisplayName)
        assertEquals(10, result[0].likeCount)
        assertEquals("comment-2", result[1].commentId)
        assertEquals("Author 2", result[1].authorDisplayName)
        assertEquals(20, result[1].likeCount)
        assertEquals("comment-3", result[2].commentId)
        assertEquals("Author 3", result[2].authorDisplayName)
        assertEquals(30, result[2].likeCount)
    }

    @Test
    fun `複数コメント変換_空のリストを正しく処理すること`() {
        // Arrange
        val emptyList = emptyList<CommentThreadItem>()

        // Act
        val result = YouTubeCommentMapper.toDomainModelList(emptyList)

        // Assert
        assertEquals(0, result.size)
    }

    @Test
    fun `複数コメント変換_1件のリストを正しく処理すること`() {
        // Arrange
        val singleItemList = listOf(
            CommentThreadItem(
                kind = "youtube#commentThread",
                id = "thread-single",
                snippet = CommentThreadSnippet(
                    videoId = "video-123",
                    topLevelComment = CommentItem(
                        kind = "youtube#comment",
                        id = "comment-single",
                        snippet = CommentSnippet(
                            textDisplay = "Single comment",
                            authorDisplayName = "Solo Author",
                            authorProfileImageUrl = "https://example.com/solo.jpg",
                            likeCount = 5,
                            publishedAt = "2023-01-01T10:00:00Z",
                        ),
                    ),
                    totalReplyCount = 0,
                ),
            ),
        )

        // Act
        val result = YouTubeCommentMapper.toDomainModelList(singleItemList)

        // Assert
        assertEquals(1, result.size)
        assertEquals("comment-single", result[0].commentId)
        assertEquals("Solo Author", result[0].authorDisplayName)
    }
}
