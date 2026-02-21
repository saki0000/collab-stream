package org.example.project.feature.timeline_sync.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.datetime.LocalDate
import org.example.project.feature.timeline_sync.TimelineSyncIntent
import org.example.project.feature.timeline_sync.TimelineSyncSideEffect
import org.example.project.feature.timeline_sync.TimelineSyncViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Container Composable (Stateful) - Connects to ViewModel and manages state.
 *
 * This is the only stateful composable in the hierarchy following the 4-tier pattern:
 * Container -> Screen -> Content -> Component
 *
 * US-4: presetDate と presetChannelsJson が渡された場合は LoadWithPresets を発行し、
 *       プリセット付きでタイムラインを読み込む。
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display), US-2 (Channel Add/Remove), US-4 (External App Navigation)
 */
@Composable
fun TimelineSyncContainer(
    modifier: Modifier = Modifier,
    presetDate: String? = null,
    presetChannelsJson: String? = null,
    onNavigateToHome: () -> Unit = {},
    onNavigateToChannels: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: TimelineSyncViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current

    // US-4: プリセットがある場合はLoadWithPresetsを、ない場合はLoadScreenを発行する
    LaunchedEffect(Unit) {
        if (presetChannelsJson != null && presetDate != null) {
            val parsedDate = runCatching { LocalDate.parse(presetDate) }.getOrNull()
            if (parsedDate != null) {
                viewModel.handleIntent(
                    TimelineSyncIntent.LoadWithPresets(
                        presetChannelsJson = presetChannelsJson,
                        presetDate = parsedDate,
                    ),
                )
            } else {
                viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
            }
        } else {
            viewModel.handleIntent(TimelineSyncIntent.LoadScreen)
        }
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is TimelineSyncSideEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        actionLabel = "閉じる",
                        duration = SnackbarDuration.Short,
                    )
                }

                is TimelineSyncSideEffect.NavigateToExternalApp -> {
                    try {
                        uriHandler.openUri(sideEffect.deepLinkUri)
                    } catch (_: Exception) {
                        try {
                            uriHandler.openUri(sideEffect.fallbackUrl)
                        } catch (_: Exception) {
                            snackbarHostState.showSnackbar(
                                message = "外部アプリを開けませんでした",
                                duration = SnackbarDuration.Short,
                            )
                        }
                    }
                }

                // Story 2: Channel Add/Remove
                is TimelineSyncSideEffect.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "${sideEffect.channelName}を削除しました",
                        actionLabel = "元に戻す",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        viewModel.handleIntent(TimelineSyncIntent.UndoRemoveChannel)
                    }
                }

                is TimelineSyncSideEffect.ShowChannelAddError -> {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        duration = SnackbarDuration.Short,
                    )
                }

                // Channel Follow (US-2)
                is TimelineSyncSideEffect.ShowFollowFeedback -> {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    TimelineSyncScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}
