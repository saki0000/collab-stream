package org.example.project.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YouTubeSearchResponse(
    @SerialName("items")
    val items: List<YouTubeSearchItem>,
    @SerialName("nextPageToken")
    val nextPageToken: String? = null,
    @SerialName("prevPageToken")
    val prevPageToken: String? = null,
    @SerialName("pageInfo")
    val pageInfo: YouTubePageInfo,
)

@Serializable
data class YouTubeSearchItem(
    @SerialName("id")
    val id: YouTubeSearchId,
    @SerialName("snippet")
    val snippet: YouTubeSearchSnippet,
)

@Serializable
data class YouTubeSearchId(
    @SerialName("kind")
    val kind: String,
    @SerialName("videoId")
    val videoId: String,
)

@Serializable
data class YouTubeSearchSnippet(
    @SerialName("publishedAt")
    val publishedAt: String,
    @SerialName("channelId")
    val channelId: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("thumbnails")
    val thumbnails: YouTubeThumbnails,
    @SerialName("channelTitle")
    val channelTitle: String,
    @SerialName("liveBroadcastContent")
    val liveBroadcastContent: String,
)

@Serializable
data class YouTubeThumbnails(
    @SerialName("default")
    val default: YouTubeThumbnail? = null,
    @SerialName("medium")
    val medium: YouTubeThumbnail? = null,
    @SerialName("high")
    val high: YouTubeThumbnail? = null,
)

@Serializable
data class YouTubeThumbnail(
    @SerialName("url")
    val url: String,
    @SerialName("width")
    val width: Int? = null,
    @SerialName("height")
    val height: Int? = null,
)

@Serializable
data class YouTubePageInfo(
    @SerialName("totalResults")
    val totalResults: Int,
    @SerialName("resultsPerPage")
    val resultsPerPage: Int,
)
