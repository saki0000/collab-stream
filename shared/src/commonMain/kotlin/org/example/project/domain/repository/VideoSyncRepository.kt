package org.example.project.domain.repository

import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType

/**
 * Repository interface for video synchronization functionality.
 * Handles communication with video service APIs (YouTube, Twitch) to retrieve video details
 * necessary for time synchronization calculations.
 */
interface VideoSyncRepository {
    /**
     * Retrieves video details from the specified video service API.
     *
     * Fetches video information including basic snippet data and service-specific
     * timing details needed for synchronization calculations.
     *
     * @param videoId Video ID to fetch details for
     * @param serviceType The video service to query (YouTube or Twitch)
     * @return Result containing VideoDetails on success, or error information on failure
     */
    suspend fun getVideoDetails(videoId: String, serviceType: VideoServiceType): Result<VideoDetails>

}
