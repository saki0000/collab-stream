package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.core.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Direction indicator for when sync line is out of view.
 *
 * Shows which direction the user should scroll to find the sync line,
 * along with the time distance in minutes.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
 */
@Composable
fun OutOfViewIndicator(
    direction: OutOfViewDirection,
    minutesAway: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            when (direction) {
                OutOfViewDirection.LEFT -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "左にスクロール",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "${minutesAway}分",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                OutOfViewDirection.RIGHT -> {
                    Text(
                        text = "${minutesAway}分",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "右にスクロール",
                        modifier = Modifier.size(20.dp),
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
            minutesAway = 12,
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
            minutesAway = 8,
            onClick = {},
        )
    }
}
