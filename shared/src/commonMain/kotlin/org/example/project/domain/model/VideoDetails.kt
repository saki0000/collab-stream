package org.example.project.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

/**
 * Common video details interface for multi-platform video services.
 * Provides unified access to video information regardless of the service.
 */
@Serializable
sealed class VideoDetails {
    abstract val id: String
    abstract val snippet: VideoSnippet
    abstract val serviceType: VideoServiceType

    /**
     * Gets the start time for synchronization calculations.
     * For YouTube: uses live streaming details actual start time
     * For Twitch: uses created_at timestamp as the reference point
     */
    @OptIn(ExperimentalTime::class)
    abstract fun getStartTimeForSync(): kotlin.time.Instant?
}

/**
 * YouTube video details implementation.
 */
@Serializable
data class YouTubeVideoDetailsImpl(
    override val id: String,
    override val snippet: VideoSnippet,
    val liveStreamingDetails: LiveStreamingDetails? = null,
) : VideoDetails() {
    override val serviceType: VideoServiceType = VideoServiceType.YOUTUBE

    @OptIn(ExperimentalTime::class)
    override fun getStartTimeForSync(): kotlin.time.Instant? {
        return liveStreamingDetails?.actualStartTime
    }
}

/**
 * Twitch video details implementation.
 */
@Serializable
data class TwitchVideoDetailsImpl(
    override val id: String,
    override val snippet: VideoSnippet,
    val streamInfo: TwitchStreamInfo? = null,
) : VideoDetails() {
    override val serviceType: VideoServiceType = VideoServiceType.TWITCH

    @OptIn(ExperimentalTime::class)
    override fun getStartTimeForSync(): kotlin.time.Instant? {
        return streamInfo?.let { info ->
            try {
                kotlin.time.Instant.parse(info.createdAt)
            } catch (e: Exception) {
                null
            }
        }
    }
}