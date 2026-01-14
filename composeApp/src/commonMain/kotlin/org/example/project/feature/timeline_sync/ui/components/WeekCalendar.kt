package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * Week calendar component with horizontal scroll and date selection.
 *
 * Displays 7 days in a horizontal row with navigation arrows.
 * Selected date is highlighted with blue background.
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Previous week button
        IconButton(
            onClick = onNavigateToPreviousWeek,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous week",
            )
        }

        // Scrollable week days
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            weekDays.forEach { date ->
                DayItem(
                    date = date,
                    isSelected = date == selectedDate,
                    onClick = { onDateSelected(date) },
                )
            }
        }

        // Next week button
        IconButton(
            onClick = onNavigateToNextWeek,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next week",
            )
        }
    }
}

/**
 * Individual day item in the calendar.
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
            .width(48.dp)
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Day of week (MON, TUE, etc.)
        Text(
            text = date.dayOfWeek.toShortString(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.8f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )

        // Day number
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
 * Extension to get short day of week string (MON, TUE, etc.)
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
