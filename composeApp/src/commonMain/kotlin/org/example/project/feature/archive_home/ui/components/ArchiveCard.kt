@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.feature.archive_home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.core.theme.AppShapes
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Dimensions
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.archive_home.ArchiveItem
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Instant

/**
 * アーカイブカードComponent。
 *
 * サムネイル、チャンネル情報、動画タイトル、配信時間を表示する。
 * isSelected が true の場合、カード枠にプライマリカラーのボーダーとチェックマークを表示する。
 *
 * 4層構造: Container -> Screen -> Content -> Component
 *
 * Epic: Channel Follow & Archive Home (US-3, US-4)
 * Story: US-3 (Archive Home Display), US-4 (Archive Selection)
 */
@Composable
fun ArchiveCard(
    archive: ArchiveItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    val selectedDescription = if (isSelected) "選択済み: ${archive.title}" else archive.title
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = selectedDescription }
            .clickable(onClick = onClick),
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        border = if (isSelected) {
            BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            null
        },
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.sm),
            ) {
                // サムネイル
                AsyncImage(
                    model = archive.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.small),
                    contentScale = ContentScale.Crop,
                )

                Spacer(modifier = Modifier.size(Spacing.sm))

                // チャンネル情報 + 配信時間
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // チャンネルアイコン
                    AsyncImage(
                        model = archive.channelIconUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(Dimensions.avatarSm)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )

                    Spacer(modifier = Modifier.width(Spacing.xs))

                    // チャンネル名
                    Text(
                        text = archive.channelName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(modifier = Modifier.width(Spacing.xs))

                    // 配信時間
                    archive.publishedAt?.let { publishedAt ->
                        val localDateTime = publishedAt.toLocalDateTime(TimeZone.currentSystemDefault())
                        val timeText = "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.size(Spacing.xxs))

                // 動画タイトル
                Text(
                    text = archive.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // 選択中チェックマーク（右上に表示）
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.xs),
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape,
                            ),
                    )
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun ArchiveCardPreview() {
    val mockArchive = ArchiveItem(
        videoId = "video1",
        title = "【APEX】ランクマッチ配信！マスターを目指す！【参加型】",
        thumbnailUrl = "https://example.com/thumb.jpg",
        channelId = "ch1",
        channelName = "ゲーム配信チャンネル",
        channelIconUrl = "https://example.com/icon.jpg",
        serviceType = VideoServiceType.TWITCH,
        publishedAt = Instant.parse("2024-01-15T14:30:00Z"),
        durationSeconds = 7200f,
    )

    AppTheme {
        ArchiveCard(
            archive = mockArchive,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun ArchiveCardYouTubePreview() {
    val mockArchive = ArchiveItem(
        videoId = "video2",
        title = "雑談配信",
        thumbnailUrl = "https://example.com/thumb2.jpg",
        channelId = "ch2",
        channelName = "トークチャンネル",
        channelIconUrl = "https://example.com/icon2.jpg",
        serviceType = VideoServiceType.YOUTUBE,
        publishedAt = Instant.parse("2024-01-15T20:00:00Z"),
        durationSeconds = 3600f,
    )

    AppTheme {
        ArchiveCard(
            archive = mockArchive,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun ArchiveCardSelectedPreview() {
    val mockArchive = ArchiveItem(
        videoId = "video3",
        title = "【APEX】ランクマッチ配信！マスターを目指す！【参加型】",
        thumbnailUrl = "https://example.com/thumb3.jpg",
        channelId = "ch1",
        channelName = "ゲーム配信チャンネル",
        channelIconUrl = "https://example.com/icon1.jpg",
        serviceType = VideoServiceType.TWITCH,
        publishedAt = Instant.parse("2024-01-15T14:30:00Z"),
        durationSeconds = 7200f,
    )

    AppTheme {
        ArchiveCard(
            archive = mockArchive,
            isSelected = true,
            onClick = {},
        )
    }
}
