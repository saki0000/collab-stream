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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SyncChannel

/**
 * Bottom sheet for adding channels to the timeline.
 *
 * Displays a search field, channel suggestions, and added channels list.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-2 (Channel Add/Remove)
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
                onSearchQueryChange = onSearchQueryChange,
                onChannelSelect = onChannelSelect,
                onChannelRemove = onChannelRemove,
            )
        }
    }
}

/**
 * Content for the channel add bottom sheet.
 */
@Composable
private fun ChannelAddContent(
    searchQuery: String,
    channelSuggestions: List<ChannelInfo>,
    addedChannels: List<SyncChannel>,
    isSearching: Boolean,
    errorMessage: String?,
    onSearchQueryChange: (String) -> Unit,
    onChannelSelect: (ChannelInfo) -> Unit,
    onChannelRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Title
        item {
            Text(
                text = "チャンネルを追加",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        // Search field
        item {
            ChannelSearchField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                isSearching = isSearching,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Error message
        if (errorMessage != null) {
            item {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        // Search suggestions
        if (channelSuggestions.isNotEmpty()) {
            item {
                Text(
                    text = "検索結果",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
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
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }

        // Divider
        if (addedChannels.isNotEmpty()) {
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        // Added channels
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
 * Search field for channel search.
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
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
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
        shape = RoundedCornerShape(12.dp),
    )
}

/**
 * Single channel suggestion item.
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
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Channel avatar
        AsyncImage(
            model = channel.thumbnailUrl,
            contentDescription = channel.displayName,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Channel info
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

        // Add icon
        Box(
            modifier = Modifier
                .size(32.dp)
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
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Single added channel item with remove button.
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
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Channel avatar
        AsyncImage(
            model = channel.channelIconUrl,
            contentDescription = channel.channelName,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Channel name
        Text(
            text = channel.channelName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "削除",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
