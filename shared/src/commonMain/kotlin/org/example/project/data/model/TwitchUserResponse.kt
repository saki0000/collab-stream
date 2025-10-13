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
)
