package org.example.project.feature.streamer_search

import kotlin.time.ExperimentalTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.video_search.SearchMode

/**
 * UI state for streamer search screen
 */
data class StreamerSearchUiState
@OptIn(ExperimentalTime::class)
constructor(
    val searchMode: String, // "MAIN" or "SUB"
    val inputText: String = "",
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val searchNextPageToken: String? = null,
    val selectedDate: LocalDate = kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
        .minus(1, DateTimeUnit.DAY),
    val channelSearchMode: SearchMode = SearchMode.CHANNEL_NAME,
    val selectedService: VideoServiceType = VideoServiceType.YOUTUBE,
    val channelSuggestions: List<ChannelInfo> = emptyList(),
    val isSearchingChannels: Boolean = false,
    val selectedChannel: ChannelInfo? = null,
    val selectedResults: List<SearchResult> = emptyList(),
)
