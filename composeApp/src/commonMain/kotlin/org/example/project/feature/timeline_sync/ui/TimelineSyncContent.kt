@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.example.project.feature.timeline_sync.TimelineSyncIntent
import org.example.project.feature.timeline_sync.TimelineSyncUiState
import org.example.project.feature.timeline_sync.TimelineSyncViewModel
import org.example.project.feature.timeline_sync.ui.components.ChannelAvatarRow
import org.example.project.feature.timeline_sync.ui.components.SyncTimeDisplay
import org.example.project.feature.timeline_sync.ui.components.TimelineCard
import org.example.project.feature.timeline_sync.ui.components.WeekCalendar

/**
 * Main content area for Timeline Sync screen.
 *
 * Displays calendar, channel avatars, sync time, and timeline cards.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
 */
@Composable
fun TimelineContent(
    uiState: TimelineSyncUiState,
    onIntent: (TimelineSyncIntent) -> Unit,
    currentTime: Instant,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Week Calendar
        item {
            WeekCalendar(
                weekDays = uiState.weekDays,
                selectedDate = uiState.selectedDate,
                onDateSelected = { date ->
                    onIntent(TimelineSyncIntent.SelectDate(date))
                },
                onNavigateToPreviousWeek = {
                    onIntent(TimelineSyncIntent.NavigateToPreviousWeek)
                },
                onNavigateToNextWeek = {
                    onIntent(TimelineSyncIntent.NavigateToNextWeek)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Channel Avatar Row
        item {
            ChannelAvatarRow(
                channels = uiState.channels,
                onAddChannel = {
                    // Story 2: Channel add functionality
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Sync Time Display
        item {
            SyncTimeDisplay(
                syncTime = uiState.syncTime,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Timeline Cards
        items(
            items = uiState.channels.filter { it.selectedStream != null },
            key = { it.channelId },
        ) { channel ->
            val barInfo = TimelineSyncViewModel.calculateTimelineBarInfo(
                channel = channel,
                selectedDate = uiState.selectedDate,
                currentTime = currentTime,
            )

            TimelineCard(
                channel = channel,
                barInfo = barInfo,
                syncTime = uiState.syncTime,
                selectedDate = uiState.selectedDate,
                onOpenClick = {
                    // Story 4: External app navigation
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
        }

        // Bottom spacer for better scrolling
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Empty state content when no channels are registered.
 */
@Composable
fun EmptyContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.VideoLibrary,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "チャンネルがありません",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "チャンネルを追加して\nタイムラインを始めましょう",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                // Story 2: Navigate to channel add
            },
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "チャンネルを追加",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/**
 * Error state content with retry button.
 */
@Composable
fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "エラーが発生しました",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
        ) {
            Text(text = "再試行")
        }
    }
}
