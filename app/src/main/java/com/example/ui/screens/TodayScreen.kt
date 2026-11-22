package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.HabitEntity
import com.example.domain.model.HabitCategory
import com.example.domain.model.HabitColor
import com.example.domain.model.HabitDateUtils
import com.example.domain.model.HabitTimeOfDay
import com.example.domain.model.HabitWithProgress
import com.example.ui.components.HabitCard
import com.example.ui.components.HabitProgressRing
import com.example.ui.components.WeekDateStrip
import com.example.ui.viewmodel.HabitDashboardState

@Composable
fun TodayScreen(
    state: HabitDashboardState,
    isDarkTheme: Boolean,
    onSelectDate: (String) -> Unit,
    onSelectTimeFilter: (HabitTimeOfDay?) -> Unit,
    onToggleHabit: (HabitWithProgress) -> Unit,
    onIncrementHabit: (HabitWithProgress) -> Unit,
    onDecrementHabit: (HabitWithProgress) -> Unit,
    onOpenHabitDetail: (Long) -> Unit,
    onOpenNewHabit: () -> Unit,
    onAddStarterPack: (List<HabitEntity>) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        HabitDashboardState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is HabitDashboardState.Ready -> {
            val todayKey = HabitDateUtils.todayKey()
            val yesterdayKey = HabitDateUtils.daysAgoKey(1, todayKey)

            val starterWaterTitle = stringResource(R.string.starter_water_title)
            val starterWaterCue = stringResource(R.string.starter_water_cue)
            val starterWaterUnit = stringResource(R.string.starter_water_unit)
            val starterReadTitle = stringResource(R.string.starter_read_title)
            val starterReadCue = stringResource(R.string.starter_read_cue)
            val starterReadUnit = stringResource(R.string.starter_read_unit)
            val starterMeditateTitle = stringResource(R.string.starter_meditate_title)
            val starterMeditateCue = stringResource(R.string.starter_meditate_cue)
            val starterWalkTitle = stringResource(R.string.starter_walk_title)
            val starterWalkCue = stringResource(R.string.starter_walk_cue)
            val starterWalkUnit = stringResource(R.string.starter_walk_unit)

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 104.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Editorial Hero Banner with Artwork + Progress Ring
                item(key = "hero_banner") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(168.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_hero_habits),
                                contentDescription = stringResource(R.string.today_hero_title),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xD90B2E2E),
                                                Color(0xA60F3D3E),
                                                Color(0x660F3D3E)
                                            )
                                        )
                                    )
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val dateBadge = when (state.selectedDateKey) {
                                        todayKey -> stringResource(R.string.today_label)
                                        yesterdayKey -> stringResource(R.string.yesterday_label)
                                        else -> HabitDateUtils.jalaliDayMonthLabel(state.selectedDateKey)
                                    }
                                    Surface(
                                        color = Color.White.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "$dateBadge • ${HabitDateUtils.jalaliFullHeader(state.selectedDateKey)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = stringResource(R.string.today_hero_title),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val subtitle = when {
                                        state.selectedDayTotalCount == 0 ->
                                            stringResource(R.string.today_hero_subtitle_empty)
                                        state.selectedDayCompletedCount == state.selectedDayTotalCount ->
                                            stringResource(R.string.today_hero_subtitle_done)
                                        else ->
                                            "${HabitDateUtils.toPersianDigits(state.selectedDayCompletedCount)} از ${HabitDateUtils.toPersianDigits(state.selectedDayTotalCount)} عادت تکمیل شده"
                                    }
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                HabitProgressRing(
                                    completed = state.selectedDayCompletedCount,
                                    total = state.selectedDayTotalCount,
                                    size = 86.dp,
                                    strokeWidth = 9.dp,
                                    progressColor = Color(0xFFF4A261),
                                    trackColor = Color.White.copy(alpha = 0.25f),
                                    labelColor = Color.White
                                )
                            }
                        }
                    }
                }

                // 7-day interactive date strip
                item(key = "week_strip") {
                    WeekDateStrip(
                        days = state.recent7Days,
                        selectedDateKey = state.selectedDateKey,
                        todayKey = todayKey,
                        onSelectDate = onSelectDate
                    )
                }

                // Time-of-day filter chips
                if (state.allHabits.isNotEmpty()) {
                    item(key = "time_filters") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.selectedTimeFilter == null,
                                onClick = { onSelectTimeFilter(null) },
                                label = { Text(stringResource(R.string.filter_all)) },
                                modifier = Modifier.testTag("filter_all")
                            )
                            HabitTimeOfDay.entries.forEach { time ->
                                FilterChip(
                                    selected = state.selectedTimeFilter == time,
                                    onClick = {
                                        onSelectTimeFilter(
                                            if (state.selectedTimeFilter == time) null else time
                                        )
                                    },
                                    label = { Text(stringResource(time.labelRes)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = time.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("filter_${time.key}")
                                )
                            }
                        }
                    }
                }

                // Empty State vs Habit Cards
                if (state.allHabits.isEmpty()) {
                    item(key = "empty_all") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_empty_habits),
                                    contentDescription = stringResource(R.string.empty_habits_title),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.empty_habits_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.empty_habits_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = onOpenNewHabit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("empty_create_habit_button"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.btn_create_first_habit))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        onAddStarterPack(
                                            listOf(
                                                HabitEntity(
                                                    title = starterWaterTitle,
                                                    cue = starterWaterCue,
                                                    categoryKey = HabitCategory.HEALTH.key,
                                                    timeOfDayKey = HabitTimeOfDay.ANYTIME.key,
                                                    colorKey = HabitColor.OCEAN.key,
                                                    targetCount = 8,
                                                    unit = starterWaterUnit,
                                                    sortOrder = 0
                                                ),
                                                HabitEntity(
                                                    title = starterMeditateTitle,
                                                    cue = starterMeditateCue,
                                                    categoryKey = HabitCategory.MINDFULNESS.key,
                                                    timeOfDayKey = HabitTimeOfDay.MORNING.key,
                                                    colorKey = HabitColor.EMERALD.key,
                                                    targetCount = 1,
                                                    unit = "",
                                                    sortOrder = 1
                                                ),
                                                HabitEntity(
                                                    title = starterReadTitle,
                                                    cue = starterReadCue,
                                                    categoryKey = HabitCategory.LEARNING.key,
                                                    timeOfDayKey = HabitTimeOfDay.EVENING.key,
                                                    colorKey = HabitColor.TERRACOTTA.key,
                                                    targetCount = 15,
                                                    unit = starterReadUnit,
                                                    sortOrder = 2
                                                ),
                                                HabitEntity(
                                                    title = starterWalkTitle,
                                                    cue = starterWalkCue,
                                                    categoryKey = HabitCategory.FITNESS.key,
                                                    timeOfDayKey = HabitTimeOfDay.AFTERNOON.key,
                                                    colorKey = HabitColor.SAGE.key,
                                                    targetCount = 30,
                                                    unit = starterWalkUnit,
                                                    sortOrder = 3
                                                )
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("add_starter_pack_button"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.btn_add_starter_pack))
                                }
                            }
                        }
                    }
                } else if (state.filteredHabits.isEmpty()) {
                    item(key = "empty_filtered") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.empty_filtered_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.empty_filtered_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = state.filteredHabits,
                        key = { _, habit -> habit.id }
                    ) { _, habit ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 })
                        ) {
                            HabitCard(
                                habit = habit,
                                isDarkTheme = isDarkTheme,
                                onToggleBinary = { onToggleHabit(habit) },
                                onIncrement = { onIncrementHabit(habit) },
                                onDecrement = { onDecrementHabit(habit) },
                                onOpenDetail = { onOpenHabitDetail(habit.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
