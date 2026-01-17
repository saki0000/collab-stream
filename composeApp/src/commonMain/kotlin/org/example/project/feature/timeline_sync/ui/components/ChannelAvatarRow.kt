@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.time.ExperimentalTime
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.VideoServiceType

/**
 * Horizontal scrolling row of channel avatars with platform badges.
 *
 * Displays channel avatars with platform-specific badges (YouTube red, Twitch purple).
 * Includes an "Add" button at the end.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display), US-2 (Channel Add/Remove)
 */
@Composable
fun ChannelAvatarRow(
    channels: List<SyncChannel>,
    onAddChannel: () -> Unit,
    canAddChannel: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Channel avatars
        channels.forEach { channel ->
            ChannelAvatar(
                channel = channel,
            )
        }

        // Add button
        AddChannelButton(
            onClick = onAddChannel,
            enabled = canAddChannel,
        )
    }
}

/**
 * Individual channel avatar with platform badge.
 */
@Composable
private fun ChannelAvatar(
    channel: SyncChannel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            // Avatar circle
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape,
                    ),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                // Placeholder icon (in real app, load image from channelIconUrl)
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = channel.channelName,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(32.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }

            // Platform badge
            PlatformBadge(
                serviceType = channel.serviceType,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp),
            )
        }

        // Channel name
        Text(
            text = channel.channelName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/**
 * Platform badge showing YouTube (red) or Twitch (purple).
 */
@Composable
private fun PlatformBadge(
    serviceType: VideoServiceType,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = getPlatformColor(serviceType)

    val text = when (serviceType) {
        VideoServiceType.YOUTUBE -> "YT"
        VideoServiceType.TWITCH -> "TW"
    }

    Surface(
        modifier = modifier.size(20.dp),
        shape = CircleShape,
        color = backgroundColor,
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

/**
 * Add channel button.
 * Enabled when canAddChannel is true (less than 10 channels).
 */
@Composable
private fun AddChannelButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    },
                    shape = CircleShape,
                ),
            color = MaterialTheme.colorScheme.surface,
        ) {
            IconButton(
                onClick = onClick,
                enabled = enabled,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add channel",
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                )
            }
        }

        Text(
            text = "+ Add",
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
