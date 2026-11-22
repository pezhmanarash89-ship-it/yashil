package com.example.ui.navigation

object HabitRoutes {
    const val ROUTE_TODAY = "today"
    const val ROUTE_ANALYTICS = "analytics"
    const val ROUTE_FOCUS = "focus"
    const val ROUTE_HABIT_DETAIL = "habit_detail/{habitId}"

    fun habitDetailRoute(habitId: Long): String = "habit_detail/$habitId"
}
