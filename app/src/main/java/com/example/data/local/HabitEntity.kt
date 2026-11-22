package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val cue: String = "",
    val categoryKey: String = "health",
    val timeOfDayKey: String = "anytime",
    val colorKey: String = "emerald",
    val targetCount: Int = 1,
    val unit: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
