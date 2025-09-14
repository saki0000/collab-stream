package org.example.project.domain.model

import kotlinx.serialization.Serializable

/**
 * YouTube video details retrieved from YouTube Data API v3.
 * Contains essential information for video synchronization.
 */
@Serializable
data class YouTubeVideoDetails(
    /**
     * YouTube video ID
     */
    val id: String,

    /**
     * Video snippet information including title and description
     */
    val snippet: VideoSnippet,

    /**
     * Live streaming details if this is a live stream or was a live stream
     * Null for regular uploaded videos
     */
    val liveStreamingDetails: LiveStreamingDetails?
)

/**
 * Basic video snippet information from YouTube API
 */
@Serializable
data class VideoSnippet(
    /**
     * Video title
     */
    val title: String,

    /**
     * Video description
     */
    val description: String,

    /**
     * Channel ID that owns this video
     */
    val channelId: String,

    /**
     * Channel title that owns this video
     */
    val channelTitle: String
)