package org.example.project.domain.model

import kotlinx.serialization.Serializable

/**
 * Twitch video details retrieved from Twitch Helix API.
 * Contains essential information for video synchronization.
 */
@Serializable
data class TwitchVideoDetails(
    /**
     * Twitch video ID
     */
    val id: String,

    /**
     * Video snippet information including title and description
     */
    val snippet: VideoSnippet,

    /**
     * Stream information for live streams
     * Contains timing information for synchronization
     */
    val streamInfo: TwitchStreamInfo? = null,
)

/**
 * Twitch stream information for live streams and archived videos.
 * Contains timing information necessary for video synchronization.
 */
@Serializable
data class TwitchStreamInfo(
    /**
     * Stream ID if this is from a live stream
     */
    val streamId: String?,

    /**
     * Time when the video was created (ISO 8601 format)
     */
    val createdAt: String,

    /**
     * Time when the video was published (ISO 8601 format)
     */
    val publishedAt: String,

    /**
     * Video type: "upload", "archive", "highlight"
     */
    val type: String,

    /**
     * Video duration in "1h2m3s" format
     */
    val duration: String,

    /**
     * Whether the video is viewable
     */
    val viewable: String,
)
