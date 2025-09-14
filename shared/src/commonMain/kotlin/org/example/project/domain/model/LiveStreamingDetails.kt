@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.domain.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.InstantComponentSerializer

/**
 * Live streaming details from YouTube Data API v3.
 * Contains timing information necessary for video synchronization.
 */
@Serializable(with = InstantComponentSerializer::class)
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
)
