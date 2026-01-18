package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.core.theme.AppShapes
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Dimensions
import org.example.project.core.theme.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Direction indicator for when archive bar is completely out of view.
 *
 * Shows the stream start time with a play icon and direction arrow
 * to indicate where the archive is located.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
 */
@Composable
fun OutOfViewIndicator(
    direction: OutOfViewDirection,
    startTime: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = AppShapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            when (direction) {
                OutOfViewDirection.LEFT -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "左にスクロール",
                        modifier = Modifier.size(Dimensions.iconSm),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = startTime,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconSm),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                OutOfViewDirection.RIGHT -> {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconSm),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = startTime,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "右にスクロール",
                        modifier = Modifier.size(Dimensions.iconSm),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

/**
 * Direction enum for out-of-view indicator.
 */
enum class OutOfViewDirection {
    LEFT,
    RIGHT,
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun OutOfViewIndicatorLeftPreview() {
    AppTheme {
        OutOfViewIndicator(
            direction = OutOfViewDirection.LEFT,
            startTime = "18:00",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun OutOfViewIndicatorRightPreview() {
    AppTheme {
        OutOfViewIndicator(
            direction = OutOfViewDirection.RIGHT,
            startTime = "20:30",
            onClick = {},
        )
    }
}
