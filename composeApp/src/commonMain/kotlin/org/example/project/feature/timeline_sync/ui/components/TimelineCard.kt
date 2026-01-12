@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.timeline_sync.TimelineBarInfo

/**
 * Timeline card component displaying channel information and timeline bar.
 *
 * Shows channel name, time range, Open/Wait button, and visual timeline bar.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
 */
@Composable
fun TimelineCard(
    channel: SyncChannel,
    barInfo: TimelineBarInfo?,
    syncTime: Instant?,
    selectedDate: LocalDate,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val platformColor = when (channel.serviceType) {
        VideoServiceType.YOUTUBE -> Color(0xFFFF0000)
        VideoServiceType.TWITCH -> Color(0xFF9146FF)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // Header: Platform icon + Channel name + Time range + Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Platform icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(platformColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = when (channel.serviceType) {
                            VideoServiceType.YOUTUBE -> "YT"
                            VideoServiceType.TWITCH -> "TW"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Channel info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel.channelName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (barInfo != null) {
                        Text(
                            text = "${barInfo.displayStartTime} - ${barInfo.displayEndTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Open/Wait button
                OpenWaitButton(
                    syncStatus = channel.syncStatus,
                    isUpcoming = barInfo?.isUpcoming == true,
                    onClick = onOpenClick,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timeline bar
            if (barInfo != null) {
                TimelineBar(
                    barInfo = barInfo,
                    platformColor = platformColor,
                    syncTime = syncTime,
                    selectedDate = selectedDate,
                )
            }

            // Upcoming stream info
            if (barInfo?.isUpcoming == true && barInfo.minutesToStart != null) {
                Spacer(modifier = Modifier.height(8.dp))
                UpcomingStreamInfo(
                    startTime = barInfo.displayStartTime,
                    minutesToStart = barInfo.minutesToStart,
                )
            }
        }
    }
}

/**
 * Open/Wait button based on sync status.
 */
@Composable
private fun OpenWaitButton(
    syncStatus: SyncStatus,
    isUpcoming: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Story 1: Display only, button is non-functional
    when {
        isUpcoming || syncStatus == SyncStatus.WAITING -> {
            OutlinedButton(
                onClick = { /* Story 4 */ },
                modifier = modifier,
                enabled = false,
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Wait")
            }
        }

        syncStatus == SyncStatus.READY -> {
            Button(
                onClick = { /* Story 4 */ },
                modifier = modifier,
                enabled = false, // Story 1: Display only
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open")
            }
        }

        else -> {
            // NOT_SYNCED or OPENED - show disabled button
            OutlinedButton(
                onClick = { /* Story 4 */ },
                modifier = modifier,
                enabled = false,
            ) {
                Text("--")
            }
        }
    }
}

/**
 * Upcoming stream information display.
 */
@Composable
private fun UpcomingStreamInfo(
    startTime: String,
    minutesToStart: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Starts $startTime",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${minutesToStart}M TO START",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}
