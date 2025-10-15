package org.example.project.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.example.project.feature.home.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Container for Home screen
 * Connects UI to ViewModel
 */
@Composable
fun HomeContainer(
    onSearchMainStreamer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        onSearchMainStreamer = onSearchMainStreamer,
        modifier = modifier,
    )
}
