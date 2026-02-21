package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Header component for Timeline Sync screen.
 *
 * Displays "Timeline Sync" title, active channel count, and save history button.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 * Story: US-2 (履歴保存機能)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineSyncHeader(
    activeChannelCount: Int,
    canSaveHistory: Boolean,
    isSavingHistory: Boolean,
    onSaveHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Timeline Sync",
                    style = MaterialTheme.typography.titleLarge,
                )

                ActiveChannelIndicator(
                    count = activeChannelCount,
                )
            }
        },
        actions = {
            // 保存ボタン: チャンネルが2つ以上かつ保存中でない場合に有効
            if (isSavingHistory) {
                // 保存処理中は CircularProgressIndicator を表示
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(Spacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            } else {
                IconButton(
                    onClick = onSaveHistoryClick,
                    enabled = canSaveHistory,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkAdd,
                        contentDescription = "チャンネル組み合わせを履歴に保存",
                        tint = if (canSaveHistory) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

/**
 * Active channel count indicator with green dot.
 */
@Composable
private fun ActiveChannelIndicator(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(end = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        // 緑のドットインジケーター
        Box(
            modifier = Modifier
                .size(Spacing.sm)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary),
        )

        Text(
            text = "$count CHANNELS ACTIVE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

// ============================================
// Previews
// ============================================

/**
 * 通常状態のプレビュー（保存ボタン有効）。
 */
@Preview
@Composable
private fun TimelineSyncHeaderPreview() {
    AppTheme {
        TimelineSyncHeader(
            activeChannelCount = 2,
            canSaveHistory = true,
            isSavingHistory = false,
            onSaveHistoryClick = {},
        )
    }
}

/**
 * 保存ボタン非活性状態のプレビュー（チャンネル1つ以下）。
 */
@Preview
@Composable
private fun TimelineSyncHeaderDisabledPreview() {
    AppTheme {
        TimelineSyncHeader(
            activeChannelCount = 1,
            canSaveHistory = false,
            isSavingHistory = false,
            onSaveHistoryClick = {},
        )
    }
}

/**
 * 保存処理中のプレビュー（CircularProgressIndicator表示）。
 */
@Preview
@Composable
private fun TimelineSyncHeaderSavingPreview() {
    AppTheme {
        TimelineSyncHeader(
            activeChannelCount = 3,
            canSaveHistory = false,
            isSavingHistory = true,
            onSaveHistoryClick = {},
        )
    }
}

/**
 * チャンネル0件のプレビュー（保存ボタン非活性）。
 */
@Preview
@Composable
private fun TimelineSyncHeaderEmptyPreview() {
    AppTheme {
        TimelineSyncHeader(
            activeChannelCount = 0,
            canSaveHistory = false,
            isSavingHistory = false,
            onSaveHistoryClick = {},
        )
    }
}
