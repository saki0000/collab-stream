package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Dimensions
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.TimestampMarker
import org.example.project.domain.model.VideoComment
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * コメントリストの1行アイテムコンポーネント。
 *
 * 左: 著者アイコン（丸形）
 * 右上: 著者名 + 投稿日時
 * 右中: コメントテキスト（タイムスタンプ部分はクリッカブルリンク）
 * 右下: いいね数
 *
 * US-4: コメントリスト機能
 */
@Composable
fun CommentListItem(
    marker: TimestampMarker,
    onTimestampClick: (timestampSeconds: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val comment = marker.comment

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        // 著者アイコン（プレースホルダー）
        Box(
            modifier = Modifier
                .size(Dimensions.icon2xl)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null, // 装飾アイコン（著者名テキストで説明される）
                modifier = Modifier.size(Dimensions.icon2xl),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }

        // コメント情報
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            // 著者名 + 投稿日時
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.authorDisplayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = formatPublishedAt(comment.publishedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            // コメントテキスト（タイムスタンプリンク付き）
            CommentTextWithTimestampLink(
                comment = comment,
                markerTimestampSeconds = marker.timestampSeconds,
                markerDisplayTimestamp = marker.displayTimestamp,
                onTimestampClick = { onTimestampClick(marker.timestampSeconds) },
            )

            // いいね数
            if (comment.likeCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = null, // いいね数テキストで説明される
                        modifier = Modifier.size(Dimensions.iconXs),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatLikeCount(comment.likeCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * コメントテキスト内のタイムスタンプをリンクとして表示するコンポーネント。
 *
 * テキスト内のタイムスタンプ部分は強調表示し、
 * マーカーの displayTimestamp を目立つタップ可能なボタンとして表示する。
 */
@Composable
private fun CommentTextWithTimestampLink(
    comment: VideoComment,
    markerTimestampSeconds: Long,
    markerDisplayTimestamp: String,
    onTimestampClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // タイムスタンプ正規表現パターン（TimestampExtractor と同じ）
    val timestampRegex = remember {
        Regex("""(?<!\d)(?<!/)(\d{1,2}):(\d{2})(?::(\d{2}))?(?!\d)""")
    }

    Column(modifier = modifier) {
        // コメントテキストを AnnotatedString で構築してタイムスタンプを強調表示
        val annotatedText = remember(comment.textContent, markerDisplayTimestamp) {
            buildAnnotatedString {
                val text = comment.textContent
                var lastEnd = 0

                // タイムスタンプパターンにマッチする箇所を強調表示
                timestampRegex.findAll(text).forEach { matchResult ->
                    val start = matchResult.range.first
                    val end = matchResult.range.last + 1

                    // マッチ前のテキストを追加
                    if (lastEnd < start) {
                        append(text.substring(lastEnd, start))
                    }

                    // タイムスタンプ部分をリンクスタイルで追加
                    withStyle(
                        style = SpanStyle(
                            // カラー参照は外部で設定するため null 相当（後でカラーを上書きしない）
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) {
                        append(matchResult.value)
                    }
                    lastEnd = end
                }

                // 残りのテキスト
                if (lastEnd < text.length) {
                    append(text.substring(lastEnd))
                }
            }
        }

        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )

        // タイムスタンプタップボタン（明示的なタップ領域として表示）
        TextButton(
            onClick = onTimestampClick,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = Spacing.xs,
                vertical = Spacing.xxs,
            ),
            modifier = Modifier.padding(top = Spacing.xxs),
        ) {
            Text(
                text = markerDisplayTimestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * ISO 8601 形式の日時文字列を簡易表示形式に変換する。
 * "2024-01-15T10:30:00Z" → "2024/01/15"
 */
private fun formatPublishedAt(publishedAt: String): String {
    return try {
        // "2024-01-15T10:30:00Z" から日付部分のみ抽出
        val dateStr = publishedAt.substringBefore("T")
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            "${parts[0]}/${parts[1]}/${parts[2]}"
        } else {
            publishedAt
        }
    } catch (_: Exception) {
        publishedAt
    }
}

/**
 * いいね数を表示用文字列にフォーマットする。
 * 1000 以上は "1.2K" 形式で表示する。
 */
private fun formatLikeCount(likeCount: Int): String {
    return when {
        likeCount >= 1000 -> "${(likeCount / 100) / 10.0}K"
        else -> likeCount.toString()
    }
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun CommentListItemPreview() {
    AppTheme {
        Surface {
            CommentListItem(
                marker = TimestampMarker(
                    timestampSeconds = 630L,
                    displayTimestamp = "10:30",
                    comment = VideoComment(
                        commentId = "comment_001",
                        authorDisplayName = "Gaming Fan",
                        authorProfileImageUrl = "",
                        textContent = "この瞬間めちゃくちゃ面白い！ 10:30 のシーンは最高でした",
                        likeCount = 1234,
                        publishedAt = "2024-01-15T10:00:00Z",
                    ),
                ),
                onTimestampClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun CommentListItemNoLikesPreview() {
    AppTheme {
        Surface {
            CommentListItem(
                marker = TimestampMarker(
                    timestampSeconds = 120L,
                    displayTimestamp = "2:00",
                    comment = VideoComment(
                        commentId = "comment_002",
                        authorDisplayName = "新規視聴者",
                        authorProfileImageUrl = "",
                        textContent = "2:00 から見てます！初めて来ました",
                        likeCount = 0,
                        publishedAt = "2024-01-20T15:30:00Z",
                    ),
                ),
                onTimestampClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun CommentListItemLongTextPreview() {
    AppTheme {
        Surface {
            CommentListItem(
                marker = TimestampMarker(
                    timestampSeconds = 3661L,
                    displayTimestamp = "1:01:01",
                    comment = VideoComment(
                        commentId = "comment_003",
                        authorDisplayName = "LongNameUser_ABCDEF",
                        authorProfileImageUrl = "",
                        textContent = "このゲームの最大の見どころは 1:01:01 のところで、ボス戦が始まる瞬間の緊張感がとても良く伝わってきました。" +
                            "特にBGMとの相乗効果が素晴らしく、視聴者を画面に釘付けにする演出が完璧でした。",
                        likeCount = 9999,
                        publishedAt = "2024-01-01T00:00:00Z",
                    ),
                ),
                onTimestampClick = {},
            )
        }
    }
}
