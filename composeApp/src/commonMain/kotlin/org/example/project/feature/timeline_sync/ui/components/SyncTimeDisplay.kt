@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "SYNC TIME",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Time display
            Text(
                text = formatSyncTime(syncTime),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            // Status text
            Text(
                text = if (syncTime != null) {
                    "同期時刻設定済み"
                } else {
                    "同期時刻未設定"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp),
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
