package org.example.project.domain.model

import kotlinx.serialization.Serializable

/**
 * Enum class defining supported video service types.
 * Supports YouTube and Twitch video services.
 */
@Serializable
enum class VideoServiceType {
    YOUTUBE,
    TWITCH,
}
