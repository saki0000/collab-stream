@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.example.project.core.theme.AppShapes
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Dimensions
import org.example.project.core.theme.Elevation
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.SelectedStreamInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.SyncStatus
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.timeline_sync.TimelineBarInfo
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Custom Layout Composable that displays timeline cards with a fixed center sync line.
 *
 * The sync line is always fixed at the center of the screen.
 * Timeline bars can be scrolled horizontally while headers stay fixed.
 * The visible window shows ±30 minutes around the sync time.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-3 (Sync Time Selection)
 *
 * @param channels List of channels with selected streams
 * @param barInfoMap Map of channelId to TimelineBarInfo
 * @param syncTime Current sync time
 * @param syncTimeRange Total time range (union of all streams)
 * @param onSyncTimeChange Callback when sync time changes during scroll
 * @param modifier Modifier for the layout
 */
@Composable
fun TimelineCardsWithSyncLine(
    channels: List<SyncChannel>,
    barInfoMap: Map<String, TimelineBarInfo>,
    syncTime: Instant?,
    syncTimeRange: Pair<Instant, Instant>?,
    onSyncTimeChange: (Instant) -> Unit,
    onOpenClick: (channelId: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (syncTimeRange == null) return

    val scrollState = rememberScrollState()

    // Track viewport width
    var viewportWidth by remember { mutableStateOf(0f) }

    // Calculate content width based on time range
    val totalDuration = syncTimeRange.second - syncTimeRange.first
    val contentWidth = remember(viewportWidth, totalDuration) {
        if (viewportWidth > 0) {
            calculateContentWidth(viewportWidth, totalDuration)
        } else {
            viewportWidth
        }
    }

    // Track if we're programmatically scrolling to avoid feedback loop
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // Initial scroll to syncTime position
    LaunchedEffect(syncTime, contentWidth, viewportWidth) {
        if (syncTime != null && contentWidth > 0 && viewportWidth > 0) {
            val targetScroll = calculateScrollPositionForTime(
                syncTime = syncTime,
                timeRange = syncTimeRange,
                contentWidth = contentWidth,
                viewportWidth = viewportWidth,
            )
            if (kotlin.math.abs(scrollState.value - targetScroll) > 10) {
                isProgrammaticScroll = true
                scrollState.scrollTo(targetScroll)
                isProgrammaticScroll = false
            }
        }
    }

    // Update syncTime when user scrolls
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .collectLatest { scrollPosition ->
                if (!isProgrammaticScroll && contentWidth > 0 && viewportWidth > 0) {
                    val newTime = calculateTimeFromScrollPosition(
                        scrollPosition = scrollPosition,
                        timeRange = syncTimeRange,
                        contentWidth = contentWidth,
                        viewportWidth = viewportWidth,
                    )
                    onSyncTimeChange(newTime)
                }
            }
    }

    var containerHeight by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .onSizeChanged { size ->
                viewportWidth = size.width.toFloat()
                containerHeight = size.height
            }
            .scrollable(
                state = scrollState,
                orientation = Orientation.Horizontal,
                reverseDirection = true,
            ),
    ) {
        Column {
            channels.forEach { channel ->
                val barInfo = barInfoMap[channel.channelId]
                if (barInfo != null) {
                    TimelineCardWithScrollableBar(
                        channel = channel,
                        barInfo = barInfo,
                        scrollState = scrollState,
                        contentWidthPx = contentWidth,
                        onOpenClick = { onOpenClick(channel.channelId) },
                    )
                }
            }
        }

        // Sync line overlay (fixed at center) with triangle indicator
        if (syncTime != null && containerHeight > 0) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val heightDp = with(density) { containerHeight.toDp() }
            val triangleSize = Dimensions.iconXs
            val lineWidth = 2.dp

            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Triangle indicator
                Box(
                    modifier = Modifier
                        .width(triangleSize)
                        .height(triangleSize)
                        .drawBehind {
                            val path = Path().apply {
                                moveTo(size.width / 2, size.height)
                                lineTo(0f, 0f)
                                lineTo(size.width, 0f)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Fill,
                            )
                        },
                )

                // Vertical line
                Box(
                    modifier = Modifier
                        .width(lineWidth)
                        .height(heightDp - triangleSize)
                        .background(primaryColor),
                )
            }
        }
    }
}

