package org.example.project.feature.video_search

import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType

/**
 * Data class representing the UI state for video search screen.
 * Contains all necessary state information for search functionality.
 */
data class VideoSearchUiState
@OptIn(ExperimentalTime::class)
constructor(
    val inputText: String = "",
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val searchNextPageToken: String? = null,
    val selectedDate: LocalDate = kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val searchMode: SearchMode = SearchMode.KEYWORD,
    val selectedServices: Set<VideoServiceType> = setOf(VideoServiceType.YOUTUBE),
)

/**
 * Enum representing the search mode
 */
enum class SearchMode {
    KEYWORD, // Free keyword search
    CHANNEL_NAME, // Channel name search
}
