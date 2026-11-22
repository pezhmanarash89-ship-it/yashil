package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, createdAtMillis ASC")
    fun observeAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit_logs")
    fun observeAllLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY completedAtMillis DESC LIMIT 50")
    fun observeRecentFocusSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateKey = :dateKey LIMIT 1")
    suspend fun getLog(habitId: Long, dateKey: String): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun deleteLog(habitId: Long, dateKey: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long
}
