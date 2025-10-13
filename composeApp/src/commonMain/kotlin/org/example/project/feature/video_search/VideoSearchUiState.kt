package org.example.project.feature.video_search

import org.example.project.domain.model.SearchResult

/**
 * Data class representing the UI state for video search screen.
 * Contains all necessary state information for search functionality.
 */
data class VideoSearchUiState(
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val searchNextPageToken: String? = null,
)
