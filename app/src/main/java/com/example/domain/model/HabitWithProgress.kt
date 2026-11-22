package com.example.domain.model

import com.example.data.local.HabitEntity

data class HabitWithProgress(
    val entity: HabitEntity,
    val category: HabitCategory,
    val timeOfDay: HabitTimeOfDay,
    val habitColor: HabitColor,
    val logsByDate: Map<String, Int>,
    val selectedDateCount: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletions: Int,
    val completionRate30Days: Int
) {
    val id: Long get() = entity.id
    val title: String get() = entity.title
    val cue: String get() = entity.cue
    val targetCount: Int get() = entity.targetCount.coerceAtLeast(1)
    val unit: String get() = entity.unit
    val isCounter: Boolean get() = targetCount > 1
    val isCompletedOnSelectedDate: Boolean get() = selectedDateCount >= targetCount
    val selectedDateFraction: Float
        get() = (selectedDateCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f)
}

data class DailyCompletionSummary(
    val dateKey: String,
    val completedCount: Int,
    val totalHabits: Int
) {
    val ratio: Float
        get() = if (totalHabits <= 0) 0f else (completedCount.toFloat() / totalHabits.toFloat()).coerceIn(0f, 1f)
}
