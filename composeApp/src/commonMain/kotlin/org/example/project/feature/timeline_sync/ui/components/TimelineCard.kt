package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import org.example.project.core.theme.Dimensions
import org.example.project.core.theme.Spacing
import org.example.project.core.theme.TwitchPurple
import org.example.project.core.theme.YouTubeRed
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.timeline_sync.TimelineBarInfo

/**
 * Returns the platform color for a given service type.
 */
fun getPlatformColor(serviceType: VideoServiceType): Color = when (serviceType) {
    VideoServiceType.YOUTUBE -> YouTubeRed
    VideoServiceType.TWITCH -> TwitchPurple
}

/**
 * Timeline card header component displaying channel information.
 *
 * Shows platform icon, channel name, time range, and Open/Wait button.
 * This is the fixed (non-scrolling) part of the timeline card.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display), US-3 (Sync Time Selection)
 */
@Composable
fun TimelineCardHeader(
    channel: SyncChannel,
    barInfo: TimelineBarInfo?,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val platformColor = getPlatformColor(channel.serviceType)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Platform icon
        Box(
            modifier = Modifier
                .size(Dimensions.iconXl)
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

        Spacer(modifier = Modifier.width(Spacing.md))

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
                    color = MaterialTheme.colorScheme.secondary,
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
                    modifier = Modifier.size(Dimensions.iconXs),
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
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
                    modifier = Modifier.size(Dimensions.iconXs),
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
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
