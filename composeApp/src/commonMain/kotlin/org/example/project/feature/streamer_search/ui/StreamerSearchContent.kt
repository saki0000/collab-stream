package org.example.project.feature.streamer_search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.video_search.ui.SearchResultItem

/**
 * Content for Streamer Search
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamerSearchContent(
    searchMode: String,
    inputText: String,
    searchQuery: String,
    searchResults: List<SearchResult>,
    isSearching: Boolean,
    searchError: String?,
    hasMoreResults: Boolean,
    selectedDate: LocalDate,
    selectedService: VideoServiceType,
    channelSuggestions: List<ChannelInfo>,
    isSearchingChannels: Boolean,
    selectedResults: List<SearchResult>,
    onInputTextChange: (String) -> Unit,
    onExecuteSearch: () -> Unit,
    onSelectResult: (SearchResult) -> Unit,
    onLoadMore: () -> Unit,
    onClearError: () -> Unit,
    onSelectService: (VideoServiceType) -> Unit,
    onSearchChannels: (String) -> Unit,
    onSelectChannel: (ChannelInfo) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Top App Bar with close button and selection count
        TopAppBar(
            title = {
                Text(
                    text = if (searchMode == "MAIN") "Select Main Streamer" else "Add Sub Streamer",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                    )
                }
            },
        )

        // Service Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Service:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selectedService == VideoServiceType.YOUTUBE,
                    onClick = { onSelectService(VideoServiceType.YOUTUBE) },
                )
                Text("YouTube")
                Spacer(modifier = Modifier.width(8.dp))
                RadioButton(
                    selected = selectedService == VideoServiceType.TWITCH,
                    onClick = { onSelectService(VideoServiceType.TWITCH) },
                )
                Text("Twitch")
            }
        }

        // Search Field with Dropdown
        Box {
            Column {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { newText ->
                        onInputTextChange(newText)
                        // Trigger channel search for Twitch only
                        if (selectedService == VideoServiceType.TWITCH) {
                            onSearchChannels(newText)
                        }
                    },
                    label = { Text("Search by channel name...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                        )
                    },
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onExecuteSearch()
                                    keyboardController?.hide()
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Execute Search",
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (inputText.isNotBlank()) {
                                onExecuteSearch()
                                keyboardController?.hide()
                            }
                        },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Channel Suggestions Dropdown (for Twitch only)
                if (selectedService == VideoServiceType.TWITCH && channelSuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    ) {
                        LazyColumn(
                            modifier = Modifier.height(200.dp),
                        ) {
                            items(channelSuggestions) { channel ->
                                ChannelSuggestionItem(
                                    channel = channel,
                                    onSelect = {
                                        onSelectChannel(channel)
                                        keyboardController?.hide()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Error display
        searchError?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onClearError) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // Search Results
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            when {
                isSearching && searchResults.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Searching...",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                searchResults.isEmpty() && searchQuery.isBlank() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Enter channel name to search",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                searchResults.isEmpty() && searchQuery.isNotBlank() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No streams found for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(searchResults) { result ->
                            val isSelected = selectedResults.any { it.videoId == result.videoId }
                            SearchResultItem(
                                result = result,
                                isSelected = isSelected,
                                isSubSearchMode = searchMode == "SUB",
                                onSelect = { onSelectResult(result) },
                            )
                        }

                        if (hasMoreResults) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isSearching) {
                                        CircularProgressIndicator()
                                    } else {
                                        TextButton(onClick = onLoadMore) {
                                            Text("Load More")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Channel suggestion item for dropdown menu
 */
@Composable
private fun ChannelSuggestionItem(
    channel: ChannelInfo,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            channel.gameName?.let { gameName ->
                Text(
                    text = gameName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
