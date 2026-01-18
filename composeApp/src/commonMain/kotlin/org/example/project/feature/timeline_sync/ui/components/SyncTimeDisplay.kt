@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.core.theme.Spacing

/**
 * Sync time display component showing the current sync time in HH:MM:SS format.
 *
 * When syncTime is null, displays "--:--:--" (initial state before sync time selection).
 * Story 1: Display only. Sync time selection is Story 3.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
 */
@Composable
fun SyncTimeDisplay(
    syncTime: Instant?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            // Label
            Text(
                text = "SYNC TIME",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            // Time display
            Text(
                text = formatSyncTime(syncTime),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

/**
 * Formats the sync time to HH:MM:SS format, or "--:--:--" if null.
 */
private fun formatSyncTime(syncTime: Instant?): String {
    if (syncTime == null) {
        return "--:--:--"
    }

    val localTime = syncTime.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localTime.hour.toString().padStart(2, '0')}:" +
        "${localTime.minute.toString().padStart(2, '0')}:" +
        localTime.second.toString().padStart(2, '0')
}
