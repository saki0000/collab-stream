package org.example.project.feature.subscription.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Spacing
import org.example.project.feature.subscription.ScreenState
import org.example.project.feature.subscription.SubscriptionIntent
import org.example.project.feature.subscription.SubscriptionUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * サブスクリプション管理画面のScreen層（Stateless）。
 *
 * 画面全体のレイアウトを定義し、[ScreenState] に応じて適切なContentを表示する。
 * Loading / FreePlan / ProPlan / Error の4状態に対応する。
 *
 * 4層構造: Container -> Screen -> Content -> Component
 *
 * Feature: サブスクリプション管理 (US-4)
 * Specification: feature/subscription/SPECIFICATION.md
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    uiState: SubscriptionUiState,
    onIntent: (SubscriptionIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("サブスクリプション") },
                navigationIcon = {
                    IconButton(
                        onClick = { onIntent(SubscriptionIntent.NavigateBack) },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "前の画面に戻る",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (val state = uiState.screenState) {
                ScreenState.Loading -> {
                    // ローディング状態
                    SubscriptionLoadingState()
                }

                ScreenState.FreePlan -> {
                    // Freeプラン表示
                    FreePlanContent(
                        isPurchasing = uiState.isPurchasing,
                        isRestoring = uiState.isRestoring,
                        onPurchaseClick = { onIntent(SubscriptionIntent.PurchaseProPlan) },
                        onRestoreClick = { onIntent(SubscriptionIntent.RestorePurchases) },
                    )
                }

                is ScreenState.ProPlan -> {
                    // Proプラン表示
                    ProPlanContent(
                        expiresAtMillis = state.expiresAtMillis,
                        willRenew = state.willRenew,
                    )
                }

                is ScreenState.Error -> {
                    // エラー状態
                    SubscriptionErrorState(
                        message = state.message,
                        onRetry = { onIntent(SubscriptionIntent.Retry) },
                    )
                }
            }
        }
    }
}

/**
 * ローディング状態の表示。
 */
@Composable
private fun SubscriptionLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * エラー状態の表示。
 */
@Composable
private fun SubscriptionErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Button(
            onClick = onRetry,
        ) {
            Text("再試行")
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun SubscriptionScreenLoadingPreview() {
    AppTheme {
        SubscriptionScreen(
            uiState = SubscriptionUiState(
                screenState = ScreenState.Loading,
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun SubscriptionScreenFreePlanPreview() {
    AppTheme {
        SubscriptionScreen(
            uiState = SubscriptionUiState(
                screenState = ScreenState.FreePlan,
                isPurchasing = false,
                isRestoring = false,
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun SubscriptionScreenFreePlanPurchasingPreview() {
    AppTheme {
        SubscriptionScreen(
            uiState = SubscriptionUiState(
                screenState = ScreenState.FreePlan,
                isPurchasing = true,
                isRestoring = false,
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun SubscriptionScreenProPlanPreview() {
    AppTheme {
        SubscriptionScreen(
            uiState = SubscriptionUiState(
                screenState = ScreenState.ProPlan(
                    // 2025年3月31日 00:00:00 UTC のエポックミリ秒（固定値）
                    expiresAtMillis = 1743379200000L,
                    willRenew = true,
                ),
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun SubscriptionScreenProPlanNoRenewPreview() {
    AppTheme {
        SubscriptionScreen(
            uiState = SubscriptionUiState(
                screenState = ScreenState.ProPlan(
                    expiresAtMillis = 1743379200000L,
                    willRenew = false,
                ),
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun SubscriptionScreenErrorPreview() {
    AppTheme {
        SubscriptionScreen(
            uiState = SubscriptionUiState(
                screenState = ScreenState.Error(
                    message = "ネットワークエラーが発生しました。\nインターネット接続を確認してください。",
                ),
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
