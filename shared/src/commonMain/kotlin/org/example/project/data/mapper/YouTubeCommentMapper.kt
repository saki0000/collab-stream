package org.example.project.data.mapper

import org.example.project.data.model.CommentThreadItem
import org.example.project.domain.model.VideoComment

/**
 * YouTube API のコメントDTOをドメインモデルに変換するマッパー。
 *
 * Epic: コメントタイムスタンプ同期
 * US-1: Comment API Proxy
 */
object YouTubeCommentMapper {

    /**
     * CommentThreadItem (API DTO) を VideoComment (domain model) に変換する。
     */
    fun toDomainModel(item: CommentThreadItem): VideoComment {
        val topLevelComment = item.snippet.topLevelComment
        val snippet = topLevelComment.snippet

        return VideoComment(
            commentId = topLevelComment.id,
            authorDisplayName = snippet.authorDisplayName,
            authorProfileImageUrl = snippet.authorProfileImageUrl,
            textContent = snippet.textDisplay,
            likeCount = snippet.likeCount,
            publishedAt = snippet.publishedAt,
        )
    }

    /**
     * 複数のCommentThreadItemをVideoCommentリストに変換する。
     */
    fun toDomainModelList(items: List<CommentThreadItem>): List<VideoComment> {
        return items.map { toDomainModel(it) }
    }
}
