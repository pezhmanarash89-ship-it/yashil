package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.FocusSessionEntity
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.PreferencesManager
import com.example.data.repository.HabitRepository
import com.example.domain.model.DailyCompletionSummary
import com.example.domain.model.HabitCategory
import com.example.domain.model.HabitColor
import com.example.domain.model.HabitDateUtils
import com.example.domain.model.HabitTimeOfDay
import com.example.domain.model.HabitWithProgress
import com.example.domain.model.StreakCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface HabitDashboardState {
    data object Loading : HabitDashboardState
    data class Ready(
        val selectedDateKey: String,
        val selectedTimeFilter: HabitTimeOfDay?,
        val allHabits: List<HabitWithProgress>,
        val filteredHabits: List<HabitWithProgress>,
        val selectedDayCompletedCount: Int,
        val selectedDayTotalCount: Int,
        val recent7Days: List<DailyCompletionSummary>,
        val heatmap35Days: List<DailyCompletionSummary>,
        val totalCompletionsAllTime: Int,
        val bestActiveStreak: Int,
        val weeklyCompletionPercent: Int,
        val totalFocusMinutes: Int,
        val focusSessions: List<FocusSessionEntity>,
        val autoCompleteFocusHabit: Boolean
    ) : HabitDashboardState
}

data class FocusTimerState(
    val selectedDurationMinutes: Int = 25,
    val isBreakMode: Boolean = false,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val linkedHabitId: Long? = null,
    val showSavedBanner: Boolean = false
) {
    val progressFraction: Float
        get() = if (totalSeconds <= 0) 0f else 1f - (remainingSeconds.toFloat() / totalSeconds.toFloat())
}

