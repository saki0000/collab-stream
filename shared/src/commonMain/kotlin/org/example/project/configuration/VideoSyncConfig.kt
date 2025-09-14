package org.example.project.configuration

/**
 * Configuration for video synchronization functionality.
 * Contains API keys and endpoint configurations.
 */
data class VideoSyncConfig(
    /**
     * YouTube Data API v3 key.
     * Required for making requests to YouTube API.
     */
    val youTubeApiKey: String,
) {
    companion object {
        /**
         * Creates a default configuration with placeholder API key.
         * In a real application, this would be set from platform-specific configuration.
         */
        fun default(): VideoSyncConfig {
            return VideoSyncConfig(
                youTubeApiKey = "YOUR_YOUTUBE_API_KEY_HERE", // Placeholder for development
            )
        }
    }
}
