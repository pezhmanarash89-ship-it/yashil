package com.example

import com.example.domain.model.HabitDateUtils
import com.example.domain.model.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class StreakCalculatorTest {

    @Test
    fun currentStreak_countsConsecutiveDaysEndingToday() {
        val today = "2026-11-22"
        val completed = setOf("2026-11-20", "2026-11-21", "2026-11-22")
        assertEquals(3, StreakCalculator.calculateCurrentStreak(completed, today))
    }

    @Test
    fun currentStreak_includesYesterdayIfTodayNotYetCompleted() {
        val today = "2026-11-22"
        val completed = setOf("2026-11-19", "2026-11-20", "2026-11-21")
        assertEquals(3, StreakCalculator.calculateCurrentStreak(completed, today))
    }

    @Test
    fun currentStreak_resetsWhenGapMoreThanOneDay() {
        val today = "2026-11-22"
        val completed = setOf("2026-11-18", "2026-11-19", "2026-11-20")
        assertEquals(0, StreakCalculator.calculateCurrentStreak(completed, today))
    }

    @Test
    fun bestStreak_findsLongestRunAcrossHistory() {
        val completed = setOf(
            "2026-10-01", "2026-10-02", "2026-10-03", "2026-10-04",
            "2026-11-21", "2026-11-22"
        )
        assertEquals(4, StreakCalculator.calculateBestStreak(completed))
    }

    @Test
    fun jalaliConversion_matchesKnownDate() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 21, 12, 0, 0)
        }
        val (jy, jm, jd) = HabitDateUtils.toJalali(cal)
        assertEquals(1405, jy)
        assertEquals(1, jm)
        assertEquals(1, jd)
    }
}
