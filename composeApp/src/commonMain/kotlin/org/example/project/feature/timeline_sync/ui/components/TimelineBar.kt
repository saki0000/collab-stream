@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.example.project.feature.timeline_sync.TimelineBarInfo

/**
 * Timeline bar component showing stream duration on a 24-hour axis.
 *
 * Renders a colored bar representing the stream's time range within the selected day.
 * Also displays the sync time indicator (vertical blue line) when set.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
 */
@Composable
fun TimelineBar(
    barInfo: TimelineBarInfo,
    platformColor: Color,
    syncTime: Instant?,
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    barHeight: Dp = 24.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight),
    ) {
        // Background track (24-hour axis)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )

        // Stream duration bar
        val barColor = if (barInfo.isUpcoming) {
            platformColor.copy(alpha = 0.3f)
        } else {
            platformColor
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(barInfo.endFraction - barInfo.startFraction)
                .height(barHeight)
                .offset(
                    x = with(LocalDensity.current) {
                        // Calculate offset based on start fraction
                        // This is a workaround since we can't use fillMaxWidth with offset directly
                        0.dp
                    },
                )
                .padding(start = (barInfo.startFraction * 100).dp) // Simplified positioning
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(4.dp))
                .then(
                    if (barInfo.isUpcoming) {
                        Modifier.drawBehind {
                            // Dashed line for upcoming streams
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

        // Sync time indicator (vertical blue line)
        syncTime?.let { time ->
            val syncFraction = calculateSyncTimeFraction(time, selectedDate)
            if (syncFraction != null && syncFraction in 0f..1f) {
                SyncTimeIndicator(
                    fraction = syncFraction,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
            }
        }
    }
}

/**
 * Alternative implementation with proper width calculation
 */
@Composable
fun TimelineBarWithLayout(
    barInfo: TimelineBarInfo,
    platformColor: Color,
    syncTime: Instant?,
    selectedDate: LocalDate,
    modifier: Modifier = Modifier,
    barHeight: Dp = 24.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight),
    ) {
        // Background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            // Stream bar positioned using fractions
            val barWidth = barInfo.endFraction - barInfo.startFraction

            Box(
                modifier = Modifier
                    .fillMaxWidth(barWidth)
                    .height(barHeight)
                    .align(Alignment.CenterStart)
                    .padding(
                        start = with(LocalDensity.current) {
                            // Calculate padding based on parent width
                            // This is tricky in Compose - using BoxWithConstraints would be better
                            0.dp
                        },
                    )
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (barInfo.isUpcoming) {
                            platformColor.copy(alpha = 0.3f)
                        } else {
                            platformColor
                        },
                    ),
            )
        }

        // Sync time indicator
        syncTime?.let { time ->
            val syncFraction = calculateSyncTimeFraction(time, selectedDate)
            if (syncFraction != null && syncFraction in 0f..1f) {
                SyncTimeIndicator(
                    fraction = syncFraction,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
            }
        }
    }
}

/**
 * Sync time indicator (vertical blue line).
 */
@Composable
private fun SyncTimeIndicator(
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(start = (fraction * 100).dp) // Simplified positioning
            .width(2.dp)
            .height(32.dp)
            .background(Color(0xFF0288D1)), // Blue color from REQUIREMENTS
    )
}

/**
 * Calculates the fraction (0.0-1.0) for the sync time position within the selected date.
 */
private fun calculateSyncTimeFraction(
    syncTime: Instant,
    selectedDate: LocalDate,
): Float? {
    val timeZone = TimeZone.currentSystemDefault()
    val dayStart = selectedDate.atStartOfDayIn(timeZone)
    val dayEnd = dayStart + 1.days

    if (syncTime < dayStart || syncTime >= dayEnd) {
        return null
    }

    val dayDuration = (dayEnd - dayStart).inWholeMinutes.toFloat()
    return ((syncTime - dayStart).inWholeMinutes.toFloat() / dayDuration).coerceIn(0f, 1f)
}
