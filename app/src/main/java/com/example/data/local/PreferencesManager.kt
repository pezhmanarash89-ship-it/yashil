package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habit_pulse_prefs")

class PreferencesManager(private val context: Context) {
    private object Keys {
        val DARK_MODE_OVERRIDE = booleanPreferencesKey("dark_mode_override")
        val HAS_THEME_OVERRIDE = booleanPreferencesKey("has_theme_override")
        val AUTO_COMPLETE_FOCUS_HABIT = booleanPreferencesKey("auto_complete_focus_habit")
    }

    val isDarkThemeOverride: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        if (prefs[Keys.HAS_THEME_OVERRIDE] == true) {
            prefs[Keys.DARK_MODE_OVERRIDE] ?: false
        } else {
            null
        }
    }

    val autoCompleteFocusHabit: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.AUTO_COMPLETE_FOCUS_HABIT] ?: true
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAS_THEME_OVERRIDE] = true
            prefs[Keys.DARK_MODE_OVERRIDE] = isDark
        }
    }

    suspend fun setAutoCompleteFocusHabit(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_COMPLETE_FOCUS_HABIT] = enabled
        }
    }
}
