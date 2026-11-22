package com.example.domain.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object HabitDateUtils {
    private val keyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun todayKey(): String = formatKey(Calendar.getInstance())

    fun formatKey(calendar: Calendar): String = keyFormatter.format(calendar.time)

    fun daysAgoKey(daysAgo: Int, baseDateKey: String = todayKey()): String {
        val cal = parseKey(baseDateKey)
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return formatKey(cal)
    }

    fun parseKey(dateKey: String): Calendar {
        val cal = Calendar.getInstance()
        val parsed = runCatching { keyFormatter.parse(dateKey) }.getOrNull()
        if (parsed != null) {
            cal.time = parsed
        }
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    fun recentDateKeys(count: Int, endDateKey: String = todayKey()): List<String> {
        return (count - 1 downTo 0).map { offset ->
            daysAgoKey(offset, endDateKey)
        }
    }

    fun persianWeekdayShort(dateKey: String): String {
        val cal = parseKey(dateKey)
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "ش"
            Calendar.SUNDAY -> "ی"
            Calendar.MONDAY -> "د"
            Calendar.TUESDAY -> "س"
            Calendar.WEDNESDAY -> "چ"
            Calendar.THURSDAY -> "پ"
            Calendar.FRIDAY -> "ج"
            else -> ""
        }
    }

    fun persianWeekdayFull(dateKey: String): String {
        val cal = parseKey(dateKey)
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "شنبه"
            Calendar.SUNDAY -> "یکشنبه"
            Calendar.MONDAY -> "دوشنبه"
            Calendar.TUESDAY -> "سه‌شنبه"
            Calendar.WEDNESDAY -> "چهارشنبه"
            Calendar.THURSDAY -> "پنج‌شنبه"
            Calendar.FRIDAY -> "جمعه"
            else -> ""
        }
    }

    fun jalaliDayNumber(dateKey: String): Int {
        val (_, _, day) = toJalali(parseKey(dateKey))
        return day
    }

    fun jalaliDayMonthLabel(dateKey: String): String {
        val (_, month, day) = toJalali(parseKey(dateKey))
        val monthName = jalaliMonthName(month)
        return "${toPersianDigits(day)} $monthName"
    }

    fun jalaliFullHeader(dateKey: String): String {
        return "${persianWeekdayFull(dateKey)}، ${jalaliDayMonthLabel(dateKey)}"
    }

    fun toPersianDigits(number: Int): String = toPersianDigits(number.toString())

    fun toPersianDigits(input: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun jalaliMonthName(month: Int): String = when (month) {
        1 -> "فروردین"
        2 -> "اردیبهشت"
        3 -> "خرداد"
        4 -> "تیر"
        5 -> "مرداد"
        6 -> "شهریور"
        7 -> "مهر"
        8 -> "آبان"
        9 -> "آذر"
        10 -> "دی"
        11 -> "بهمن"
        12 -> "اسفند"
        else -> ""
    }

    // Exact Gregorian-to-Jalali conversion (Kazemi / Borkowski algorithm)
    fun toJalali(calendar: Calendar): Triple<Int, Int, Int> {
        val gy = calendar.get(Calendar.YEAR)
        val gm = calendar.get(Calendar.MONTH) + 1
        val gd = calendar.get(Calendar.DAY_OF_MONTH)

        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
            ((gy2 + 399) / 400) + gd + gdm[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return Triple(jy, jm, jd)
    }
}

object StreakCalculator {
    /**
     * Computes the active streak ending at [todayKey] (or yesterday if [todayKey] is not yet completed).
     */
    fun calculateCurrentStreak(
        completedDateKeys: Set<String>,
        todayKey: String = HabitDateUtils.todayKey()
    ): Int {
        if (completedDateKeys.isEmpty()) return 0
        val startOffset = if (completedDateKeys.contains(todayKey)) {
            0
        } else if (completedDateKeys.contains(HabitDateUtils.daysAgoKey(1, todayKey))) {
            1
        } else {
            return 0
        }

        var streak = 0
        var offset = startOffset
        while (completedDateKeys.contains(HabitDateUtils.daysAgoKey(offset, todayKey))) {
            streak++
            offset++
        }
        return streak
    }

    /**
     * Computes the longest consecutive streak across all recorded [completedDateKeys].
     */
    fun calculateBestStreak(completedDateKeys: Set<String>): Int {
        if (completedDateKeys.isEmpty()) return 0
        val sorted = completedDateKeys.sorted()
        var best = 1
        var current = 1
        for (i in 1 until sorted.size) {
            val expectedNext = HabitDateUtils.daysAgoKey(-1, sorted[i - 1])
            if (sorted[i] == expectedNext) {
                current++
                if (current > best) best = current
            } else {
                current = 1
            }
        }
        return best
    }
}
