package org.example.project.domain.model

import kotlin.time.ExperimentalTime
import kotlinx.serialization.Serializable

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

    /**
     * Gets the video duration in seconds.
     * For YouTube: calculates from actualEndTime - actualStartTime
     * For Twitch: parses duration string (e.g., "1h2m3s")
     * Returns null if duration information is not available
     */
    @OptIn(ExperimentalTime::class)
    abstract fun getDurationInSeconds(): Float?
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

    @OptIn(ExperimentalTime::class)
    override fun getDurationInSeconds(): Float? {
        val startTime = liveStreamingDetails?.actualStartTime
        val endTime = liveStreamingDetails?.actualEndTime
        return if (startTime != null && endTime != null) {
            (endTime - startTime).inWholeSeconds.toFloat()
        } else {
            null
        }
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

    @OptIn(ExperimentalTime::class)
    override fun getDurationInSeconds(): Float? {
        return streamInfo?.duration?.let { parseTwitchDuration(it) }
    }

    /**
     * Parses Twitch duration format (e.g., "1h2m3s", "30m", "1h") to seconds.
     * Handles variations with hours, minutes, and/or seconds.
     */
    private fun parseTwitchDuration(duration: String): Float? {
        return try {
            var totalSeconds = 0f
            var currentNumber = ""

            for (char in duration) {
                when {
                    char.isDigit() -> currentNumber += char
                    char == 'h' && currentNumber.isNotEmpty() -> {
                        totalSeconds += currentNumber.toFloat() * 3600
                        currentNumber = ""
                    }
                    char == 'm' && currentNumber.isNotEmpty() -> {
                        totalSeconds += currentNumber.toFloat() * 60
                        currentNumber = ""
                    }
                    char == 's' && currentNumber.isNotEmpty() -> {
                        totalSeconds += currentNumber.toFloat()
                        currentNumber = ""
                    }
                }
            }

            if (totalSeconds > 0f) totalSeconds else null
        } catch (e: Exception) {
            null
        }
    }
}
