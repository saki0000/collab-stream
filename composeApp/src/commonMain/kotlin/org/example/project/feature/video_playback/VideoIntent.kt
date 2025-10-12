package org.example.project.feature.video_playback

import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType

/**
 * Sealed interface defining all possible user intents for video functionality.
 * Following MVI architecture pattern for state management.
 */
sealed interface VideoIntent {
    /**
     * Intent to load a video with the specified video ID (legacy)
     */
    data class LoadVideo(val videoId: String) : VideoIntent

    /**
     * Intent to load a video with the specified video ID and service type
     */
    data class LoadVideoWithService(val videoId: String, val serviceType: VideoServiceType) : VideoIntent

    /**
     * Intent to change the service type
     */
    data class ChangeServiceType(val serviceType: VideoServiceType) : VideoIntent

    /**
     * Intent to clear any error state
     */
    data object ClearError : VideoIntent

    /**
     * Intent to retry loading the current video
     */
    data object RetryLoad : VideoIntent

    /**
     * Intent to synchronize video playback position to absolute time
     */
    data class SyncToAbsoluteTime(val currentTime: Float) : VideoIntent

    /**
     * Intent to handle user-initiated seek to specific position
     */
    data class UserSeekToPosition(val position: Float) : VideoIntent

    /**
     * Intent to clear sync error state
     */
    data object ClearSyncError : VideoIntent

    // Search-related intents

    /**
     * Intent to search for videos with the specified query
     */
    data class SearchVideos(val query: String) : VideoIntent

    /**
     * Intent to load more search results
     */
    data object LoadMoreSearchResults : VideoIntent

    /**
     * Intent to select a search result and load the video
     */
    data class SelectSearchResult(val searchResult: SearchResult) : VideoIntent

    /**
     * Intent to clear search error state
     */
    data object ClearSearchError : VideoIntent

    /**
     * Intent to toggle search bottom sheet visibility
     */
    data object ToggleSearchBottomSheet : VideoIntent

    /**
     * Intent to clear search results
     */
    data object ClearSearchResults : VideoIntent
}

/**
 * Sealed interface defining side effects for one-time events.
 * Used for navigation, snackbars, and other one-time actions.
 */
sealed interface VideoSideEffect {
    /**
     * Show an error message to the user
     */
    data class ShowError(val message: String) : VideoSideEffect

    /**
     * Show a success message when video loads successfully
     */
    data class ShowSuccess(val message: String) : VideoSideEffect

    /**
     * Show sync result to the user
     */
    data class ShowSyncResult(val absoluteTime: String) : VideoSideEffect

    /**
     * Show sync error message
     */
    data class ShowSyncError(val message: String) : VideoSideEffect

    /**
     * Show search error message
     */
    data class ShowSearchError(val message: String) : VideoSideEffect

    /**
     * Show search success message
     */
    data class ShowSearchSuccess(val message: String) : VideoSideEffect
}
