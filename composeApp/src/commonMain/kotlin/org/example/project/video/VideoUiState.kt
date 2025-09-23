@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.video

import kotlin.time.Instant
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.video.player.PlayerState

/**
 * Data class representing the UI state for video display components.
 * Contains all necessary state information for video player rendering and sync functionality.
 */
data class VideoUiState(
    val videoId: String = "",
    val serviceType: VideoServiceType = VideoServiceType.YOUTUBE,
    val syncDateTime: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Player state integration
    val playerState: PlayerState = PlayerState.NotInitialized,
    val isPlayerReady: Boolean = false,
    // Sync-related state
    val isSyncing: Boolean = false,
    val syncResult: VideoSyncUiState? = null,
    val syncError: String? = null,
    val currentTime: Float = 0L.toFloat(),
    // User seek tracking
    val lastUserSeekPosition: Float? = null,
    val lastUserSeekTime: String? = null,
    // Search-related state
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val isSearchBottomSheetVisible: Boolean = false,
    val searchNextPageToken: String? = null,
)

/**
 * UI state representing the result of video synchronization.
 */
data class VideoSyncUiState(
    val videoId: String,
    val playbackSeconds: Float,
    val streamStartTime: Instant,
    val absoluteTime: Instant,
    val formattedAbsoluteTime: String,
)
