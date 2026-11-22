package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.HabitDateUtils
import com.example.domain.model.HabitWithProgress

@Composable
fun HabitCard(
    habit: HabitWithProgress,
    isDarkTheme: Boolean,
    onToggleBinary: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val accentColor = habit.habitColor.color(isDarkTheme)
    val isDone = habit.isCompletedOnSelectedDate

    val cardBg by animateColorAsState(
        targetValue = if (isDone) {
            accentColor.copy(alpha = if (isDarkTheme) 0.16f else 0.08f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "habit_card_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isDone) {
            accentColor.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.outline
        },
        label = "habit_card_border"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (isDone) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "habit_check_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onOpenDetail() }
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (isDone) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category icon badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = habit.category.icon,
                        contentDescription = stringResource(habit.category.labelRes),
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title, cue, metadata
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isDone) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (habit.cue.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = habit.cue,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Time of day pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = habit.timeOfDay.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(habit.timeOfDay.labelRes),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Streak badge
                        if (habit.currentStreak > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = stringResource(
                                        R.string.streak_days_format,
                                        HabitDateUtils.toPersianDigits(habit.currentStreak).toIntOrNull()
                                            ?: habit.currentStreak
                                    ).let {
                                        "${HabitDateUtils.toPersianDigits(habit.currentStreak)} روز"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action control: binary check circle or counter -/+ controls
                if (!habit.isCounter) {
                    val toggleCd = stringResource(R.string.habit_toggle_cd)
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .scale(checkScale)
                            .clip(CircleShape)
                            .background(if (isDone) accentColor else Color.Transparent)
                            .border(
                                width = 2.dp,
                                color = if (isDone) accentColor else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleBinary()
                            }
                            .testTag("habit_toggle_${habit.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = toggleCd,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = toggleCd,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onDecrement()
                            },
                            enabled = habit.selectedDateCount > 0,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("habit_dec_${habit.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = stringResource(R.string.habit_decrement_cd),
                                tint = if (habit.selectedDateCount > 0) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                }
                            )
                        }

                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onIncrement()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDone) accentColor else accentColor.copy(alpha = 0.14f)
                                )
                                .testTag("habit_inc_${habit.id}")
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Rounded.Check else Icons.Rounded.Add,
                                contentDescription = stringResource(R.string.habit_increment_cd),
                                tint = if (isDone) Color.White else accentColor
                            )
                        }
                    }
                }
            }

            // Counter progress bar
            if (habit.isCounter) {
                Spacer(modifier = Modifier.height(10.dp))
                val animatedProgress by animateFloatAsState(
                    targetValue = habit.selectedDateFraction,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "counter_bar"
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accentColor,
                        trackColor = accentColor.copy(alpha = 0.16f),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    val unitLabel = habit.unit.ifBlank { "" }
                    Text(
                        text = "${HabitDateUtils.toPersianDigits(habit.selectedDateCount)} / ${HabitDateUtils.toPersianDigits(habit.targetCount)} $unitLabel".trim(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isDone) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
