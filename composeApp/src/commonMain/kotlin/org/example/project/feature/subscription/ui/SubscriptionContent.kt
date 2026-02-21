@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.feature.subscription.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Spacing
import org.example.project.feature.subscription.ScreenState
import org.example.project.feature.subscription.SubscriptionUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Freeプランのコンテンツ表示。
 *
 * 「Proにアップグレード」ボタンと「購入を復元」ボタンを表示する。
 * 購入中・復元中はローディングインジケーターを表示してUIをブロッキングする。
 *
 * 4層構造: Container -> Screen -> Content -> Component
 *
 * Feature: サブスクリプション管理 (US-4)
 * Specification: feature/subscription/SPECIFICATION.md
 */
@Composable
fun FreePlanContent(
    isPurchasing: Boolean,
    isRestoring: Boolean,
    onPurchaseClick: () -> Unit,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 購入中または復元中はUIをブロック
    val isProcessing = isPurchasing || isRestoring

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 現在のプランラベル
        PlanHeaderSection(
            title = "Freeプラン",
            subtitle = "現在ご利用中のプラン",
            isPro = false,
        )

        Spacer(modifier = Modifier.height(Spacing.xxl))

        // プラン比較カード
        PlanComparisonCard()

        Spacer(modifier = Modifier.height(Spacing.xxl))

        if (isProcessing) {
            // 処理中のローディング表示
            val processingText = if (isPurchasing) "購入処理中..." else "復元処理中..."
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = processingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            // アップグレードボタン
            Button(
                onClick = onPurchaseClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Proにアップグレード",
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // 購入復元ボタン
            OutlinedButton(
                onClick = onRestoreClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "購入を復元",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

/**
 * Proプランのコンテンツ表示。
 *
 * 有効期限と自動更新状態を表示する。
 * [expiresAtMillis] が null の場合は有効期限を表示しない。
 *
 * 4層構造: Container -> Screen -> Content -> Component
 *
 * Feature: サブスクリプション管理 (US-4)
 * Specification: feature/subscription/SPECIFICATION.md
 */
@Composable
fun ProPlanContent(
    expiresAtMillis: Long?,
    willRenew: Boolean,
    modifier: Modifier = Modifier,
) {
    // 有効期限のフォーマット（Screen/Content/Componentでは Clock.System 使用禁止）
    val expiresDateText = remember(expiresAtMillis) {
        expiresAtMillis?.let { millis ->
            val instant = Instant.fromEpochMilliseconds(millis)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val year = localDateTime.year
            val month = localDateTime.monthNumber
            val day = localDateTime.dayOfMonth
            "${year}年${month}月${day}日まで"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 現在のプランラベル
        PlanHeaderSection(
            title = "Proプラン",
            subtitle = "ご利用中のプラン",
            isPro = true,
        )

        Spacer(modifier = Modifier.height(Spacing.xxl))

        // プラン詳細カード
        ProPlanDetailCard(
            expiresDateText = expiresDateText,
            willRenew = willRenew,
        )

        Spacer(modifier = Modifier.height(Spacing.xxl))

        Text(
            text = "すべての機能をご利用いただけます",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ============================================
// 内部コンポーネント
// ============================================

/**
 * プランのヘッダーセクション。
 * アイコン・プラン名・サブタイトルを表示する。
 */
@Composable
private fun PlanHeaderSection(
    title: String,
    subtitle: String,
    isPro: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = if (isPro) Icons.Default.Star else Icons.Default.CheckCircle,
            contentDescription = null, // タイトルテキストで説明されるため装飾扱い
            tint = if (isPro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(64.dp),
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * FreeプランとProプランの機能比較カード。
 */
@Composable
private fun PlanComparisonCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
        ) {
            Text(
                text = "Proプランの特典",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            PlanFeatureItem(text = "すべてのプレミアム機能へのアクセス")

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            )

            PlanFeatureItem(text = "広告なし体験")

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            )

            PlanFeatureItem(text = "優先サポート")
        }
    }
}

/**
 * 機能アイテムの1行表示。
 * チェックマークアイコンとテキストを横に並べる。
 */
@Composable
private fun PlanFeatureItem(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null, // テキストで内容が説明されるため装飾扱い
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Proプランの詳細情報カード。
 * 有効期限と自動更新状態を表示する。
 */
@Composable
private fun ProPlanDetailCard(
    expiresDateText: String?,
    willRenew: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
        ) {
            Text(
                text = "プラン詳細",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // 有効期限（nullの場合は非表示）
            if (expiresDateText != null) {
                ProPlanDetailRow(
                    label = "有効期限",
                    value = expiresDateText,
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Spacing.sm),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                )
            }

            // 自動更新状態
            ProPlanDetailRow(
                label = "自動更新",
                value = if (willRenew) "有効" else "無効",
            )
        }
    }
}

/**
 * プラン詳細の1行表示。
 * ラベルと値を左右に配置する。
 */
@Composable
private fun ProPlanDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun FreePlanContentPreview() {
    AppTheme {
        FreePlanContent(
            isPurchasing = false,
            isRestoring = false,
            onPurchaseClick = {},
            onRestoreClick = {},
        )
    }
}

@Preview
@Composable
private fun FreePlanContentPurchasingPreview() {
    AppTheme {
        FreePlanContent(
            isPurchasing = true,
            isRestoring = false,
            onPurchaseClick = {},
            onRestoreClick = {},
        )
    }
}

@Preview
@Composable
private fun FreePlanContentRestoringPreview() {
    AppTheme {
        FreePlanContent(
            isPurchasing = false,
            isRestoring = true,
            onPurchaseClick = {},
            onRestoreClick = {},
        )
    }
}

@Preview
@Composable
private fun ProPlanContentWithExpiryAndRenewPreview() {
    AppTheme {
        ProPlanContent(
            // 2025年3月31日 00:00:00 UTC のエポックミリ秒（固定値）
            expiresAtMillis = 1743379200000L,
            willRenew = true,
        )
    }
}

@Preview
@Composable
private fun ProPlanContentWithExpiryNoRenewPreview() {
    AppTheme {
        ProPlanContent(
            expiresAtMillis = 1743379200000L,
            willRenew = false,
        )
    }
}

@Preview
@Composable
private fun ProPlanContentNoExpiryPreview() {
    AppTheme {
        ProPlanContent(
            expiresAtMillis = null,
            willRenew = false,
        )
    }
}

