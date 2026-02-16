package org.example.project.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from Twitch Helix API users endpoint.
 * Used to get user information by login name or user ID.
 */
@Serializable
data class TwitchUserResponse(
    @SerialName("data")
    val data: List<TwitchUser> = emptyList(),

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
 * Twitch user information from the users endpoint.
 */
@Serializable
data class TwitchUser(
    @SerialName("id")
    val id: String,

    @SerialName("display_name")
    val displayName: String,

    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null,

    @SerialName("broadcaster_language")
    val broadcasterLanguage: String? = null,

    @SerialName("game_id")
    val gameId: String? = null,

    @SerialName("game_name")
    val gameName: String? = null,
)
