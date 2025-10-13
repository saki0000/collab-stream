package org.example.project.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from Twitch Helix API videos search endpoint.
 * Used for searching VODs (Video on Demand) on Twitch.
 */
@Serializable
data class TwitchSearchResponse(
    @SerialName("data")
    val data: List<TwitchSearchItem> = emptyList(),

    @SerialName("pagination")
    val pagination: TwitchPagination? = null,

    @SerialName("error")
    val error: String? = null,

    @SerialName("message")
    val message: String? = null,

    @SerialName("status")
    val status: Int? = null,
)

/**
 * Individual video item from Twitch search response.
 * Represents a VOD (archived stream) result.
 */
@Serializable
data class TwitchSearchItem(
    @SerialName("id")
    val id: String,

    @SerialName("stream_id")
    val streamId: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("user_login")
    val userLogin: String,

    @SerialName("user_name")
    val userName: String,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("published_at")
    val publishedAt: String,

    @SerialName("url")
    val url: String,

    @SerialName("thumbnail_url")
    val thumbnailUrl: String,

    @SerialName("viewable")
    val viewable: String,

    @SerialName("view_count")
    val viewCount: Int,

    @SerialName("language")
    val language: String,

    @SerialName("type")
    val type: String, // "upload", "archive", "highlight"

    @SerialName("duration")
    val duration: String, // Format: "1h2m3s"
)
