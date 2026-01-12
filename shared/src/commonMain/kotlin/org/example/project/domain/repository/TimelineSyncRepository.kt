package org.example.project.domain.repository

import kotlinx.datetime.LocalDate
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType

/**
 * Repository interface for Timeline Sync functionality.
 *
 * Extends VideoSyncRepository to inherit getVideoDetails functionality,
 * and adds channel-specific video retrieval for timeline display.
 *
 * Handles communication with video service APIs (YouTube, Twitch) to retrieve
 * channel videos and video details necessary for timeline display and
 * synchronization calculations.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Shared across: US-1 (Timeline Display), US-2 (Channel Management),
 *                US-3 (Sync Calculation)
 */
interface TimelineSyncRepository : VideoSyncRepository {
    /**
     * Retrieves a list of videos from a channel within a specified date range.
     *
     * Fetches past broadcasts/videos from the specified channel that fall within
     * the given date range. Used for populating the timeline with available streams.
     *
     * @param channelId Channel ID to fetch videos from
     * @param serviceType The video service to query (YouTube or Twitch)
     * @param dateRange Date range to filter videos by (inclusive)
     * @return Result containing a list of VideoDetails on success, or error information on failure
     */
    suspend fun getChannelVideos(
        channelId: String,
        serviceType: VideoServiceType,
        dateRange: ClosedRange<LocalDate>,
    ): Result<List<VideoDetails>>
}
