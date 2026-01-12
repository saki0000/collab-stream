package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import org.example.project.feature.timeline_sync.TimelineBarInfo

/**
 * Timeline bar component showing stream duration on a 24-hour axis.
 *
 * Renders a colored bar representing the stream's time range within the selected day.
 * Note: Sync line is now rendered as an overlay by TimelineCardsWithSyncLine.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
 */
@Composable
fun TimelineBar(
    barInfo: TimelineBarInfo,
    platformColor: Color,
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
                .background(MaterialTheme.colorScheme.primaryContainer),
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
    }
}
