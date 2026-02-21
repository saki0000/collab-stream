package org.example.project.feature.subscription.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.example.project.feature.subscription.SubscriptionIntent
import org.example.project.feature.subscription.SubscriptionSideEffect
import org.example.project.feature.subscription.SubscriptionViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * サブスクリプション管理画面のContainer層（Stateful）。
 *
 * ViewModelと接続し、SideEffectを処理する。
 * Navigation コールバックを受け取り、SideEffect に応じて遷移を実行する。
 *
 * 4層構造: Container -> Screen -> Content -> Component
 *
 * Feature: サブスクリプション管理 (US-4)
 * Specification: feature/subscription/SPECIFICATION.md
 */
@Composable
fun SubscriptionContainer(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubscriptionViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 画面初回表示時にサブスクリプション状態を読み込む
    LaunchedEffect(Unit) {
        viewModel.handleIntent(SubscriptionIntent.LoadSubscription)
    }

    // SideEffect を処理
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is SubscriptionSideEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        actionLabel = "閉じる",
                        duration = SnackbarDuration.Short,
                    )
                }

                is SubscriptionSideEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = sideEffect.message,
                        duration = SnackbarDuration.Short,
                    )
                }

                SubscriptionSideEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    SubscriptionScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}
