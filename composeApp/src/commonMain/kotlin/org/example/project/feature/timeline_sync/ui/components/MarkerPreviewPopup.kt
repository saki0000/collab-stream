package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Dimensions
import org.example.project.core.theme.Elevation
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.TimestampMarker
import org.example.project.domain.model.VideoComment
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * タイムスタンプマーカーのコメントプレビューを表示するポップアップ。
 *
 * マーカータップ時に表示され、以下の情報を提供する:
 * - タイムスタンプ（例: 30:00）
 * - 著者名
 * - コメントテキスト（最大2行）
 * - いいね数
 *
 * 画面外へのタップで自動的に閉じる。
 *
 * Epic: コメントタイムスタンプ同期
 * Story: US-3 (タイムスタンプマーカープレビュー)
 *
 * @param marker 表示するタイムスタンプマーカー
 * @param onDismiss ポップアップを閉じる際のコールバック
 * @param modifier Modifier
 */
@Composable
fun MarkerPreviewPopup(
    marker: TimestampMarker,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnClickOutside = true,
        ),
    ) {
        MarkerPreviewCard(
            marker = marker,
            onDismiss = onDismiss,
            modifier = modifier,
        )
    }
}

/**
 * マーカープレビューカード（ポップアップのコンテンツ部分）。
 *
 * Preview での確認のために Popup と分離している。
 *
 * @param marker 表示するタイムスタンプマーカー
 * @param onDismiss 閉じるボタン押下時のコールバック
 * @param modifier Modifier
 */
@Composable
fun MarkerPreviewCard(
    marker: TimestampMarker,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .widthIn(min = 240.dp, max = 320.dp)
            .padding(Spacing.md),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.high),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // タイムスタンプ行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = marker.displayTimestamp,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                TextButton(
                    onClick = onDismiss,
                ) {
                    Text(
                        text = "閉じる",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 著者名
            Text(
                text = marker.comment.authorDisplayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // コメントテキスト（最大2行）
            Text(
                text = marker.comment.textContent,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            // いいね数
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = null, // テキストで説明されるため装飾アイコン
                    modifier = Modifier.size(Dimensions.iconXs),
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = formatLikeCount(marker.comment.likeCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

/**
 * いいね数を表示用にフォーマットする。
 * 1000以上の場合は "1.2k" 形式で短縮する。
 *
 * @param likeCount いいね数
 * @return フォーマット済みの文字列
 */
private fun formatLikeCount(likeCount: Int): String {
    return when {
        likeCount >= 1_000_000 -> "${likeCount / 1_000_000}M"
        likeCount >= 1_000 -> "${(likeCount / 100) / 10.0}k".replace(".0k", "k")
        else -> likeCount.toString()
    }
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun MarkerPreviewCardPreview() {
    val mockMarker = TimestampMarker(
        timestampSeconds = 1800L,
        displayTimestamp = "30:00",
        comment = VideoComment(
            commentId = "c1",
            authorDisplayName = "ユーザーA",
            authorProfileImageUrl = "",
            textContent = "30:00 ここが一番盛り上がる場面！みんなもそう思うよね",
            likeCount = 1234,
            publishedAt = "2024-01-01T10:00:00Z",
        ),
    )

    AppTheme {
        Surface {
            MarkerPreviewCard(
                marker = mockMarker,
                onDismiss = {},
            )
        }
    }
}

@Preview
@Composable
private fun MarkerPreviewCardHighLikeCountPreview() {
    val mockMarker = TimestampMarker(
        timestampSeconds = 3600L,
        displayTimestamp = "1:00:00",
        comment = VideoComment(
            commentId = "c2",
            authorDisplayName = "有名コメンター",
            authorProfileImageUrl = "",
            textContent = "1:00:00 最高のシーン",
            likeCount = 12345,
            publishedAt = "2024-01-01T10:00:00Z",
        ),
    )

    AppTheme {
        Surface {
            MarkerPreviewCard(
                marker = mockMarker,
                onDismiss = {},
            )
        }
    }
}

@Preview
@Composable
private fun MarkerPreviewCardLongTextPreview() {
    val mockMarker = TimestampMarker(
        timestampSeconds = 600L,
        displayTimestamp = "10:00",
        comment = VideoComment(
            commentId = "c3",
            authorDisplayName = "非常に長い名前のユーザーアカウント名前太郎",
            authorProfileImageUrl = "",
            textContent = "10:00 とても長いコメントのテキストがここに入ります。2行を超える場合は省略されます。さらに長くなってもこれ以上は表示されません。",
            likeCount = 99,
            publishedAt = "2024-01-01T10:00:00Z",
        ),
    )

    AppTheme {
        Surface {
            MarkerPreviewCard(
                marker = mockMarker,
                onDismiss = {},
            )
        }
    }
}
