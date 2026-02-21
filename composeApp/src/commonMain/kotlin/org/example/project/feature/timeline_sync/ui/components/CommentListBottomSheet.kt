package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Dimensions
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.TimestampMarker
import org.example.project.domain.model.VideoComment
import org.example.project.feature.timeline_sync.ChannelCommentState
import org.example.project.feature.timeline_sync.CommentLoadStatus
import org.example.project.feature.timeline_sync.CommentSortOrder
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * コメントリスト BottomSheet コンポーネント（Container 役割）。
 *
 * ModalBottomSheet で表示し、以下の構成を持つ:
 * - ヘッダー: チャンネル名 + ソート切替トグル
 * - コンテンツ: LazyColumn でコメントリスト
 * - フッター: ページネーションローディング
 *
 * 状態: Loading / Content / Empty / Error / Disabled
 *
 * US-4: コメントリスト機能
 *
 * @param channelName 表示するチャンネル名
 * @param commentState チャンネルのコメント状態
 * @param sortOrder 現在のソート順
 * @param isLoadingMore 追加読み込み中かどうか
 * @param onSortOrderChange ソート順変更コールバック
 * @param onTimestampClick タイムスタンプタップコールバック（秒数を渡す）
 * @param onLoadMore 追加読み込みコールバック（スクロール末尾到達時）
 * @param onDismiss BottomSheet を閉じるコールバック
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentListBottomSheet(
    channelName: String,
    commentState: ChannelCommentState,
    sortOrder: CommentSortOrder,
    isLoadingMore: Boolean,
    onSortOrderChange: (CommentSortOrder) -> Unit,
    onTimestampClick: (timestampSeconds: Long) -> Unit,
    onLoadMore: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        CommentListBottomSheetContent(
            channelName = channelName,
            commentState = commentState,
            sortOrder = sortOrder,
            isLoadingMore = isLoadingMore,
            onSortOrderChange = onSortOrderChange,
            onTimestampClick = onTimestampClick,
            onLoadMore = onLoadMore,
        )
    }
}

/**
 * コメントリスト BottomSheet の内部コンテンツ（Content 層）。
 *
 * BottomSheet のスクロール可能コンテンツ部分を担当する。
 * CommentLoadStatus に応じて適切な UI を表示する。
 *
 * @param channelName 表示するチャンネル名
 * @param commentState チャンネルのコメント状態
 * @param sortOrder 現在のソート順
 * @param isLoadingMore 追加読み込み中かどうか
 * @param onSortOrderChange ソート順変更コールバック
 * @param onTimestampClick タイムスタンプタップコールバック（秒数を渡す）
 * @param onLoadMore 追加読み込みコールバック
 */
@Composable
fun CommentListBottomSheetContent(
    channelName: String,
    commentState: ChannelCommentState,
    sortOrder: CommentSortOrder,
    isLoadingMore: Boolean,
    onSortOrderChange: (CommentSortOrder) -> Unit,
    onTimestampClick: (timestampSeconds: Long) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // ヘッダー: チャンネル名 + ソート切替
        CommentListHeader(
            channelName = channelName,
            sortOrder = sortOrder,
            onSortOrderChange = onSortOrderChange,
            isEnabled = commentState.status == CommentLoadStatus.LOADED,
        )

        HorizontalDivider()

        // コンテンツ部分: 状態に応じて表示を切り替え
        when (commentState.status) {
            CommentLoadStatus.LOADING -> {
                CommentListLoadingState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.icon4xl * 3),
                )
            }

            CommentLoadStatus.LOADED -> {
                val sortedMarkers = remember(commentState.markers, sortOrder) {
                    sortMarkers(commentState.markers, sortOrder)
                }

                if (sortedMarkers.isEmpty()) {
                    CommentListEmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimensions.icon4xl * 3),
                    )
                } else {
                    CommentListContentState(
                        markers = sortedMarkers,
                        isLoadingMore = isLoadingMore,
                        hasNextPage = commentState.nextPageToken != null,
                        onTimestampClick = onTimestampClick,
                        onLoadMore = onLoadMore,
                    )
                }
            }

            CommentLoadStatus.DISABLED -> {
                CommentListDisabledState(
                    message = commentState.errorMessage ?: "この動画ではコメントが無効です",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.icon4xl * 3),
                )
            }

            CommentLoadStatus.ERROR -> {
                CommentListErrorState(
                    message = commentState.errorMessage ?: "コメントの読み込みに失敗しました",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.icon4xl * 3),
                )
            }

            CommentLoadStatus.NOT_LOADED -> {
                CommentListLoadingState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.icon4xl * 3),
                )
            }
        }
    }
}

