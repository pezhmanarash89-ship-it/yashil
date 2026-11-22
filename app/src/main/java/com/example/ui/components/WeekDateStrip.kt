package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.DailyCompletionSummary
import com.example.domain.model.HabitDateUtils

@Composable
fun WeekDateStrip(
    days: List<DailyCompletionSummary>,
    selectedDateKey: String,
    todayKey: String,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEach { day ->
            val isSelected = day.dateKey == selectedDateKey
            val isToday = day.dateKey == todayKey
            val weekday = HabitDateUtils.persianWeekdayShort(day.dateKey)
            val dayNum = HabitDateUtils.toPersianDigits(HabitDateUtils.jalaliDayNumber(day.dateKey))
            val fullLabel = HabitDateUtils.jalaliFullHeader(day.dateKey)

            val containerColor by animateColorAsState(
                targetValue = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    else -> MaterialTheme.colorScheme.surface
                },
                label = "date_strip_bg"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                label = "date_strip_fg"
            )
            val borderColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(containerColor)
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable { onSelectDate(day.dateKey) }
                    .padding(vertical = 10.dp, horizontal = 4.dp)
                    .testTag("date_pill_${day.dateKey}")
                    .semantics { contentDescription = fullLabel },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = weekday,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = if (isSelected) 0.9f else 0.65f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dayNum,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                val dotColor = when {
                    day.totalHabits > 0 && day.completedCount == day.totalHabits -> {
                        if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                    }
                    day.completedCount > 0 -> {
                        if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        else MaterialTheme.colorScheme.secondary
                    }
                    else -> contentColor.copy(alpha = 0.18f)
                }
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}
