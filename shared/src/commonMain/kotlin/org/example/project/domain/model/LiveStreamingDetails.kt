@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Live streaming details from YouTube Data API v3.
 * Contains timing information necessary for video synchronization.
 */
@Serializable
data class LiveStreamingDetails(
    /**
     * Time when the live stream actually started broadcasting.
     * This is crucial for calculating absolute time from playback position.
     * Format: RFC 3339 timestamp (e.g., "2023-01-15T10:00:00Z")
     */
    val actualStartTime: Instant?,

    /**
     * Time when the live stream was scheduled to start.
     * May differ from actualStartTime if the stream started early/late.
     */
    val scheduledStartTime: Instant?,

    /**
     * Time when the live stream ended.
     * Null for ongoing streams or streams without recorded end time.
     */
    val actualEndTime: Instant?,

    /**
     * Current concurrent viewers for live streams.
     * Null for ended streams or if viewer count is hidden.
     */
    val concurrentViewers: Long?
)