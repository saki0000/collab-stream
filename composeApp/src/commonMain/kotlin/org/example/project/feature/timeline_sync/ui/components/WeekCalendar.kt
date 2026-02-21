package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.example.project.core.theme.AppShapes
import org.example.project.core.theme.AppTheme
import org.example.project.core.theme.Dimensions
import org.example.project.core.theme.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 週カレンダーコンポーネント。横並び日付セレクターと日付選択機能を提供する。
 *
 * 7日分の日付を横並びで表示し、ナビゲーション矢印で週を移動できる。
 * 選択日はプライマリカラーでハイライト表示される。
 * 週の上部に月ラベルを表示し、週が月をまたぐ場合は「1月 - 2月」形式で表示する。
 * DayItemの幅はweight(1f)で均等配分し、画面幅に応じてレスポンシブに調整される。
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-1 (Timeline Display)
 */
@Composable
fun WeekCalendar(
    weekDays: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onNavigateToPreviousWeek: () -> Unit,
    onNavigateToNextWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 週の月ラベルを計算（月またぎ対応）
    val monthLabel = remember(weekDays) {
        if (weekDays.isEmpty()) return@remember ""
        val firstMonth = weekDays.first().monthNumber
        val lastMonth = weekDays.last().monthNumber
        if (firstMonth == lastMonth) {
            "${firstMonth}月"
        } else {
            "${firstMonth}月 - ${lastMonth}月"
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // 月ラベル（週の上部に表示）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 前の週ボタン
            IconButton(
                onClick = onNavigateToPreviousWeek,
                modifier = Modifier.size(Dimensions.iconXl),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "前の週に移動",
                )
            }

            // 週の日付（均等配分・レスポンシブ幅）
            // horizontalScrollを使用せず、weight(1f)で均等配分する
            // weekDaysは常に7日固定であることを前提とする
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                weekDays.forEach { date ->
                    DayItem(
                        date = date,
                        isSelected = date == selectedDate,
                        onClick = { onDateSelected(date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // 次の週ボタン
            IconButton(
                onClick = onNavigateToNextWeek,
                modifier = Modifier.size(Dimensions.iconXl),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "次の週に移動",
                )
            }
        }
    }
}

/**
 * カレンダーの個別日付アイテム。
 */
@Composable
private fun DayItem(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .padding(horizontal = Spacing.xxs)
            .clip(AppShapes.small)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 曜日（MON, TUE など）
        Text(
            text = date.dayOfWeek.toShortString(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.8f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )

        // 日付数字
        Text(
            text = date.day.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 曜日の短縮文字列を返す拡張関数（MON, TUE など）。
 */
private fun DayOfWeek.toShortString(): String = when (this) {
    DayOfWeek.MONDAY -> "MON"
    DayOfWeek.TUESDAY -> "TUE"
    DayOfWeek.WEDNESDAY -> "WED"
    DayOfWeek.THURSDAY -> "THU"
    DayOfWeek.FRIDAY -> "FRI"
    DayOfWeek.SATURDAY -> "SAT"
    DayOfWeek.SUNDAY -> "SUN"
    else -> ""
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun WeekCalendarPreview() {
    // 2024年1月15日（月曜）〜21日（日曜）
    val weekDays = listOf(
        LocalDate.parse("2024-01-15"),
        LocalDate.parse("2024-01-16"),
        LocalDate.parse("2024-01-17"),
        LocalDate.parse("2024-01-18"),
        LocalDate.parse("2024-01-19"),
        LocalDate.parse("2024-01-20"),
        LocalDate.parse("2024-01-21"),
    )
    val selectedDate = LocalDate.parse("2024-01-15")

    AppTheme {
        WeekCalendar(
            weekDays = weekDays,
            selectedDate = selectedDate,
            onDateSelected = {},
            onNavigateToPreviousWeek = {},
            onNavigateToNextWeek = {},
        )
    }
}

@Preview
@Composable
private fun WeekCalendarMonthBoundaryPreview() {
    // 月またぎの週（1月28日〜2月3日）
    val weekDays = listOf(
        LocalDate.parse("2024-01-29"),
        LocalDate.parse("2024-01-30"),
        LocalDate.parse("2024-01-31"),
        LocalDate.parse("2024-02-01"),
        LocalDate.parse("2024-02-02"),
        LocalDate.parse("2024-02-03"),
        LocalDate.parse("2024-02-04"),
    )
    val selectedDate = LocalDate.parse("2024-02-01")

    AppTheme {
        WeekCalendar(
            weekDays = weekDays,
            selectedDate = selectedDate,
            onDateSelected = {},
            onNavigateToPreviousWeek = {},
            onNavigateToNextWeek = {},
        )
    }
}
