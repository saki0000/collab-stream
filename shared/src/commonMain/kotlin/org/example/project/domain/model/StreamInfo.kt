package org.example.project.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Unified data model for both Main and Sub streams.
 * Extends SearchResult concept with additional fields for multi-stream synchronization.
 */
@OptIn(ExperimentalTime::class)
data class StreamInfo(
    // Core video information
    val streamId: String, // videoId
    val title: String,
    val thumbnailUrl: String,

    // Channel/Streamer information
    val channelId: String,
    val channelName: String,
    val channelIconUrl: String,

    // Service and metadata
    val serviceType: VideoServiceType,
    val publishedAt: Instant,
    val isLive: Boolean,

    // Sync-related state (mutable during playback)
    val currentTime: Float = 0f,
    val isSynced: Boolean = false,
)

/**
 * Convenience function to convert SearchResult to StreamInfo
 */
@OptIn(ExperimentalTime::class)
fun SearchResult.toStreamInfo(
    channelId: String,
    channelIconUrl: String,
    serviceType: VideoServiceType,
): StreamInfo = StreamInfo(
    streamId = this.videoId,
    title = this.title,
    thumbnailUrl = this.thumbnailUrl,
    channelId = channelId,
    channelName = this.channelTitle,
    channelIconUrl = channelIconUrl,
    serviceType = serviceType,
    publishedAt = this.publishedAt,
    isLive = this.isLiveBroadcast,
    currentTime = 0f,
    isSynced = false,
)
