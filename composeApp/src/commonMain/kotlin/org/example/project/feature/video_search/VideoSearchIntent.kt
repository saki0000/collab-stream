package org.example.project.feature.video_search

import kotlinx.datetime.LocalDate
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType

/**
 * Sealed interface defining all possible user intents for video search functionality.
 * Following MVI architecture pattern for state management.
 */
sealed interface VideoSearchIntent {
    /**
     * Intent to update the input text field (typing)
     */
    data class UpdateInputText(val text: String) : VideoSearchIntent

    /**
     * Intent to execute search with current input text
     */
    data object ExecuteSearch : VideoSearchIntent

    /**
     * Intent to search for videos with the specified query (legacy - prefer ExecuteSearch)
     */
    data class SearchVideos(val query: String) : VideoSearchIntent

    /**
     * Intent to load more search results (pagination)
     */
    data object LoadMoreSearchResults : VideoSearchIntent

    /**
     * Intent to select a search result
     */
    data class SelectSearchResult(val searchResult: SearchResult) : VideoSearchIntent

    /**
     * Intent to clear search error state
     */
    data object ClearSearchError : VideoSearchIntent

    /**
     * Intent to dismiss the search bottom sheet
     */
    data object Dismiss : VideoSearchIntent

    /**
     * Intent to clear search results
     */
    data object ClearSearchResults : VideoSearchIntent

    /**
     * Intent to change the selected date for search
     */
    data class ChangeSelectedDate(val date: LocalDate) : VideoSearchIntent

    /**
     * Intent to change the search mode
     */
    data class ChangeSearchMode(val mode: SearchMode) : VideoSearchIntent

    /**
     * Intent to select a video service (single selection)
     */
    data class SelectService(val service: VideoServiceType) : VideoSearchIntent
}

/**
 * Sealed interface defining side effects for one-time events in video search.
 * Used for navigation and other one-time actions.
 */
sealed interface VideoSearchSideEffect {
    /**
     * Navigate back (dismiss bottom sheet)
     */
    data object NavigateBack : VideoSearchSideEffect

    /**
     * Video was selected, pass the result back
     */
    data class VideoSelected(
        val videoId: String,
        val serviceType: org.example.project.domain.model.VideoServiceType,
    ) : VideoSearchSideEffect

    /**
     * Show search error message
     */
    data class ShowSearchError(val message: String) : VideoSearchSideEffect

    /**
     * Show search success message
     */
    data class ShowSearchSuccess(val message: String) : VideoSearchSideEffect
}
