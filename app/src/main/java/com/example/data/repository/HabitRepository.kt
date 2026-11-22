package com.example.data.repository

import com.example.data.local.FocusSessionEntity
import com.example.data.local.HabitDao
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.domain.model.HabitDateUtils
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {

    val habitsFlow: Flow<List<HabitEntity>> = habitDao.observeAllHabits()
    val logsFlow: Flow<List<HabitLogEntity>> = habitDao.observeAllLogs()
    val focusSessionsFlow: Flow<List<FocusSessionEntity>> = habitDao.observeRecentFocusSessions()

    suspend fun createHabit(habit: HabitEntity): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteHabitById(habitId)
    }

    suspend fun toggleBinaryHabit(habit: HabitEntity, dateKey: String) {
        val existing = habitDao.getLog(habit.id, dateKey)
        if (existing != null && existing.count >= habit.targetCount) {
            habitDao.deleteLog(habit.id, dateKey)
        } else {
            habitDao.upsertLog(
                HabitLogEntity(
                    id = existing?.id ?: 0,
                    habitId = habit.id,
                    dateKey = dateKey,
                    count = habit.targetCount.coerceAtLeast(1),
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun adjustHabitCount(habit: HabitEntity, dateKey: String, delta: Int) {
        val existing = habitDao.getLog(habit.id, dateKey)
        val current = existing?.count ?: 0
        val next = (current + delta).coerceIn(0, habit.targetCount.coerceAtLeast(1))
        if (next <= 0) {
            habitDao.deleteLog(habit.id, dateKey)
        } else {
            habitDao.upsertLog(
                HabitLogEntity(
                    id = existing?.id ?: 0,
                    habitId = habit.id,
                    dateKey = dateKey,
                    count = next,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun markHabitCompletedForFocus(habitId: Long, dateKey: String) {
        val habit = habitDao.getHabitById(habitId) ?: return
        val existing = habitDao.getLog(habitId, dateKey)
        val current = existing?.count ?: 0
        val next = if (habit.targetCount > 1) {
            (current + 1).coerceAtMost(habit.targetCount)
        } else {
            1
        }
        habitDao.upsertLog(
            HabitLogEntity(
                id = existing?.id ?: 0,
                habitId = habitId,
                dateKey = dateKey,
                count = next,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordFocusSession(
        habitId: Long?,
        habitTitle: String?,
        durationMinutes: Int,
        autoCompleteHabit: Boolean
    ) {
        val today = HabitDateUtils.todayKey()
        habitDao.insertFocusSession(
            FocusSessionEntity(
                habitId = habitId,
                habitTitle = habitTitle,
                durationMinutes = durationMinutes,
                completedAtMillis = System.currentTimeMillis(),
                dateKey = today
            )
        )
        if (autoCompleteHabit && habitId != null) {
            markHabitCompletedForFocus(habitId, today)
        }
    }

    suspend fun addStarterHabits(templates: List<HabitEntity>) {
        habitDao.insertHabits(templates)
    }
}
