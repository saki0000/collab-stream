package org.example.project.feature.video_search.ui

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.video_search.SearchMode

/**
 * Content (Business Logic) - Search form and results list
 * Handles user input and displays search results
 */
@Composable
fun VideoSearchContent(
    inputText: String,
    searchQuery: String,
    searchResults: List<SearchResult>,
    isSearching: Boolean,
    searchError: String?,
    hasMoreResults: Boolean,
    selectedDate: LocalDate,
    searchMode: SearchMode,
    selectedServices: Set<VideoServiceType>,
    onInputTextChange: (String) -> Unit,
    onExecuteSearch: () -> Unit,
    onSelectResult: (SearchResult) -> Unit,
    onLoadMore: () -> Unit,
    onClearError: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onSearchModeChange: (SearchMode) -> Unit,
    onToggleService: (VideoServiceType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Title
        Text(
            text = "Search Videos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
        )

        // Date Selection (Simple text display with icon for now)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Calendar",
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Date: $selectedDate",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "(Date picker - coming soon)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Service Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Services:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = selectedServices.contains(VideoServiceType.YOUTUBE),
                    onCheckedChange = { onToggleService(VideoServiceType.YOUTUBE) },
                )
                Text("YouTube")
                Spacer(modifier = Modifier.width(8.dp))
                Checkbox(
                    checked = selectedServices.contains(VideoServiceType.TWITCH),
                    onCheckedChange = { onToggleService(VideoServiceType.TWITCH) },
                )
                Text("Twitch")
            }
        }

        // Search Mode Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = searchMode == SearchMode.KEYWORD,
                onClick = { onSearchModeChange(SearchMode.KEYWORD) },
                label = { Text("Keyword Search") },
            )
            FilterChip(
                selected = searchMode == SearchMode.CHANNEL_NAME,
                onClick = { onSearchModeChange(SearchMode.CHANNEL_NAME) },
                label = { Text("Channel Name") },
            )
        }

        // Search Field
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputTextChange,
            label = {
                Text(
                    when (searchMode) {
                        SearchMode.KEYWORD -> "Search by keyword..."
                        SearchMode.CHANNEL_NAME -> "Search by channel name..."
                    },
                )
            },
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
                .fillMaxWidth()
                .height(400.dp),
        ) {
            when {
                isSearching && searchResults.isEmpty() -> {
                    // Initial loading
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
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Enter a search query to find videos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                searchResults.isEmpty() && searchQuery.isNotBlank() -> {
                    // No results
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No videos found for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                else -> {
                    // Results list
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(searchResults) { result ->
                            SearchResultItem(
                                result = result,
                                onSelect = { onSelectResult(result) },
                            )
                        }

                        // Load more button
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

        // Bottom spacing for safe area
        Spacer(modifier = Modifier.height(16.dp))
    }
}
