package org.example.project.feature.video_search

import kotlinx.serialization.Serializable
import org.example.project.domain.model.VideoServiceType

/**
 * Result data class for passing selected video information back to the calling screen.
 * Used with SavedStateHandle for type-safe result passing between navigation destinations.
 */
@Serializable
data class VideoSelectionResult(
    val videoId: String,
    val serviceType: VideoServiceType,
)
