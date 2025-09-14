@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Result of video synchronization containing the calculated absolute time.
 * This represents the computed result after processing video playback position
 * and stream start time information.
 */
@Serializable
data class VideoSyncInfo(
    /**
     * YouTube video ID that was synchronized
     */
    val videoId: String,

    /**
     * Current playback position in seconds
     */
    val playbackSeconds: Float,

    /**
     * When the live stream originally started
     */
    val streamStartTime: Instant,

    /**
     * Calculated absolute time corresponding to current playback position
     * Formula: streamStartTime + playbackSeconds
     */
    val absoluteTime: Instant,
)
