package org.example.project.feature.streamer_search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlin.time.ExperimentalTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.streamer_search.ui.SearchResultItem

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
    showDatePicker: Boolean,
    onInputTextChange: (String) -> Unit,
    onExecuteSearch: () -> Unit,
    onSelectResult: (SearchResult) -> Unit,
    onLoadMore: () -> Unit,
    onClearError: () -> Unit,
    onSelectService: (VideoServiceType) -> Unit,
    onSearchChannels: (String) -> Unit,
    onSelectChannel: (ChannelInfo) -> Unit,
    onToggleDatePicker: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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

        // Service Selection (FilterChips)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = selectedService == VideoServiceType.YOUTUBE,
                onClick = { onSelectService(VideoServiceType.YOUTUBE) },
                label = { Text("YouTube") },
            )
            FilterChip(
                selected = selectedService == VideoServiceType.TWITCH,
                onClick = { onSelectService(VideoServiceType.TWITCH) },
                label = { Text("Twitch") },
            )
            FilterChip(
                selected = true,
                onClick = onToggleDatePicker,
                label = { Text(formatDateLabel(selectedDate)) },
            )
        }

        // DatePicker Dialog
        if (showDatePicker) {
            DatePickerModal(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    onDateSelected(date)
                    onToggleDatePicker()
                },
                onDismiss = onToggleDatePicker,
            )
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
 * Formats the selected date as a label for the UI.
 * Shows "昨日" if the date is yesterday, otherwise shows the date in YYYY-MM-DD format.
 */
@OptIn(ExperimentalTime::class)
private fun formatDateLabel(date: LocalDate): String {
    val today = kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)

    return when (date) {
        yesterday -> "昨日"
        today -> "今日"
        else -> "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
    }
}

/**
 * DatePicker dialog for selecting a date
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun DatePickerModal(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialSelectedDateMillis = selectedDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val localDate = instant.toLocalDateTime(TimeZone.UTC).date
                        onDateSelected(localDate)
                    }
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        },
    ) {
        DatePicker(state = datePickerState)
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