class HabitViewModel(
    private val repository: HabitRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val selectedDateKey = MutableStateFlow(HabitDateUtils.todayKey())
    private val selectedTimeFilter = MutableStateFlow<HabitTimeOfDay?>(null)

    private val _focusState = MutableStateFlow(FocusTimerState())
    val focusState: StateFlow<FocusTimerState> = _focusState.asStateFlow()
    private var timerJob: Job? = null

    val isDarkThemeOverride: StateFlow<Boolean?> = preferencesManager.isDarkThemeOverride
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val filterFlow = combine(selectedDateKey, selectedTimeFilter) { dateKey, timeFilter ->
        dateKey to timeFilter
    }

    private val dataFlow = combine(
        repository.habitsFlow,
        repository.logsFlow,
        repository.focusSessionsFlow,
        preferencesManager.autoCompleteFocusHabit
    ) { habits, logs, sessions, autoComplete ->
        DataSnapshot(habits, logs, sessions, autoComplete)
    }

    val dashboardState: StateFlow<HabitDashboardState> = combine(
        dataFlow,
        filterFlow
    ) { snapshot, (dateKey, timeFilter) ->
        buildReadyState(snapshot, dateKey, timeFilter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitDashboardState.Loading
    )

    private fun buildReadyState(
        snapshot: DataSnapshot,
        dateKey: String,
        timeFilter: HabitTimeOfDay?
    ): HabitDashboardState.Ready {
        val today = HabitDateUtils.todayKey()
        val last30Keys = HabitDateUtils.recentDateKeys(30, today)
        val last7Keys = HabitDateUtils.recentDateKeys(7, today)
        val last35Keys = HabitDateUtils.recentDateKeys(35, today)

        val logsByHabit = snapshot.logs.groupBy { it.habitId }

        val habitsWithProgress = snapshot.habits.map { habit ->
            val target = habit.targetCount.coerceAtLeast(1)
            val habitLogs = logsByHabit[habit.id].orEmpty()
            val logsMap = habitLogs.associate { it.dateKey to it.count }
            val completedDates = habitLogs.filter { it.count >= target }.map { it.dateKey }.toSet()

            val currentStreak = StreakCalculator.calculateCurrentStreak(completedDates, today)
            val bestStreak = StreakCalculator.calculateBestStreak(completedDates)
            val rate30 = if (last30Keys.isEmpty()) 0 else {
                val hits = last30Keys.count { completedDates.contains(it) }
                ((hits * 100f) / last30Keys.size).toInt().coerceIn(0, 100)
            }

            HabitWithProgress(
                entity = habit,
                category = HabitCategory.fromKey(habit.categoryKey),
                timeOfDay = HabitTimeOfDay.fromKey(habit.timeOfDayKey),
                habitColor = HabitColor.fromKey(habit.colorKey),
                logsByDate = logsMap,
                selectedDateCount = logsMap[dateKey] ?: 0,
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                totalCompletions = completedDates.size,
                completionRate30Days = rate30
            )
        }

        val filtered = if (timeFilter == null) {
            habitsWithProgress
        } else {
            habitsWithProgress.filter { it.timeOfDay == timeFilter }
        }

        val totalHabits = habitsWithProgress.size
        val selectedCompleted = habitsWithProgress.count { it.isCompletedOnSelectedDate }

        fun summarizeDates(keys: List<String>): List<DailyCompletionSummary> {
            return keys.map { key ->
                val doneCount = habitsWithProgress.count { h ->
                    (h.logsByDate[key] ?: 0) >= h.targetCount
                }
                DailyCompletionSummary(
                    dateKey = key,
                    completedCount = doneCount,
                    totalHabits = totalHabits
                )
            }
        }

        val recent7 = summarizeDates(last7Keys)
        val heatmap35 = summarizeDates(last35Keys)

        val totalCompletionsAll = habitsWithProgress.sumOf { it.totalCompletions }
        val bestActiveStreak = habitsWithProgress.maxOfOrNull { it.currentStreak } ?: 0
        val weeklyRate = if (totalHabits == 0) {
            0
        } else {
            val totalPossible = totalHabits * 7
            val actualDone = recent7.sumOf { it.completedCount }
            ((actualDone * 100f) / totalPossible).toInt().coerceIn(0, 100)
        }
        val totalFocusMinutes = snapshot.focusSessions.sumOf { it.durationMinutes }

        return HabitDashboardState.Ready(
            selectedDateKey = dateKey,
            selectedTimeFilter = timeFilter,
            allHabits = habitsWithProgress,
            filteredHabits = filtered,
            selectedDayCompletedCount = selectedCompleted,
            selectedDayTotalCount = totalHabits,
            recent7Days = recent7,
            heatmap35Days = heatmap35,
            totalCompletionsAllTime = totalCompletionsAll,
            bestActiveStreak = bestActiveStreak,
            weeklyCompletionPercent = weeklyRate,
            totalFocusMinutes = totalFocusMinutes,
            focusSessions = snapshot.focusSessions,
            autoCompleteFocusHabit = snapshot.autoCompleteFocusHabit
        )
    }

    fun selectDate(dateKey: String) {
        selectedDateKey.value = dateKey
    }

    fun selectTimeFilter(timeOfDay: HabitTimeOfDay?) {
        selectedTimeFilter.value = timeOfDay
    }

    fun toggleTheme(currentlyDark: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDarkTheme(!currentlyDark)
        }
    }

    fun setAutoCompleteFocusHabit(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoCompleteFocusHabit(enabled)
        }
    }

    fun toggleHabit(habit: HabitWithProgress, dateKey: String = selectedDateKey.value) {
        viewModelScope.launch {
            if (habit.isCounter) {
                val delta = if (habit.isCompletedOnSelectedDate) -habit.selectedDateCount else 1
                repository.adjustHabitCount(habit.entity, dateKey, delta)
            } else {
                repository.toggleBinaryHabit(habit.entity, dateKey)
            }
        }
    }

    fun adjustHabitCount(habit: HabitWithProgress, delta: Int, dateKey: String = selectedDateKey.value) {
        viewModelScope.launch {
            repository.adjustHabitCount(habit.entity, dateKey, delta)
        }
    }

    fun saveHabit(
        editingHabitId: Long?,
        title: String,
        cue: String,
        category: HabitCategory,
        timeOfDay: HabitTimeOfDay,
        color: HabitColor,
        targetCount: Int,
        unit: String
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val cleanTarget = targetCount.coerceIn(1, 999)
        val cleanUnit = if (cleanTarget > 1) unit.trim() else ""

        viewModelScope.launch {
            if (editingHabitId == null) {
                repository.createHabit(
                    HabitEntity(
                        title = cleanTitle,
                        cue = cue.trim(),
                        categoryKey = category.key,
                        timeOfDayKey = timeOfDay.key,
                        colorKey = color.key,
                        targetCount = cleanTarget,
                        unit = cleanUnit
                    )
                )
            } else {
                val current = (dashboardState.value as? HabitDashboardState.Ready)
                    ?.allHabits
                    ?.firstOrNull { it.id == editingHabitId }
                    ?.entity
                repository.updateHabit(
                    HabitEntity(
                        id = editingHabitId,
                        title = cleanTitle,
                        cue = cue.trim(),
                        categoryKey = category.key,
                        timeOfDayKey = timeOfDay.key,
                        colorKey = color.key,
                        targetCount = cleanTarget,
                        unit = cleanUnit,
                        createdAtMillis = current?.createdAtMillis ?: System.currentTimeMillis(),
                        sortOrder = current?.sortOrder ?: 0
                    )
                )
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            if (_focusState.value.linkedHabitId == habitId) {
                _focusState.update { it.copy(linkedHabitId = null) }
            }
            repository.deleteHabit(habitId)
        }
    }

    fun addStarterPack(templates: List<HabitEntity>) {
        viewModelScope.launch {
            repository.addStarterHabits(templates)
        }
    }

    // --- Focus Timer Controls ---

    fun selectFocusPreset(minutes: Int, isBreak: Boolean) {
        timerJob?.cancel()
        val seconds = minutes * 60
        _focusState.update {
            it.copy(
                selectedDurationMinutes = minutes,
                isBreakMode = isBreak,
                totalSeconds = seconds,
                remainingSeconds = seconds,
                isRunning = false,
                showSavedBanner = false
            )
        }
    }

    fun linkHabitToFocus(habitId: Long?) {
        _focusState.update { it.copy(linkedHabitId = habitId, showSavedBanner = false) }
    }

    fun toggleFocusRunning() {
        val current = _focusState.value
        if (current.isRunning) {
            timerJob?.cancel()
            _focusState.update { it.copy(isRunning = false) }
        } else {
            if (current.remainingSeconds <= 0) {
                _focusState.update { it.copy(remainingSeconds = it.totalSeconds) }
            }
            _focusState.update { it.copy(isRunning = true, showSavedBanner = false) }
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (true) {
                    delay(1000L)
                    val state = _focusState.value
                    if (!state.isRunning) break
                    if (state.remainingSeconds <= 1) {
                        onFocusFinished()
                        break
                    } else {
                        _focusState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
                    }
                }
            }
        }
    }

    fun resetFocusTimer() {
        timerJob?.cancel()
        _focusState.update {
            it.copy(
                remainingSeconds = it.totalSeconds,
                isRunning = false,
                showSavedBanner = false
            )
        }
    }

    fun completeFocusSessionNow() {
        timerJob?.cancel()
        val elapsedMinutes = ((_focusState.value.totalSeconds - _focusState.value.remainingSeconds) / 60)
            .coerceAtLeast(_focusState.value.selectedDurationMinutes)
        viewModelScope.launch {
            saveFocusCompletion(elapsedMinutes)
        }
    }

    private suspend fun onFocusFinished() {
        val minutes = _focusState.value.selectedDurationMinutes
        saveFocusCompletion(minutes)
    }

    private suspend fun saveFocusCompletion(minutes: Int) {
        val state = _focusState.value
        val ready = dashboardState.value as? HabitDashboardState.Ready
        val linkedHabit = if (!state.isBreakMode && state.linkedHabitId != null) {
            ready?.allHabits?.firstOrNull { it.id == state.linkedHabitId }
        } else {
            null
        }

        if (!state.isBreakMode) {
            repository.recordFocusSession(
                habitId = linkedHabit?.id,
                habitTitle = linkedHabit?.title,
                durationMinutes = minutes,
                autoCompleteHabit = ready?.autoCompleteFocusHabit ?: true
            )
        }

        _focusState.update {
            it.copy(
                isRunning = false,
                remainingSeconds = it.totalSeconds,
                showSavedBanner = !state.isBreakMode
            )
        }
    }

    fun dismissFocusBanner() {
        _focusState.update { it.copy(showSavedBanner = false) }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    private data class DataSnapshot(
        val habits: List<HabitEntity>,
        val logs: List<HabitLogEntity>,
        val focusSessions: List<FocusSessionEntity>,
        val autoCompleteFocusHabit: Boolean
    )
}

class HabitViewModelFactory(
    private val repository: HabitRepository,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            return HabitViewModel(repository, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
