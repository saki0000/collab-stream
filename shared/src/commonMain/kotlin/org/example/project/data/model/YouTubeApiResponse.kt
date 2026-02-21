package org.example.project.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from YouTube Data API v3 videos endpoint.
 * Maps to the JSON structure returned by the API.
 */
@Serializable
data class YouTubeApiResponse(
    @SerialName("kind")
    val kind: String,

    @SerialName("etag")
    val etag: String,

    @SerialName("items")
    val items: List<YouTubeVideoItem>,
)

/**
 * Individual video item from YouTube API response.
 */
@Serializable
data class YouTubeVideoItem(
    @SerialName("kind")
    val kind: String,

    @SerialName("etag")
    val etag: String,

    @SerialName("id")
    val id: String,

    @SerialName("snippet")
    val snippet: YouTubeSnippetDto?,

    @SerialName("liveStreamingDetails")
    val liveStreamingDetails: YouTubeLiveStreamingDetailsDto? = null,
)

/**
 * Video snippet DTO from YouTube API.
 */
@Serializable
data class YouTubeSnippetDto(
    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("channelId")
    val channelId: String,

    @SerialName("channelTitle")
    val channelTitle: String,
)

/**
 * Live streaming details DTO from YouTube API.
 */
@Serializable
data class YouTubeLiveStreamingDetailsDto(
    @SerialName("actualStartTime")
    val actualStartTime: String?,

    @SerialName("scheduledStartTime")
    val scheduledStartTime: String?,

    @SerialName("actualEndTime")
    val actualEndTime: String?,
)
