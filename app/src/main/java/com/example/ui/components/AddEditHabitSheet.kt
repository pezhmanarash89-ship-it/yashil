package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.HabitCategory
import com.example.domain.model.HabitColor
import com.example.domain.model.HabitTimeOfDay
import com.example.domain.model.HabitWithProgress

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditHabitSheet(
    editingHabit: HabitWithProgress?,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        editingId: Long?,
        title: String,
        cue: String,
        category: HabitCategory,
        timeOfDay: HabitTimeOfDay,
        color: HabitColor,
        targetCount: Int,
        unit: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember(editingHabit) { mutableStateOf(editingHabit?.title ?: "") }
    var cue by remember(editingHabit) { mutableStateOf(editingHabit?.cue ?: "") }
    var isCounter by remember(editingHabit) { mutableStateOf(editingHabit?.isCounter ?: false) }
    var targetText by remember(editingHabit) {
        mutableStateOf((editingHabit?.targetCount?.takeIf { it > 1 } ?: 8).toString())
    }
    var unit by remember(editingHabit) { mutableStateOf(editingHabit?.unit ?: "") }
    var selectedCategory by remember(editingHabit) {
        mutableStateOf(editingHabit?.category ?: HabitCategory.HEALTH)
    }
    var selectedTime by remember(editingHabit) {
        mutableStateOf(editingHabit?.timeOfDay ?: HabitTimeOfDay.MORNING)
    }
    var selectedColor by remember(editingHabit) {
        mutableStateOf(editingHabit?.habitColor ?: HabitColor.EMERALD)
    }
    var titleError by remember { mutableStateOf(false) }
    var targetError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = stringResource(
                    if (editingHabit == null) R.string.sheet_title_new else R.string.sheet_title_edit
                ),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (titleError && it.isNotBlank()) titleError = false
                },
                label = { Text(stringResource(R.string.field_habit_title)) },
                placeholder = { Text(stringResource(R.string.field_habit_title_hint)) },
                singleLine = true,
                isError = titleError,
                supportingText = if (titleError) {
                    { Text(stringResource(R.string.error_title_required)) }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = cue,
                onValueChange = { cue = it },
                label = { Text(stringResource(R.string.field_habit_cue)) },
                placeholder = { Text(stringResource(R.string.field_habit_cue_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_cue_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.section_habit_type),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !isCounter,
                    onClick = { isCounter = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("type_binary_btn")
                ) {
                    Text(
                        text = stringResource(R.string.type_binary),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                SegmentedButton(
                    selected = isCounter,
                    onClick = { isCounter = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("type_counter_btn")
                ) {
                    Text(
                        text = stringResource(R.string.type_counter),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            AnimatedVisibility(visible = isCounter) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { input ->
                            if (input.length <= 3 && input.all { it.isDigit() }) {
                                targetText = input
                                targetError = false
                            }
                        },
                        label = { Text(stringResource(R.string.field_target_count)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = targetError,
                        supportingText = if (targetError) {
                            { Text(stringResource(R.string.error_target_invalid)) }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("habit_target_input")
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text(stringResource(R.string.field_unit)) },
                        placeholder = { Text(stringResource(R.string.field_unit_hint)) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("habit_unit_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.section_time_of_day),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HabitTimeOfDay.entries.forEach { time ->
                    FilterChip(
                        selected = selectedTime == time,
                        onClick = { selectedTime = time },
                        label = { Text(stringResource(time.labelRes)) },
                        leadingIcon = {
                            Icon(
                                imageVector = time.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.section_category),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HabitCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(stringResource(category.labelRes)) },
                        leadingIcon = {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.section_color),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HabitColor.entries.forEach { colorOption ->
                    val swatchColor = colorOption.color(isDarkTheme)
                    val isSelected = selectedColor == colorOption
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                } else Modifier
                            )
                            .clickable { selectedColor = colorOption }
                            .testTag("color_swatch_${colorOption.key}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = colorOption.key,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("cancel_habit_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }

                Button(
                    onClick = {
                        val trimmedTitle = title.trim()
                        if (trimmedTitle.isEmpty()) {
                            titleError = true
                            return@Button
                        }
                        val parsedTarget = if (isCounter) {
                            targetText.toIntOrNull()
                        } else {
                            1
                        }
                        if (parsedTarget == null || parsedTarget < 1 || parsedTarget > 999) {
                            targetError = true
                            return@Button
                        }
                        onSave(
                            editingHabit?.id,
                            trimmedTitle,
                            cue.trim(),
                            selectedCategory,
                            selectedTime,
                            selectedColor,
                            parsedTarget,
                            unit.trim()
                        )
                    },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(50.dp)
                        .testTag("save_habit_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        stringResource(
                            if (editingHabit == null) R.string.action_save else R.string.action_update
                        )
                    )
                }
            }
        }
    }
}