/**
 * コメントリストヘッダー。
 * チャンネル名とソート切替トグルを表示する。
 */
@Composable
private fun CommentListHeader(
    channelName: String,
    sortOrder: CommentSortOrder,
    onSortOrderChange: (CommentSortOrder) -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // チャンネル名
        Text(
            text = channelName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        // ソート切替ボタン
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            FilterChip(
                selected = sortOrder == CommentSortOrder.LIKES,
                onClick = { onSortOrderChange(CommentSortOrder.LIKES) },
                label = { Text("いいね順") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconXs),
                    )
                },
                enabled = isEnabled,
            )
            FilterChip(
                selected = sortOrder == CommentSortOrder.TIME,
                onClick = { onSortOrderChange(CommentSortOrder.TIME) },
                label = { Text("時間順") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconXs),
                    )
                },
                enabled = isEnabled,
            )
        }
    }
}

/**
 * コメントリストのコンテンツ状態（リスト表示）。
 * スクロール末尾でページネーションを発火する。
 */
@Composable
private fun CommentListContentState(
    markers: List<TimestampMarker>,
    isLoadingMore: Boolean,
    hasNextPage: Boolean,
    onTimestampClick: (timestampSeconds: Long) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // スクロール末尾到達の検出
    val shouldLoadMore by remember(hasNextPage, isLoadingMore) {
        derivedStateOf {
            if (!hasNextPage || isLoadingMore) return@derivedStateOf false
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItemCount = listState.layoutInfo.totalItemsCount
            lastVisibleItem != null && lastVisibleItem.index >= totalItemCount - 3
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { shouldLoadMore }
            .collect { should ->
                if (should) onLoadMore()
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = markers,
            key = { it.comment.commentId },
        ) { marker ->
            CommentListItem(
                marker = marker,
                onTimestampClick = onTimestampClick,
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Spacing.lg),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }

        // ページネーションローディング
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.iconLg),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // リスト末尾の余白
        item {
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

/**
 * コメント読み込み中の状態表示。
 */
@Composable
private fun CommentListLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * タイムスタンプ付きコメントが存在しない場合の空状態表示。
 */
@Composable
private fun CommentListEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "タイムスタンプ付きコメントはありません",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(Spacing.xl),
        )
    }
}

/**
 * コメントが無効化されている場合の状態表示。
 */
@Composable
private fun CommentListDisabledState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(Spacing.xl),
        )
    }
}

/**
 * コメント取得エラーの状態表示。
 */
@Composable
private fun CommentListErrorState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.xl),
            )
        }
    }
}

/**
 * マーカーリストをソート順に並び替える。
 * - LIKES: いいね数降順（重複は commentId で除去）
 * - TIME: タイムスタンプ秒昇順（重複は commentId で除去）
 */
private fun sortMarkers(
    markers: List<TimestampMarker>,
    sortOrder: CommentSortOrder,
): List<TimestampMarker> {
    return when (sortOrder) {
        CommentSortOrder.LIKES -> markers
            .sortedByDescending { it.comment.likeCount }
            .distinctBy { it.comment.commentId }

        CommentSortOrder.TIME -> markers
            .sortedBy { it.timestampSeconds }
            .distinctBy { it.comment.commentId }
    }
}

// ============================================
// Previews
// ============================================

