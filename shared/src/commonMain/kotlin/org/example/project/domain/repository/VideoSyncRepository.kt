package org.example.project.domain.repository

import org.example.project.domain.model.YouTubeVideoDetails

/**
 * Repository interface for video synchronization functionality.
 * Handles communication with YouTube Data API v3 to retrieve video details
 * necessary for time synchronization calculations.
 */
interface VideoSyncRepository {
    /**
     * Retrieves video details from YouTube Data API v3.
     *
     * Fetches both basic video information (snippet) and live streaming details
     * which contain the actual start time needed for synchronization.
     *
     * @param videoId YouTube video ID to fetch details for
     * @return Result containing YouTubeVideoDetails on success, or error information on failure
     */
    suspend fun getVideoDetails(videoId: String): Result<YouTubeVideoDetails>
}
