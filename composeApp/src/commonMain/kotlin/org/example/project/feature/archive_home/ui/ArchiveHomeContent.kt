@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.feature.archive_home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.archive_home.ArchiveItem
import org.example.project.feature.archive_home.ui.components.ArchiveCard
import org.example.project.feature.timeline_sync.ui.components.WeekCalendar
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Instant

/**
 * アーカイブHome画面のContent層（Stateless）。
 *
 * WeekCalendar + アーカイブカードリストを表示する。
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
    selectedDate: LocalDate,
    displayedWeekStart: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onNavigateToPreviousWeek: () -> Unit,
    onNavigateToNextWeek: () -> Unit,
    modifier: Modifier = Modifier,
    selectedArchiveIds: Set<String> = emptySet(),
    onToggleSelection: (String) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        // 週カレンダー（WeekCalendar再利用）
        val weekDays = (0..6).map { displayedWeekStart.plus(it, DateTimeUnit.DAY) }
        WeekCalendar(
            weekDays = weekDays,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            onNavigateToPreviousWeek = onNavigateToPreviousWeek,
            onNavigateToNextWeek = onNavigateToNextWeek,
            modifier = Modifier.fillMaxWidth(),
        )

        // アーカイブカードリスト
        LazyColumn(
            modifier = Modifier
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
    val today = LocalDate.parse("2024-01-15")

    AppTheme {
        ArchiveHomeContent(
            archives = previewArchives,
            selectedDate = today,
            displayedWeekStart = today,
            onDateSelected = {},
            onNavigateToPreviousWeek = {},
            onNavigateToNextWeek = {},
        )
    }
}

@Preview
@Composable
private fun ArchiveHomeContentSelectedPreview() {
    val today = LocalDate.parse("2024-01-15")

    AppTheme {
        ArchiveHomeContent(
            archives = previewArchives,
            selectedDate = today,
            displayedWeekStart = today,
            selectedArchiveIds = setOf("video1"),
            onDateSelected = {},
            onNavigateToPreviousWeek = {},
            onNavigateToNextWeek = {},
            onToggleSelection = {},
        )
    }
}

@Preview
@Composable
private fun ArchiveHomeContentEmptyPreview() {
    val today = LocalDate.parse("2024-01-15")

    AppTheme {
        ArchiveHomeContent(
            archives = emptyList(),
            selectedDate = today,
            displayedWeekStart = today,
            onDateSelected = {},
            onNavigateToPreviousWeek = {},
            onNavigateToNextWeek = {},
        )
    }
}