/**
 * Individual timeline card with fixed header and scrollable timeline bar.
 */
@Composable
private fun TimelineCardWithScrollableBar(
    channel: SyncChannel,
    barInfo: TimelineBarInfo,
    scrollState: androidx.compose.foundation.ScrollState,
    contentWidthPx: Float,
    onOpenClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val platformColor = getPlatformColor(channel.serviceType)
    val coroutineScope = rememberCoroutineScope()

    // Track viewport width for OutOfViewIndicator calculations
    var viewportWidth by remember { mutableStateOf(0f) }

    // Calculate bar positions in pixels
    val barStartPx = barInfo.startFraction * contentWidthPx
    val barEndPx = barInfo.endFraction * contentWidthPx

    // Derive visibility states based on scroll position
    // Bar is completely to the left of viewport (no part visible)
    val isBarCompletelyLeftOfView by remember(contentWidthPx, viewportWidth) {
        derivedStateOf {
            viewportWidth > 0 && barEndPx < scrollState.value
        }
    }

    // Bar is completely to the right of viewport (no part visible)
    val isBarCompletelyRightOfView by remember(contentWidthPx, viewportWidth) {
        derivedStateOf {
            viewportWidth > 0 && barStartPx > (scrollState.value + viewportWidth)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        shape = AppShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
        ) {
            // Fixed header
            TimelineCardHeader(
                channel = channel,
                barInfo = barInfo,
                onOpenClick = onOpenClick,
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Timeline bar area with OutOfViewIndicators overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        viewportWidth = size.width.toFloat()
                    },
            ) {
                // Scrollable timeline bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.iconLg)
                        .clip(AppShapes.small)
                        .horizontalScroll(scrollState),
                ) {
                    // Background track (full content width)
                    val contentWidthDp = with(density) { contentWidthPx.toDp() }

                    Box(
                        modifier = Modifier
                            .width(contentWidthDp)
                            .height(Dimensions.iconLg)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        // Stream duration bar
                        val barWidthFraction = barInfo.endFraction - barInfo.startFraction
                        val barWidthDp = with(density) { (barWidthFraction * contentWidthPx).toDp() }
                        val barOffsetDp = with(density) { (barInfo.startFraction * contentWidthPx).toDp() }

                        val barColor = if (barInfo.isUpcoming) {
                            platformColor.copy(alpha = 0.3f)
                        } else {
                            platformColor
                        }

                        Box(
                            modifier = Modifier
                                .padding(start = barOffsetDp)
                                .width(barWidthDp)
                                .height(Dimensions.iconLg)
                                .clip(AppShapes.small)
                                .then(
                                    if (barInfo.isUpcoming) {
                                        Modifier.drawBehind {
                                            val pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(10f, 10f),
                                                0f,
                                            )
                                            drawLine(
                                                color = platformColor,
                                                start = Offset(0f, size.height / 2),
                                                end = Offset(size.width, size.height / 2),
                                                strokeWidth = Spacing.xs.toPx(),
                                                pathEffect = pathEffect,
                                            )
                                        }
                                    } else {
                                        // Solid background with diagonal stripe pattern
                                        Modifier
                                            .background(barColor)
                                            .drawBehind {
                                                val stripeSpacing = Spacing.md.toPx()
                                                val stripeWidth = 3.dp.toPx()
                                                // Darker shade of bar color for better contrast
                                                val stripeColor = androidx.compose.ui.graphics.Color(
                                                    red = barColor.red * 0.7f,
                                                    green = barColor.green * 0.7f,
                                                    blue = barColor.blue * 0.7f,
                                                    alpha = 1f,
                                                )

                                                // Draw 45-degree diagonal stripes
                                                var x = -size.height
                                                while (x < size.width + size.height) {
                                                    drawLine(
                                                        color = stripeColor,
                                                        start = Offset(x, size.height),
                                                        end = Offset(x + size.height, 0f),
                                                        strokeWidth = stripeWidth,
                                                    )
                                                    x += stripeSpacing
                                                }
                                            }
                                    },
                                ),
                        )
                    }
                }

                // Left OutOfViewIndicator (bar is completely off-screen to the left)
                if (isBarCompletelyLeftOfView) {
                    OutOfViewIndicator(
                        direction = OutOfViewDirection.LEFT,
                        startTime = barInfo.displayStartTime,
                        onClick = {
                            coroutineScope.launch {
                                // Scroll to show bar start at center
                                val targetScroll = (barStartPx - viewportWidth / 2)
                                    .toInt()
                                    .coerceAtLeast(0)
                                scrollState.animateScrollTo(targetScroll)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = Spacing.xs),
                    )
                }

                // Right OutOfViewIndicator (bar is completely off-screen to the right)
                if (isBarCompletelyRightOfView) {
                    OutOfViewIndicator(
                        direction = OutOfViewDirection.RIGHT,
                        startTime = barInfo.displayStartTime,
                        onClick = {
                            coroutineScope.launch {
                                // Scroll to show bar end at center
                                val targetScroll = (barEndPx - viewportWidth / 2)
                                    .toInt()
                                    .coerceAtLeast(0)
                                scrollState.animateScrollTo(targetScroll)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = Spacing.xs),
                    )
                }
            }

            // Upcoming stream info
            if (barInfo.isUpcoming && barInfo.minutesToStart != null) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                UpcomingStreamInfoRow(
                    startTime = barInfo.displayStartTime,
                    minutesToStart = barInfo.minutesToStart,
                )
            }
        }
    }
}

