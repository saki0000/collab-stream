package org.example.project.feature.archive_home.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.example.project.feature.archive_home.ArchiveHomeIntent
import org.example.project.feature.archive_home.ArchiveHomeSideEffect
import org.example.project.feature.archive_home.ArchiveHomeViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * アーカイブHome画面のContainer層（Stateful）。
 *
 * ViewModelと接続し、SideEffectを処理する。
 *
 * 4層構造: Container -> Screen -> Content -> Component
 *
 * Epic: Channel Follow & Archive Home (US-3)
 * Story: US-3 (Archive Home Display)
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ArchiveHomeContainer(
    modifier: Modifier = Modifier,
    viewModel: ArchiveHomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 画面初回表示時にデータを読み込む
    LaunchedEffect(Unit) {
        viewModel.handleIntent(ArchiveHomeIntent.LoadScreen)
    }

    // SideEffect を処理
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is ArchiveHomeSideEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        actionLabel = "閉じる",
                        duration = SnackbarDuration.Short,
                    )
                }

                is ArchiveHomeSideEffect.ShowFollowFeedback -> {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    ArchiveHomeScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}
