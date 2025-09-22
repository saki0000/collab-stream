package org.example.project.util

import org.example.project.domain.model.VideoServiceType

/**
 * Utility object for parsing video IDs and URLs from different video services.
 * Supports YouTube and Twitch video URL parsing and ID validation.
 */
object VideoIdParser {

    /**
     * Parse a video input (URL or ID) to extract the video ID and determine the service type.
     *
     * @param input The input string (URL or direct video ID)
     * @return Pair of (videoId, serviceType) if successful, null if parsing fails
     */
    fun parseVideoInput(input: String): Pair<String, VideoServiceType>? {
        val trimmedInput = input.trim()

        return when {
            // Twitch URL patterns
            isTwitchUrl(trimmedInput) -> {
                val videoId = extractTwitchVideoId(trimmedInput)
                if (videoId != null) Pair(videoId, VideoServiceType.TWITCH) else null
            }

            // YouTube URL patterns
            isYouTubeUrl(trimmedInput) -> {
                val videoId = extractYouTubeVideoId(trimmedInput)
                if (videoId != null) Pair(videoId, VideoServiceType.YOUTUBE) else null
            }

            // Direct Twitch video ID (with or without 'v' prefix)
            isTwitchVideoId(trimmedInput) -> {
                Pair(trimmedInput, VideoServiceType.TWITCH)
            }

            // Direct YouTube video ID (11 characters alphanumeric)
            isYouTubeVideoId(trimmedInput) -> {
                Pair(trimmedInput, VideoServiceType.YOUTUBE)
            }

            else -> null
        }
    }

    /**
     * Parse video input with explicit service type preference.
     * If service type is provided, it will be used for ambiguous cases.
     */
    fun parseVideoInput(input: String, preferredServiceType: VideoServiceType): Pair<String, VideoServiceType>? {
        val trimmedInput = input.trim()

        return when {
            // URL patterns - these take precedence
            isTwitchUrl(trimmedInput) -> {
                val videoId = extractTwitchVideoId(trimmedInput)
                if (videoId != null) Pair(videoId, VideoServiceType.TWITCH) else null
            }

            isYouTubeUrl(trimmedInput) -> {
                val videoId = extractYouTubeVideoId(trimmedInput)
                if (videoId != null) Pair(videoId, VideoServiceType.YOUTUBE) else null
            }

            // Direct IDs - use preferred service type for ambiguous cases
            else -> {
                when (preferredServiceType) {
                    VideoServiceType.TWITCH -> {
                        if (isTwitchVideoId(trimmedInput)) {
                            Pair(trimmedInput, VideoServiceType.TWITCH)
                        } else {
                            null
                        }
                    }
                    VideoServiceType.YOUTUBE -> {
                        if (isYouTubeVideoId(trimmedInput)) {
                            Pair(trimmedInput, VideoServiceType.YOUTUBE)
                        } else {
                            null
                        }
                    }
                }
            }
        }
    }

    // Twitch URL detection and parsing
    private fun isTwitchUrl(input: String): Boolean {
        return input.contains("twitch.tv", ignoreCase = true)
    }

    private fun extractTwitchVideoId(url: String): String? {
        val videoPattern = Regex("""twitch\.tv/videos/(\d+)""", RegexOption.IGNORE_CASE)
        val match = videoPattern.find(url)
        return match?.groupValues?.get(1)
    }

    private fun isTwitchVideoId(input: String): Boolean {
        // Twitch video IDs are numeric, optionally with 'v' prefix
        return when {
            input.startsWith("v") && input.length > 1 -> {
                input.substring(1).all { it.isDigit() }
            }
            else -> input.all { it.isDigit() } && input.length >= 8 // Typical Twitch video ID length
        }
    }

    // YouTube URL detection and parsing
    private fun isYouTubeUrl(input: String): Boolean {
        return input.contains("youtube.com", ignoreCase = true) ||
            input.contains("youtu.be", ignoreCase = true)
    }

    private fun extractYouTubeVideoId(url: String): String? {
        // Handle various YouTube URL formats
        val patterns = listOf(
            // Standard youtube.com URLs
            Regex("""youtube\.com/watch\?v=([a-zA-Z0-9_-]{11})""", RegexOption.IGNORE_CASE),
            // Short youtu.be URLs
            Regex("""youtu\.be/([a-zA-Z0-9_-]{11})""", RegexOption.IGNORE_CASE),
            // Embedded URLs
            Regex("""youtube\.com/embed/([a-zA-Z0-9_-]{11})""", RegexOption.IGNORE_CASE),
            // YouTube with additional parameters
            Regex("""youtube\.com/watch\?.*v=([a-zA-Z0-9_-]{11})""", RegexOption.IGNORE_CASE),
        )

        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }

        return null
    }

    private fun isYouTubeVideoId(input: String): Boolean {
        // YouTube video IDs are exactly 11 characters, alphanumeric with hyphens and underscores
        return input.length == 11 && input.all { it.isLetterOrDigit() || it == '-' || it == '_' }
    }

    /**
     * Validate if a video ID is valid for the given service type.
     */
    fun isValidVideoId(videoId: String, serviceType: VideoServiceType): Boolean {
        return when (serviceType) {
            VideoServiceType.YOUTUBE -> isYouTubeVideoId(videoId)
            VideoServiceType.TWITCH -> isTwitchVideoId(videoId)
        }
    }

    /**
     * Format a Twitch video ID to ensure it has the 'v' prefix required by the API.
     */
    fun formatTwitchVideoId(videoId: String): String {
        return if (videoId.startsWith("v")) videoId else "v$videoId"
    }
}
