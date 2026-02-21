@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.feature.archive_home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.archive_home.ArchiveItem
import org.example.project.feature.archive_home.ui.components.ArchiveCard
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Instant

/**
 * アーカイブHome画面のContent層（Stateless）。
 *
 * アーカイブカードリストを表示する。
 * WeekCalendarはScreen層に移動済みのため、このContentはカードリストのみを担当する。
 * US-4: カードタップで選択トグル。選択状態はSelectedArchiveIdsで管理。
 *
 * 4層構造: Container -> Screen -> Content -> Component
 *
 * Epic: Channel Follow & Archive Home (US-3, US-4)
 * Story: US-3 (Archive Home Display), US-4 (Archive Selection)
 */
@Composable
fun ArchiveHomeContent(
    archives: List<ArchiveItem>,
    modifier: Modifier = Modifier,
    selectedArchiveIds: Set<String> = emptySet(),
    onToggleSelection: (String) -> Unit = {},
) {
    // アーカイブカードリスト
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(archives, key = { it.videoId }) { archive ->
            ArchiveCard(
                archive = archive,
                isSelected = archive.videoId in selectedArchiveIds,
                onClick = { onToggleSelection(archive.videoId) },
            )
        }
    }
}

// ============================================
// Previews
// ============================================

private val previewArchives = listOf(
    ArchiveItem(
        videoId = "video1",
        title = "配信タイトル1",
        thumbnailUrl = "https://example.com/thumb1.jpg",
        channelId = "ch1",
        channelName = "チャンネル1",
        channelIconUrl = "https://example.com/icon1.jpg",
        serviceType = VideoServiceType.TWITCH,
        publishedAt = Instant.parse("2024-01-15T10:00:00Z"),
        durationSeconds = 7200f,
    ),
    ArchiveItem(
        videoId = "video2",
        title = "配信タイトル2",
        thumbnailUrl = "https://example.com/thumb2.jpg",
        channelId = "ch2",
        channelName = "チャンネル2",
        channelIconUrl = "https://example.com/icon2.jpg",
        serviceType = VideoServiceType.YOUTUBE,
        publishedAt = Instant.parse("2024-01-15T14:00:00Z"),
        durationSeconds = 5400f,
    ),
)

@Preview
@Composable
private fun ArchiveHomeContentPreview() {
    AppTheme {
        ArchiveHomeContent(
            archives = previewArchives,
        )
    }
}

@Preview
@Composable
private fun ArchiveHomeContentSelectedPreview() {
    AppTheme {
        ArchiveHomeContent(
            archives = previewArchives,
            selectedArchiveIds = setOf("video1"),
            onToggleSelection = {},
        )
    }
}

@Preview
@Composable
private fun ArchiveHomeContentEmptyPreview() {
    AppTheme {
        ArchiveHomeContent(
            archives = emptyList(),
        )
    }
}
