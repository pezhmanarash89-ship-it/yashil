package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.HabitDateUtils
import com.example.ui.viewmodel.FocusTimerState
import com.example.ui.viewmodel.HabitDashboardState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FocusScreen(
    dashboardState: HabitDashboardState,
    focusState: FocusTimerState,
    onSelectPreset: (minutes: Int, isBreak: Boolean) -> Unit,
    onLinkHabit: (Long?) -> Unit,
    onToggleRunning: () -> Unit,
    onReset: () -> Unit,
    onCompleteNow: () -> Unit,
    onToggleAutoComplete: (Boolean) -> Unit,
    onDismissBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val readyState = dashboardState as? HabitDashboardState.Ready
    val habits = readyState?.allHabits.orEmpty()
    val sessions = readyState?.focusSessions.orEmpty()
    val autoComplete = readyState?.autoCompleteFocusHabit ?: true

    val activeAccent = if (focusState.isBreakMode) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val animatedProgress by animateFloatAsState(
        targetValue = focusState.progressFraction,
        animationSpec = tween(durationMillis = 450),
        label = "focus_arc"
    )

    val mins = focusState.remainingSeconds / 60
    val secs = focusState.remainingSeconds % 60
    val formattedClock = HabitDateUtils.toPersianDigits(
        String.format(Locale.US, "%02d:%02d", mins, secs)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 104.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "focus_header") {
            Column {
                Text(
                    text = stringResource(R.string.focus_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.focus_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "saved_banner") {
            AnimatedVisibility(visible = focusState.showSavedBanner) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.focus_session_saved),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onDismissBanner) {
                            Text("باشه")
                        }
                    }
                }
            }
        }

        // Preset durations
        item(key = "presets") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !focusState.isBreakMode && focusState.selectedDurationMinutes == 15,
                    onClick = { onSelectPreset(15, false) },
                    label = { Text(stringResource(R.string.focus_duration_15)) },
                    modifier = Modifier.testTag("focus_preset_15")
                )
                FilterChip(
                    selected = !focusState.isBreakMode && focusState.selectedDurationMinutes == 25,
                    onClick = { onSelectPreset(25, false) },
                    label = { Text(stringResource(R.string.focus_duration_25)) },
                    modifier = Modifier.testTag("focus_preset_25")
                )
                FilterChip(
                    selected = !focusState.isBreakMode && focusState.selectedDurationMinutes == 45,
                    onClick = { onSelectPreset(45, false) },
                    label = { Text(stringResource(R.string.focus_duration_45)) },
                    modifier = Modifier.testTag("focus_preset_45")
                )
                FilterChip(
                    selected = focusState.isBreakMode && focusState.selectedDurationMinutes == 5,
                    onClick = { onSelectPreset(5, true) },
                    label = { Text(stringResource(R.string.focus_duration_break)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Coffee,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("focus_preset_break")
                )
            }
        }

        // Timer Dial Card
        item(key = "timer_card") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = activeAccent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = stringResource(
                                if (focusState.isBreakMode) R.string.focus_mode_break else R.string.focus_mode_work
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = activeAccent,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Box(
                        modifier = Modifier.size(216.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokePx = 14.dp.toPx()
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = activeAccent,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formattedClock,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val linkedName = habits.firstOrNull { it.id == focusState.linkedHabitId }?.title
                            if (!focusState.isBreakMode && linkedName != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = linkedName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("focus_reset_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = stringResource(R.string.focus_reset),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onToggleRunning,
                            modifier = Modifier
                                .height(54.dp)
                                .width(156.dp)
                                .testTag("focus_play_pause_button"),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = activeAccent)
                        ) {
                            Icon(
                                imageVector = if (focusState.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(
                                    when {
                                        focusState.isRunning -> R.string.focus_pause
                                        focusState.remainingSeconds < focusState.totalSeconds -> R.string.focus_resume
                                        else -> R.string.focus_start
                                    }
                                ),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    if (!focusState.isBreakMode) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onCompleteNow,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("focus_complete_now_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TaskAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.focus_complete_now))
                        }
                    }
                }
            }
        }

        // Link to a habit
        if (!focusState.isBreakMode) {
            item(key = "link_habit_section") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.focus_link_habit),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = focusState.linkedHabitId == null,
                                onClick = { onLinkHabit(null) },
                                label = { Text(stringResource(R.string.focus_no_habit_linked)) }
                            )
                            habits.forEach { habit ->
                                FilterChip(
                                    selected = focusState.linkedHabitId == habit.id,
                                    onClick = { onLinkHabit(habit.id) },
                                    label = { Text(habit.title) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = habit.category.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }

                        if (focusState.linkedHabitId != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.focus_auto_complete_toggle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = autoComplete,
                                    onCheckedChange = onToggleAutoComplete,
                                    modifier = Modifier.testTag("focus_auto_complete_switch")
                                )
                            }
                        }
                    }
                }
            }
        }

        item(key = "history_header") {
            Text(
                text = stringResource(R.string.focus_history_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (sessions.isEmpty()) {
            item(key = "history_empty") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = stringResource(R.string.focus_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(items = sessions, key = { it.id }) { session ->
                val timeFmt = SimpleDateFormat("HH:mm", Locale.US)
                val timeStr = HabitDateUtils.toPersianDigits(timeFmt.format(Date(session.completedAtMillis)))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = session.habitTitle ?: stringResource(R.string.focus_no_habit_linked),
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${HabitDateUtils.jalaliDayMonthLabel(session.dateKey)} • $timeStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${HabitDateUtils.toPersianDigits(session.durationMinutes)} دقیقه",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
