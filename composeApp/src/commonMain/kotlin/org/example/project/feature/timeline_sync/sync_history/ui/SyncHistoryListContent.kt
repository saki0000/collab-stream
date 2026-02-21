@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.feature.timeline_sync.sync_history.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.SavedChannelInfo
import org.example.project.domain.model.SyncHistory
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.model.displayName
import org.example.project.feature.timeline_sync.sync_history.SyncHistoryListIntent
import org.example.project.feature.timeline_sync.sync_history.ui.components.SyncHistoryCard
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 同期履歴一覧のContent層（Stateless）。
 *
 * 履歴リストをLazyColumnで表示する。
 * 相対日時テキストの計算はここで行い（nowを引数で受け取る）、
 * 各カードに渡す。
 *
 * 4層構造: Container -> Screen -> Content -> Component
 *
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 * Story: US-3 (履歴一覧表示)
 *
 * @param histories 表示する履歴リスト
 * @param now 現在時刻（Container層から渡す。Clock使用禁止）
 * @param onIntent Intentを処理するコールバック
 */
@OptIn(ExperimentalTime::class)
@Composable
fun SyncHistoryListContent(
    histories: List<SyncHistory>,
    now: Instant,
    onIntent: (SyncHistoryListIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Spacing.lg,
            vertical = Spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(
            items = histories,
            key = { it.id },
        ) { history ->
            val relativeText = remember(history.lastUsedAt, now) {
                formatRelativeTime(from = history.lastUsedAt, to = now)
            }

            SyncHistoryCard(
                history = history,
                relativeTimeText = relativeText,
                onDeleteClick = {
                    onIntent(SyncHistoryListIntent.ShowDeleteDialog(history.id))
                },
                onRenameClick = {
                    onIntent(
                        SyncHistoryListIntent.ShowRenameDialog(
                            historyId = history.id,
                            currentName = history.displayName,
                        ),
                    )
                },
            )
        }
    }
}

/**
 * 2つのInstant間の差を相対日時テキストに変換する。
 *
 * @param from 基準時刻（lastUsedAt）
 * @param to 現在時刻
 * @return "たった今"、"N分前"、"N時間前"、"N日前"、"N週間前"、"N ヶ月前"、"N年前" 等の文字列
 */
@OptIn(ExperimentalTime::class)
internal fun formatRelativeTime(from: Instant, to: Instant): String {
    val diff = to - from
    val totalSeconds = diff.inWholeSeconds

    // 未来の場合はそのまま
    if (totalSeconds < 0) return "たった今"

    return when {
        totalSeconds < 60 -> "たった今"
        totalSeconds < 60 * 60 -> "${diff.inWholeMinutes}分前"
        totalSeconds < 60 * 60 * 24 -> "${diff.inWholeHours}時間前"
        totalSeconds < 60 * 60 * 24 * 7 -> "${diff.inWholeDays}日前"
        totalSeconds < 60 * 60 * 24 * 30 -> "${diff.inWholeDays / 7}週間前"
        totalSeconds < 60 * 60 * 24 * 365 -> "${diff.inWholeDays / 30}ヶ月前"
        else -> "${diff.inWholeDays / 365}年前"
    }
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun SyncHistoryListContentPreview() {
    val now = Instant.parse("2024-01-15T12:00:00Z")
    val histories = listOf(
        SyncHistory(
            id = "1",
            name = "Apex大会グループ",
            channels = listOf(
                SavedChannelInfo("ch1", "チャンネルA", "", VideoServiceType.TWITCH),
                SavedChannelInfo("ch2", "チャンネルB", "", VideoServiceType.YOUTUBE),
            ),
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            lastUsedAt = Instant.parse("2024-01-12T10:00:00Z"),
            usageCount = 5,
        ),
        SyncHistory(
            id = "2",
            name = null,
            channels = listOf(
                SavedChannelInfo("ch3", "チャンネルC", "", VideoServiceType.TWITCH),
                SavedChannelInfo("ch4", "チャンネルD", "", VideoServiceType.TWITCH),
                SavedChannelInfo("ch5", "チャンネルE", "", VideoServiceType.YOUTUBE),
            ),
            createdAt = Instant.parse("2024-01-05T00:00:00Z"),
            lastUsedAt = Instant.parse("2024-01-08T15:00:00Z"),
            usageCount = 2,
        ),
        SyncHistory(
            id = "3",
            name = "コラボ配信",
            channels = listOf(
                SavedChannelInfo("ch6", "チャンネルF", "", VideoServiceType.YOUTUBE),
                SavedChannelInfo("ch7", "チャンネルG", "", VideoServiceType.TWITCH),
                SavedChannelInfo("ch8", "チャンネルH", "", VideoServiceType.TWITCH),
                SavedChannelInfo("ch9", "チャンネルI", "", VideoServiceType.YOUTUBE),
            ),
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            lastUsedAt = Instant.parse("2024-01-14T20:00:00Z"),
            usageCount = 10,
        ),
    )
    AppTheme {
        SyncHistoryListContent(
            histories = histories,
            now = now,
            onIntent = {},
        )
    }
}
