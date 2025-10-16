package org.example.project.feature.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for Home screen
 * Manages saved groups and recent streams (UI only for now)
 */
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // TODO: Load saved groups and recent streams from local storage
        // For now, just show empty state
        _uiState.value = HomeUiState(
            hasRecentStreams = false,
            savedGroupsCount = 0,
        )
    }
}
