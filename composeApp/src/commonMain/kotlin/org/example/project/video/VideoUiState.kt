package org.example.project.video

/**
 * Data class representing the UI state for video display components.
 * Contains all necessary state information for video player rendering.
 */
data class VideoUiState(
    val videoId: String = "",
    val serviceType: VideoServiceType = VideoServiceType.YOUTUBE,
    val syncDateTime: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Enum class defining supported video service types.
 * Currently only supports YouTube, but can be extended for future services.
 */
enum class VideoServiceType {
    YOUTUBE
}