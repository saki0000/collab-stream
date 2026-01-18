@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.core.theme.Dimensions
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.SyncChannel
import org.example.project.feature.timeline_sync.TimelineBarInfo
import org.example.project.feature.timeline_sync.TimelineSyncIntent
import org.example.project.feature.timeline_sync.TimelineSyncUiState
import org.example.project.feature.timeline_sync.ui.components.ChannelAvatarRow
import org.example.project.feature.timeline_sync.ui.components.SyncTimeDisplay
import org.example.project.feature.timeline_sync.ui.components.TimelineCardsWithSyncLine
import org.example.project.feature.timeline_sync.ui.components.WeekCalendar

/**
 * Main content area for Timeline Sync screen.
 *
 * Displays calendar, channel avatars, sync time, and timeline cards with draggable sync line.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display), US-3 (Sync Time Selection)
 */
@Composable
fun TimelineContent(
    uiState: TimelineSyncUiState,
    onIntent: (TimelineSyncIntent) -> Unit,
    currentTime: Instant,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    // Filter channels with selected streams
    val channelsWithStreams = remember(uiState.channels) {
        uiState.channels.filter { it.selectedStream != null }
    }

    // Calculate bar info for each channel based on syncTimeRange (union of all streams)
    val barInfoMap = remember(channelsWithStreams, uiState.syncTimeRange, currentTime) {
        val range = uiState.syncTimeRange ?: return@remember emptyMap()
        channelsWithStreams.mapNotNull { channel ->
            val barInfo = calculateBarInfoForRange(
                channel = channel,
                timeRange = range,
                currentTime = currentTime,
            )
            barInfo?.let { channel.channelId to it }
        }.toMap()
    }

    Column(
        modifier = modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        // Week Calendar
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

        // Channel Avatar Row
        ChannelAvatarRow(
            channels = uiState.channels,
            onAddChannel = {
                onIntent(TimelineSyncIntent.OpenChannelAddModal)
            },
            canAddChannel = uiState.canAddChannel,
            modifier = Modifier.fillMaxWidth(),
        )

        // Sync Time Display
        SyncTimeDisplay(
            syncTime = uiState.syncTime,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        // Timeline Cards with Sync Line (horizontal scrolling)
        if (channelsWithStreams.isNotEmpty()) {
            TimelineCardsWithSyncLine(
                channels = channelsWithStreams,
                barInfoMap = barInfoMap,
                syncTime = uiState.syncTime,
                syncTimeRange = uiState.syncTimeRange,
                onSyncTimeChange = { newTime ->
                    onIntent(TimelineSyncIntent.UpdateSyncTime(newTime))
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Bottom spacer for better scrolling
        Spacer(modifier = Modifier.height(Spacing.lg))
    }
}

/**
 * Empty state content when no channels are registered.
 */
@Composable
fun EmptyContent(
    onAddChannel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.VideoLibrary,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.icon4xl),
            tint = MaterialTheme.colorScheme.secondary,
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Text(
            text = "チャンネルがありません",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = "チャンネルを追加して\nタイムラインを始めましょう",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        OutlinedButton(
            onClick = onAddChannel,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.iconSm),
            )
            Text(
                text = "チャンネルを追加",
                modifier = Modifier.padding(start = Spacing.sm),
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
        modifier = modifier.padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.icon4xl),
            tint = MaterialTheme.colorScheme.error,
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Text(
            text = "エラーが発生しました",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        Button(
            onClick = onRetry,
        ) {
            Text(text = "再試行")
        }
    }
}

/**
 * Calculates timeline bar info for a channel based on the given time range.
 * The time range represents the union of all streams.
 *
 * @param channel The channel with stream information
 * @param timeRange The total time range (union of all streams)
 * @param currentTime Current time for live stream calculations
 * @return TimelineBarInfo if stream exists, null otherwise
 */
private fun calculateBarInfoForRange(
    channel: SyncChannel,
    timeRange: Pair<Instant, Instant>,
    currentTime: Instant,
): TimelineBarInfo? {
    val stream = channel.selectedStream ?: return null
    val startTime = stream.startTime ?: return null
    val streamEnd = stream.endTime ?: currentTime

    val rangeStart = timeRange.first
    val rangeEnd = timeRange.second
    val rangeDuration = (rangeEnd - rangeStart).inWholeMinutes.toFloat()

    if (rangeDuration <= 0) return null

    // Calculate fractions relative to the time range
    val startFraction = ((startTime - rangeStart).inWholeMinutes.toFloat() / rangeDuration).coerceIn(0f, 1f)
    val endFraction = ((streamEnd - rangeStart).inWholeMinutes.toFloat() / rangeDuration).coerceIn(0f, 1f)

    // Format display times
    val timeZone = TimeZone.currentSystemDefault()
    val startLocal = startTime.toLocalDateTime(timeZone)
    val endLocal = streamEnd.toLocalDateTime(timeZone)
    val displayStartTime = "${startLocal.hour.toString().padStart(2, '0')}:${startLocal.minute.toString().padStart(2, '0')}"
    val displayEndTime = "${endLocal.hour.toString().padStart(2, '0')}:${endLocal.minute.toString().padStart(2, '0')}"

    // Check if upcoming
    val isUpcoming = startTime > currentTime
    val minutesToStart = if (isUpcoming) {
        (startTime - currentTime).inWholeMinutes
    } else {
        null
    }

    return TimelineBarInfo(
        channelId = channel.channelId,
        startFraction = startFraction,
        endFraction = endFraction,
        displayStartTime = displayStartTime,
        displayEndTime = displayEndTime,
        isLive = stream.endTime == null && !isUpcoming,
        isUpcoming = isUpcoming,
        minutesToStart = minutesToStart,
    )
}
