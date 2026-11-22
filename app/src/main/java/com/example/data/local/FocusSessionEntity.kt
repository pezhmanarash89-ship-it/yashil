package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long? = null,
    val habitTitle: String? = null,
    val durationMinutes: Int,
    val completedAtMillis: Long = System.currentTimeMillis(),
    val dateKey: String
)