/**
 * Upcoming stream information row.
 */
@Composable
private fun UpcomingStreamInfoRow(
    startTime: String,
    minutesToStart: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Starts $startTime",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${minutesToStart}M TO START",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
    }
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun TimelineCardsWithSyncLinePreview() {
    val now = Instant.parse("2024-01-01T12:00:00Z")
    val mockChannels = listOf(
        SyncChannel(
            channelId = "channel_1",
            channelName = "Gaming Channel",
            channelIconUrl = "",
            serviceType = VideoServiceType.YOUTUBE,
            selectedStream = SelectedStreamInfo(
                id = "stream_1",
                title = "Morning Stream",
                thumbnailUrl = "",
                startTime = Instant.parse("2024-01-01T10:00:00Z"),
                endTime = Instant.parse("2024-01-01T13:00:00Z"),
                duration = null,
            ),
            syncStatus = SyncStatus.READY,
        ),
        SyncChannel(
            channelId = "channel_2",
            channelName = "Esports Pro",
            channelIconUrl = "",
            serviceType = VideoServiceType.TWITCH,
            selectedStream = SelectedStreamInfo(
                id = "stream_2",
                title = "Afternoon Tournament",
                thumbnailUrl = "",
                startTime = Instant.parse("2024-01-01T14:00:00Z"),
                endTime = Instant.parse("2024-01-01T18:00:00Z"),
                duration = null,
            ),
            syncStatus = SyncStatus.READY,
        ),
    )

    val mockBarInfoMap = mapOf(
        "channel_1" to TimelineBarInfo(
            channelId = "channel_1",
            startFraction = 0.0f, // 10:00 relative to 10:00-18:00
            endFraction = 0.375f, // 13:00 relative to 10:00-18:00
            displayStartTime = "10:00",
            displayEndTime = "13:00",
            isLive = false,
            isUpcoming = false,
            minutesToStart = null,
        ),
        "channel_2" to TimelineBarInfo(
            channelId = "channel_2",
            startFraction = 0.5f, // 14:00 relative to 10:00-18:00
            endFraction = 1.0f, // 18:00 relative to 10:00-18:00
            displayStartTime = "14:00",
            displayEndTime = "18:00",
            isLive = false,
            isUpcoming = false,
            minutesToStart = null,
        ),
    )

    // Time range: 10:00 to 18:00 (8 hours)
    val timeRange = Instant.parse("2024-01-01T10:00:00Z") to Instant.parse("2024-01-01T18:00:00Z")

    AppTheme {
        TimelineCardsWithSyncLine(
            channels = mockChannels,
            barInfoMap = mockBarInfoMap,
            syncTime = now,
            syncTimeRange = timeRange,
            onSyncTimeChange = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
