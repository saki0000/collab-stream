package org.example.project.feature.home

/**
 * UI state for Home screen
 */
data class HomeUiState(
    val hasRecentStreams: Boolean = false,
    val savedGroupsCount: Int = 0, // Placeholder for future group save feature
)
