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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
 * Shows platform icon, channel name, time range, Open/Wait button,
 * and optional comment list button (US-4).
 * This is the fixed (non-scrolling) part of the timeline card.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display), US-3 (Sync Time Selection), US-4 (External App Navigation / コメントリスト)
 *
 * @param channel チャンネル情報
 * @param barInfo タイムラインバー情報
 * @param onOpenClick 外部アプリを開くボタンのコールバック
 * @param showCommentListButton コメントリストボタンを表示するかどうか（LOADED状態かつマーカーあり）
 * @param onCommentListClick コメントリストボタンタップコールバック
 * @param modifier Modifier
 */
@Composable
fun TimelineCardHeader(
    channel: SyncChannel,
    barInfo: TimelineBarInfo?,
    onOpenClick: () -> Unit,
    showCommentListButton: Boolean = false,
    onCommentListClick: () -> Unit = {},
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

        // US-4: コメントリストボタン（LOADED状態かつマーカーありの場合のみ表示）
        if (showCommentListButton) {
            IconButton(
                onClick = onCommentListClick,
                modifier = Modifier.size(Dimensions.icon2xl),
            ) {
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = "コメントリストを開く",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimensions.iconLg),
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
 * Story 4: READY/OPENEDで有効化、タップで外部アプリ起動。
 */
@Composable
private fun OpenWaitButton(
    syncStatus: SyncStatus,
    isUpcoming: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isUpcoming || syncStatus == SyncStatus.WAITING -> {
            OutlinedButton(
                onClick = {},
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
                onClick = onClick,
                modifier = modifier,
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "外部アプリで開く",
                    modifier = Modifier.size(Dimensions.iconXs),
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text("Open")
            }
        }

        syncStatus == SyncStatus.OPENED -> {
            Button(
                onClick = onClick,
                modifier = modifier,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "外部アプリで再度開く",
                    modifier = Modifier.size(Dimensions.iconXs),
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text("Open")
            }
        }

        else -> {
            // NOT_SYNCED - show disabled button
            OutlinedButton(
                onClick = {},
                modifier = modifier,
                enabled = false,
            ) {
                Text("--")
            }
        }
    }
}
