package com.example.domain.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

enum class HabitCategory(
    val key: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    HEALTH("health", R.string.cat_health, Icons.Rounded.Favorite),
    MINDFULNESS("mindfulness", R.string.cat_mindfulness, Icons.Rounded.SelfImprovement),
    LEARNING("learning", R.string.cat_learning, Icons.Rounded.AutoStories),
    FITNESS("fitness", R.string.cat_fitness, Icons.Rounded.FitnessCenter),
    PRODUCTIVITY("productivity", R.string.cat_productivity, Icons.Rounded.Bolt);

    companion object {
        fun fromKey(key: String): HabitCategory =
            entries.firstOrNull { it.key == key } ?: HEALTH
    }
}

enum class HabitTimeOfDay(
    val key: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    ANYTIME("anytime", R.string.time_anytime, Icons.Rounded.Schedule),
    MORNING("morning", R.string.time_morning, Icons.Rounded.LightMode),
    AFTERNOON("afternoon", R.string.time_afternoon, Icons.Rounded.WbTwilight),
    EVENING("evening", R.string.time_evening, Icons.Rounded.Bedtime);

    companion object {
        fun fromKey(key: String): HabitTimeOfDay =
            entries.firstOrNull { it.key == key } ?: ANYTIME
    }
}

enum class HabitColor(
    val key: String,
    val lightAccent: Color,
    val darkAccent: Color
) {
    EMERALD("emerald", Color(0xFF0F4C4C), Color(0xFF6DD5B0)),
    TERRACOTTA("terracotta", Color(0xFFD96B27), Color(0xFFF4A261)),
    SAGE("sage", Color(0xFF3D8B68), Color(0xFF7AD6AC)),
    OCEAN("ocean", Color(0xFF2364AA), Color(0xFF7FB3F5)),
    PLUM("plum", Color(0xFF7A4E7E), Color(0xFFD6A4DC)),
    GOLD("gold", Color(0xFFB57C14), Color(0xFFF2C56B));

    fun color(isDark: Boolean): Color = if (isDark) darkAccent else lightAccent

    companion object {
        fun fromKey(key: String): HabitColor =
            entries.firstOrNull { it.key == key } ?: EMERALD
    }
}
