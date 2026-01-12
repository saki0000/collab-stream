@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.example.project.feature.timeline_sync.TimelineBarInfo

/**
 * Default visible duration (±30 minutes = 60 minutes total)
 */
val DEFAULT_VISIBLE_DURATION = 60.minutes

/**
 * Timeline bar that can be positioned within a scrollable container.
 *
 * This component renders a timeline bar at the correct position based on the
 * time range. It's designed to be used inside a horizontally scrollable container
 * where all timeline bars share the same scroll state.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-3 (Sync Time Selection)
 *
 * @param barInfo Timeline bar information
 * @param platformColor Color for the platform (YouTube red, Twitch purple)
 * @param timeRange Total time range for the scrollable area (union of all streams)
 * @param contentWidthDp Total width of the scrollable content in dp
 * @param modifier Modifier for the component
 * @param barHeight Height of the timeline bar
 */
@Composable
fun ScrollableTimelineBar(
    barInfo: TimelineBarInfo,
    platformColor: Color,
    timeRange: Pair<Instant, Instant>,
    contentWidthDp: Dp,
    modifier: Modifier = Modifier,
    barHeight: Dp = 24.dp,
) {
    val density = LocalDensity.current

    // Calculate bar position and width based on the time range
    val totalDuration = timeRange.second - timeRange.first
    val totalDurationMinutes = totalDuration.inWholeMinutes.toFloat()

    if (totalDurationMinutes <= 0) return

    // Bar start/end relative to the time range
    val barStartFraction = barInfo.startFraction
    val barEndFraction = barInfo.endFraction
    val barWidth = (barEndFraction - barStartFraction) * contentWidthDp.value

    // Bar x position
    val barX = barStartFraction * contentWidthDp.value

    Box(
        modifier = modifier
            .width(contentWidthDp)
            .height(barHeight),
    ) {
        // Background track
        Box(
            modifier = Modifier
                .width(contentWidthDp)
                .height(barHeight)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
        )

        // Stream duration bar
        val barColor = if (barInfo.isUpcoming) {
            platformColor.copy(alpha = 0.3f)
        } else {
            platformColor
        }

        with(density) {
            Box(
                modifier = Modifier
                    .width(barWidth.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .then(
                        if (barInfo.isUpcoming) {
                            Modifier.drawBehind {
                                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                drawLine(
                                    color = platformColor,
                                    start = Offset(0f, size.height / 2),
                                    end = Offset(size.width, size.height / 2),
                                    strokeWidth = 4.dp.toPx(),
                                    pathEffect = pathEffect,
                                )
                            }
                        } else {
                            Modifier.background(barColor)
                        },
                    ),
            )
        }
    }
}

/**
 * Calculates the content width needed to display the full time range
 * with a given visible duration.
 *
 * @param viewportWidth Width of the viewport (visible area)
 * @param totalDuration Total duration of the time range
 * @param visibleDuration Duration that should be visible at once (default 60 minutes)
 * @return Content width in the same unit as viewportWidth
 */
fun calculateContentWidth(
    viewportWidth: Float,
    totalDuration: Duration,
    visibleDuration: Duration = DEFAULT_VISIBLE_DURATION,
): Float {
    val totalMinutes = totalDuration.inWholeMinutes.toFloat()
    val visibleMinutes = visibleDuration.inWholeMinutes.toFloat()
    if (visibleMinutes <= 0) return viewportWidth
    return viewportWidth * (totalMinutes / visibleMinutes)
}

/**
 * Calculates the scroll position to center the given time in the viewport.
 *
 * @param syncTime The time to center
 * @param timeRange Total time range
 * @param contentWidth Total content width
 * @param viewportWidth Viewport width
 * @return Scroll position in pixels
 */
fun calculateScrollPositionForTime(
    syncTime: Instant,
    timeRange: Pair<Instant, Instant>,
    contentWidth: Float,
    viewportWidth: Float,
): Int {
    val totalDuration = timeRange.second - timeRange.first
    if (totalDuration.inWholeMilliseconds <= 0) return 0

    val fraction = (
        (syncTime - timeRange.first).inWholeMilliseconds.toFloat() /
            totalDuration.inWholeMilliseconds.toFloat()
        ).coerceIn(0f, 1f)

    val centerPosition = fraction * contentWidth
    return (centerPosition - viewportWidth / 2).toInt().coerceAtLeast(0)
}

/**
 * Calculates the time at the center of the viewport based on scroll position.
 *
 * @param scrollPosition Current scroll position
 * @param timeRange Total time range
 * @param contentWidth Total content width
 * @param viewportWidth Viewport width
 * @return Time at the center of the viewport
 */
fun calculateTimeFromScrollPosition(
    scrollPosition: Int,
    timeRange: Pair<Instant, Instant>,
    contentWidth: Float,
    viewportWidth: Float,
): Instant {
    val centerPosition = scrollPosition + viewportWidth / 2
    val fraction = (centerPosition / contentWidth).coerceIn(0f, 1f)

    val totalDuration = timeRange.second - timeRange.first
    return timeRange.first + totalDuration * fraction.toDouble()
}
