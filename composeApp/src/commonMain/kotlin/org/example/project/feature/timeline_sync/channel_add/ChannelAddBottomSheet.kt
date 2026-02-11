package org.example.project.feature.timeline_sync.channel_add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import org.example.project.core.theme.AppShapes
import org.example.project.core.theme.Dimensions
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SyncChannel
import org.example.project.domain.model.VideoServiceType

/**
 * チャンネル追加用ボトムシート。
 *
 * プラットフォーム選択タブ、検索フィールド、チャンネル候補、追加済みチャンネルリストを表示する。
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-2 (Channel Add/Remove), US-5 (Multi-Platform Search)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelAddBottomSheet(
    isVisible: Boolean,
    searchQuery: String,
    channelSuggestions: List<ChannelInfo>,
    addedChannels: List<SyncChannel>,
    isSearching: Boolean,
    errorMessage: String?,
    selectedPlatform: VideoServiceType,
    onPlatformSelect: (VideoServiceType) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onChannelSelect: (ChannelInfo) -> Unit,
    onChannelRemove: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier,
        ) {
            ChannelAddContent(
                searchQuery = searchQuery,
                channelSuggestions = channelSuggestions,
                addedChannels = addedChannels,
                isSearching = isSearching,
                errorMessage = errorMessage,
                selectedPlatform = selectedPlatform,
                onPlatformSelect = onPlatformSelect,
                onSearchQueryChange = onSearchQueryChange,
                onChannelSelect = onChannelSelect,
                onChannelRemove = onChannelRemove,
            )
        }
    }
}

/**
 * チャンネル追加ボトムシートのコンテンツ。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelAddContent(
    searchQuery: String,
    channelSuggestions: List<ChannelInfo>,
    addedChannels: List<SyncChannel>,
    isSearching: Boolean,
    errorMessage: String?,
    selectedPlatform: VideoServiceType,
    onPlatformSelect: (VideoServiceType) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onChannelSelect: (ChannelInfo) -> Unit,
    onChannelRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
            .padding(bottom = Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        // タイトル
        item {
            Text(
                text = "チャンネルを追加",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )
        }

        // プラットフォーム選択タブ（US-5）
        item {
            PlatformSelectionTabs(
                selectedPlatform = selectedPlatform,
                onPlatformSelect = onPlatformSelect,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // 検索フィールド
        item {
            ChannelSearchField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                isSearching = isSearching,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // エラーメッセージ
        if (errorMessage != null) {
            item {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        // 検索候補
        if (channelSuggestions.isNotEmpty()) {
            item {
                Text(
                    text = "検索結果",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.sm),
                )
            }
            items(channelSuggestions, key = { it.id }) { channel ->
                ChannelSuggestionItem(
                    channel = channel,
                    onClick = { onChannelSelect(channel) },
                )
            }
        } else if (searchQuery.isNotBlank() && !isSearching) {
            item {
                Text(
                    text = "検索結果が見つかりませんでした",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Spacing.sm),
                )
            }
        }

        // 区切り線
        if (addedChannels.isNotEmpty()) {
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))
            }
        }

        // 追加済みチャンネル
        if (addedChannels.isNotEmpty()) {
            item {
                Text(
                    text = "追加済みチャンネル (${addedChannels.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(addedChannels, key = { it.channelId }) { channel ->
                AddedChannelItem(
                    channel = channel,
                    onRemove = { onChannelRemove(channel.channelId) },
                )
            }
        }
    }
}

/**
 * プラットフォーム選択タブ（SegmentedButton）。
 * Twitch / YouTube の切り替えを行う。
 * Story 5: Multi-Platform Search
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlatformSelectionTabs(
    selectedPlatform: VideoServiceType,
    onPlatformSelect: (VideoServiceType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val platforms = listOf(VideoServiceType.TWITCH, VideoServiceType.YOUTUBE)

    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        platforms.forEachIndexed { index, platform ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = platforms.size,
                ),
                onClick = { onPlatformSelect(platform) },
                selected = selectedPlatform == platform,
                label = {
                    Text(
                        text = when (platform) {
                            VideoServiceType.TWITCH -> "Twitch"
                            VideoServiceType.YOUTUBE -> "YouTube"
                        },
                    )
                },
            )
        }
    }
}

/**
 * チャンネル検索フィールド。
 */
@Composable
private fun ChannelSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("チャンネル名で検索") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "検索",
            )
        },
        trailingIcon = {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimensions.iconMd),
                    strokeWidth = Spacing.xxs,
                )
            } else if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "クリア",
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions.Default,
        shape = AppShapes.large,
    )
}

/**
 * チャンネル候補アイテム（プラットフォームアイコン付き）。
 */
@Composable
private fun ChannelSuggestionItem(
    channel: ChannelInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.medium)
            .clickable(onClick = onClick)
            .padding(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // チャンネルアバター
        AsyncImage(
            model = channel.thumbnailUrl,
            contentDescription = channel.displayName,
            modifier = Modifier
                .size(Dimensions.avatarSm)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(Spacing.sm))

        // プラットフォームアイコン（US-5）
        PlatformIcon(serviceType = channel.serviceType)

        Spacer(modifier = Modifier.width(Spacing.sm))

        // チャンネル情報
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            channel.gameName?.let { gameName ->
                Text(
                    text = gameName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // 追加アイコン
        Box(
            modifier = Modifier
                .size(Dimensions.iconXl)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "追加",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(Dimensions.iconMd),
            )
        }
    }
}

/**
 * 追加済みチャンネルアイテム（プラットフォームアイコン付き）。
 */
@Composable
private fun AddedChannelItem(
    channel: SyncChannel,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // チャンネルアバター
        AsyncImage(
            model = channel.channelIconUrl,
            contentDescription = channel.channelName,
            modifier = Modifier
                .size(Dimensions.avatarSm)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(Spacing.sm))

        // プラットフォームアイコン（US-5）
        PlatformIcon(serviceType = channel.serviceType)

        Spacer(modifier = Modifier.width(Spacing.sm))

        // チャンネル名
        Text(
            text = channel.channelName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        // 削除ボタン
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(Dimensions.iconXl),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "削除",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(Dimensions.iconMd),
            )
        }
    }
}

/**
 * プラットフォームアイコン表示。
 * Twitch / YouTube のアイコンをテキストで表示する。
 * Story 5: Multi-Platform Search
 */
@Composable
private fun PlatformIcon(
    serviceType: VideoServiceType,
    modifier: Modifier = Modifier,
) {
    Text(
        text = when (serviceType) {
            VideoServiceType.TWITCH -> "Tw"
            VideoServiceType.YOUTUBE -> "YT"
        },
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = when (serviceType) {
            VideoServiceType.TWITCH -> MaterialTheme.colorScheme.primary
            VideoServiceType.YOUTUBE -> MaterialTheme.colorScheme.error
        },
        modifier = modifier,
    )
}
