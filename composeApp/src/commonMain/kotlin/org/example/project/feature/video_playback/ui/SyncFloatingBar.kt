package org.example.project.feature.video_playback.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Sync Floating Bar - Floating Card for sync controls
 * Displays: Sync time | Sync status | Sync button
 * Styled like BottomNavigation with rounded top corners
 */
@OptIn(ExperimentalTime::class)
@Composable
fun SyncFloatingBar(
    absoluteTime: Instant?,
    syncedCount: Int,
    totalSubCount: Int,
    isSyncing: Boolean,
    onSyncAll: (onGetCurrentTime: (Float) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: Time and Status
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                // Sync Time
                if (absoluteTime != null) {
                    Text(
                        text = formatAbsoluteTime(absoluteTime),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                } else {
                    Text(
                        text = "Not synced",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    )
                }

                // Sync Status
                if (totalSubCount > 0) {
                    Text(
                        text = "$syncedCount/$totalSubCount",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }

            // Right: Sync All Button
            if (totalSubCount > 0) {
                Button(
                    onClick = {
                        onSyncAll { currentTime ->
                            // Callback will be invoked with current playback position
                        }
                    },
                    enabled = !isSyncing,
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync All")
                }
            }
        }
    }
}

/**
 * Formats absolute time to readable date-time string (compact format)
 */
@OptIn(ExperimentalTime::class)
private fun formatAbsoluteTime(absoluteTime: Instant): String {
    val localDateTime = absoluteTime.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.date} ${localDateTime.hour.toString().padStart(
        2,
        '0',
    )}:${localDateTime.minute.toString().padStart(2, '0')}:${localDateTime.second.toString().padStart(2, '0')}"
}
