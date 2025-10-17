package org.example.project.domain.model

/**
 * Domain model for channel information.
 * Used for displaying channel search suggestions.
 */
data class ChannelInfo(
    val id: String,
    val displayName: String,
    val thumbnailUrl: String? = null,
    val broadcasterLanguage: String? = null,
    val gameId: String? = null,
    val gameName: String? = null,
)
