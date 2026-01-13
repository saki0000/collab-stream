package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sync line width
 */
private val SyncLineWidth = 2.dp

/**
 * Handle size (triangle)
 */
private val HandleSize = 16.dp

/**
 * Draggable sync line component with triangle handle.
 *
 * This component renders a vertical blue line with a triangle handle at the top.
 * Users can drag the handle horizontally to change the sync time.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-3 (Sync Time Selection)
 *
 * @param modifier Modifier for the component
 * @param onDragStart Callback when drag starts
 * @param onDrag Callback during drag with deltaX in pixels
 * @param onDragEnd Callback when drag ends
 */
@Composable
fun DraggableSyncLine(
    modifier: Modifier = Modifier,
    onDragStart: () -> Unit = {},
    onDrag: (deltaX: Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val syncLineColor = MaterialTheme.colorScheme.primary

        // Triangle handle at the top
        SyncLineHandle(
            size = HandleSize,
            color = syncLineColor,
        )

        // Vertical line (height will be determined by Layout)
        Box(
            modifier = Modifier
                .width(SyncLineWidth)
                .weight(1f)
                .background(syncLineColor),
        )
    }
}

/**
 * Triangle handle for the sync line.
 * Renders a downward-pointing triangle.
 *
 * @param size Size of the handle
 * @param color Color of the handle
 * @param modifier Modifier for the component
 */
@Composable
private fun SyncLineHandle(
    size: Dp,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier.size(size),
    ) {
        val path = Path().apply {
            // Downward-pointing triangle
            moveTo(this@Canvas.size.width / 2, this@Canvas.size.height) // Bottom center
            lineTo(0f, 0f) // Top left
            lineTo(this@Canvas.size.width, 0f) // Top right
            close()
        }
        drawPath(
            path = path,
            color = color,
        )
    }
}