private fun createPreviewMarkers(): List<TimestampMarker> = listOf(
    TimestampMarker(
        timestampSeconds = 630L,
        displayTimestamp = "10:30",
        comment = VideoComment(
            commentId = "c1",
            authorDisplayName = "TechFan2024",
            authorProfileImageUrl = "",
            textContent = "10:30 のところが一番の見どころ！すごく盛り上がった",
            likeCount = 1234,
            publishedAt = "2024-01-15T10:00:00Z",
        ),
    ),
    TimestampMarker(
        timestampSeconds = 1200L,
        displayTimestamp = "20:00",
        comment = VideoComment(
            commentId = "c2",
            authorDisplayName = "GamersChannel",
            authorProfileImageUrl = "",
            textContent = "20:00 からのボス戦マジで熱い",
            likeCount = 567,
            publishedAt = "2024-01-16T12:30:00Z",
        ),
    ),
    TimestampMarker(
        timestampSeconds = 3720L,
        displayTimestamp = "1:02:00",
        comment = VideoComment(
            commentId = "c3",
            authorDisplayName = "ViewerABC",
            authorProfileImageUrl = "",
            textContent = "1:02:00 エンディングが最高すぎる",
            likeCount = 890,
            publishedAt = "2024-01-17T08:00:00Z",
        ),
    ),
)

@Preview
@Composable
private fun CommentListBottomSheetContentPreview() {
    AppTheme {
        CommentListBottomSheetContent(
            channelName = "Gaming Channel",
            commentState = ChannelCommentState(
                videoId = "video_001",
                status = CommentLoadStatus.LOADED,
                markers = createPreviewMarkers(),
                nextPageToken = null,
            ),
            sortOrder = CommentSortOrder.LIKES,
            isLoadingMore = false,
            onSortOrderChange = {},
            onTimestampClick = {},
            onLoadMore = {},
        )
    }
}

@Preview
@Composable
private fun CommentListBottomSheetContentLoadingPreview() {
    AppTheme {
        CommentListBottomSheetContent(
            channelName = "Gaming Channel",
            commentState = ChannelCommentState(
                videoId = "video_001",
                status = CommentLoadStatus.LOADING,
            ),
            sortOrder = CommentSortOrder.LIKES,
            isLoadingMore = false,
            onSortOrderChange = {},
            onTimestampClick = {},
            onLoadMore = {},
        )
    }
}

@Preview
@Composable
private fun CommentListBottomSheetContentEmptyPreview() {
    AppTheme {
        CommentListBottomSheetContent(
            channelName = "Gaming Channel",
            commentState = ChannelCommentState(
                videoId = "video_001",
                status = CommentLoadStatus.LOADED,
                markers = emptyList(),
                nextPageToken = null,
            ),
            sortOrder = CommentSortOrder.LIKES,
            isLoadingMore = false,
            onSortOrderChange = {},
            onTimestampClick = {},
            onLoadMore = {},
        )
    }
}

@Preview
@Composable
private fun CommentListBottomSheetContentDisabledPreview() {
    AppTheme {
        CommentListBottomSheetContent(
            channelName = "Gaming Channel",
            commentState = ChannelCommentState(
                videoId = "video_001",
                status = CommentLoadStatus.DISABLED,
                errorMessage = "この動画ではコメントが無効です",
            ),
            sortOrder = CommentSortOrder.LIKES,
            isLoadingMore = false,
            onSortOrderChange = {},
            onTimestampClick = {},
            onLoadMore = {},
        )
    }
}

@Preview
@Composable
private fun CommentListBottomSheetContentErrorPreview() {
    AppTheme {
        CommentListBottomSheetContent(
            channelName = "Gaming Channel",
            commentState = ChannelCommentState(
                videoId = "video_001",
                status = CommentLoadStatus.ERROR,
                errorMessage = "コメントの読み込みに失敗しました",
            ),
            sortOrder = CommentSortOrder.LIKES,
            isLoadingMore = false,
            onSortOrderChange = {},
            onTimestampClick = {},
            onLoadMore = {},
        )
    }
}

@Preview
@Composable
private fun CommentListBottomSheetContentTimeSortPreview() {
    AppTheme {
        CommentListBottomSheetContent(
            channelName = "Esports Channel",
            commentState = ChannelCommentState(
                videoId = "video_002",
                status = CommentLoadStatus.LOADED,
                markers = createPreviewMarkers(),
                nextPageToken = "next_page_token_abc",
            ),
            sortOrder = CommentSortOrder.TIME,
            isLoadingMore = true,
            onSortOrderChange = {},
            onTimestampClick = {},
            onLoadMore = {},
        )
    }
}
