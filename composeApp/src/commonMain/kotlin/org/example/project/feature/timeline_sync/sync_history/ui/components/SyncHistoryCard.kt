@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.feature.timeline_sync.sync_history.ui.components

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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.SavedChannelInfo
import org.example.project.domain.model.SyncHistory
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.model.displayName
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 同期履歴のカードコンポーネント（Component層）。
 *
 * 履歴の表示名、チャンネルアイコン（最大3つ）、最終使用日時（相対表示）、
 * 使用回数を表示する。右端のメニューアイコンから削除・名前変更が可能。
 *
 * 4層構造: Container -> Screen -> Content -> Component
 *
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 * Story: US-3 (履歴一覧表示)
 *
 * @param history 表示する履歴データ
 * @param relativeTimeText 最終使用日時の相対表示テキスト（"3日前" 等）
 * @param onDeleteClick 削除メニュー選択時のコールバック
 * @param onRenameClick 名前変更メニュー選択時のコールバック
 */
@OptIn(ExperimentalTime::class)
@Composable
fun SyncHistoryCard(
    history: SyncHistory,
    relativeTimeText: String,
    onDeleteClick: () -> Unit,
    onRenameClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 左側: チャンネルアイコン群
            ChannelIconGroup(
                channels = history.channels,
                modifier = Modifier.padding(end = Spacing.md),
            )

            // 中央: 表示名と詳細情報
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                // 表示名
                Text(
                    text = history.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )

                // 最終使用日時と使用回数
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Text(
                        text = relativeTimeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${history.usageCount}回使用",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 右端: メニューアイコン
            Box {
                IconButton(
                    onClick = { isMenuExpanded = true },
                    modifier = Modifier.semantics {
                        contentDescription = "${history.displayName}のメニューを開く"
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("名前を変更") },
                        onClick = {
                            isMenuExpanded = false
                            onRenameClick()
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "削除",
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            isMenuExpanded = false
                            onDeleteClick()
                        },
                    )
                }
            }
        }
    }
}

/**
 * チャンネルアイコンを横並びで表示するコンポーネント。
 *
 * 最大3つのアイコンを表示し、残りは "+N" で表示する。
 */
@Composable
private fun ChannelIconGroup(
    channels: List<SavedChannelInfo>,
    modifier: Modifier = Modifier,
) {
    val maxIcons = 3
    val displayedChannels = remember(channels) { channels.take(maxIcons) }
    val remainingCount = remember(channels) { maxOf(0, channels.size - maxIcons) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        displayedChannels.forEach { channel ->
            // チャンネルアイコン（URLがある場合はコイル等でロードする想定、現在はプレースホルダー）
            ChannelIconPlaceholder(channelName = channel.channelName)
        }

        // 残りチャンネル数の表示
        if (remainingCount > 0) {
            Text(
                text = "+$remainingCount",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * チャンネルアイコンのプレースホルダー。
 * アイコンURLが未実装のためPersonアイコンで代替する。
 */
@Composable
private fun ChannelIconPlaceholder(
    channelName: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null, // チャンネル名はカード全体で読み上げられるため装飾アイコン
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun SyncHistoryCardPreview() {
    val history = SyncHistory(
        id = "1",
        name = "Apex大会グループ",
        channels = listOf(
            SavedChannelInfo(
                channelId = "ch1",
                channelName = "チャンネルA",
                channelIconUrl = "",
                serviceType = VideoServiceType.TWITCH,
            ),
            SavedChannelInfo(
                channelId = "ch2",
                channelName = "チャンネルB",
                channelIconUrl = "",
                serviceType = VideoServiceType.YOUTUBE,
            ),
        ),
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        lastUsedAt = Instant.parse("2024-01-15T10:00:00Z"),
        usageCount = 5,
    )
    AppTheme {
        SyncHistoryCard(
            history = history,
            relativeTimeText = "3日前",
            onDeleteClick = {},
            onRenameClick = {},
        )
    }
}

@Preview
@Composable
private fun SyncHistoryCardManyChannelsPreview() {
    val history = SyncHistory(
        id = "2",
        name = null,
        channels = listOf(
            SavedChannelInfo("ch1", "チャンネルA", "", VideoServiceType.TWITCH),
            SavedChannelInfo("ch2", "チャンネルB", "", VideoServiceType.YOUTUBE),
            SavedChannelInfo("ch3", "チャンネルC", "", VideoServiceType.TWITCH),
            SavedChannelInfo("ch4", "チャンネルD", "", VideoServiceType.TWITCH),
        ),
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        lastUsedAt = Instant.parse("2024-01-01T00:00:00Z"),
        usageCount = 1,
    )
    AppTheme {
        SyncHistoryCard(
            history = history,
            relativeTimeText = "1週間前",
            onDeleteClick = {},
            onRenameClick = {},
        )
    }
}
