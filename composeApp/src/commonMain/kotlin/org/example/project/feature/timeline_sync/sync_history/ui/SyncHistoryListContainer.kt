@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.feature.timeline_sync.sync_history.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.example.project.feature.timeline_sync.sync_history.SyncHistoryListIntent
import org.example.project.feature.timeline_sync.sync_history.SyncHistoryListSideEffect
import org.example.project.feature.timeline_sync.sync_history.SyncHistoryListViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * 同期履歴一覧画面のContainer層（Stateful）。
 *
 * ViewModelと接続し、SideEffectを処理する。
 * Clock.System.now() の呼び出しはこの層でのみ行い、Screen/Content/Componentには渡す。
 *
 * 4層構造: Container -> Screen -> Content -> Component
 *
 * Epic: 同期チャンネル履歴保存 (EPIC-003)
 * Story: US-3 (履歴一覧表示)
 *
 * @param onNavigateBack 戻るボタンのコールバック（NavGraphから提供）
 */
@OptIn(ExperimentalTime::class)
@Composable
fun SyncHistoryListContainer(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SyncHistoryListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    // Clock.System はContainer層でのみ使用する（Clock使用ルール準拠）
    val now = remember { Clock.System.now() }

    // 画面初回表示時にデータを読み込む
    LaunchedEffect(Unit) {
        viewModel.handleIntent(SyncHistoryListIntent.LoadScreen)
    }

    // SideEffect を処理
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                SyncHistoryListSideEffect.ShowDeleteSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = "履歴を削除しました",
                        duration = SnackbarDuration.Short,
                    )
                }

                SyncHistoryListSideEffect.ShowDeleteError -> {
                    snackbarHostState.showSnackbar(
                        message = "削除に失敗しました",
                        duration = SnackbarDuration.Short,
                    )
                }

                SyncHistoryListSideEffect.ShowRenameError -> {
                    snackbarHostState.showSnackbar(
                        message = "名前の変更に失敗しました",
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    SyncHistoryListScreen(
        uiState = uiState,
        now = now,
        onIntent = viewModel::handleIntent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}
