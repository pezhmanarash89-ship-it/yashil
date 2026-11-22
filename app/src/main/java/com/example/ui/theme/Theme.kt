package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val HabitDarkColorScheme = darkColorScheme(
    primary = MintBright,
    onPrimary = Color(0xFF04261E),
    primaryContainer = DeepPineContainer,
    onPrimaryContainer = MintSoft,
    secondary = AmberLightDark,
    onSecondary = Color(0xFF3A1803),
    secondaryContainer = TerracottaContainerDark,
    onSecondaryContainer = AmberSoft,
    tertiary = SageBotanical,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF1A3E30),
    onTertiaryContainer = SageSoft,
    background = PineObsidianBg,
    onBackground = CreamInk,
    surface = PineCard,
    onSurface = CreamInk,
    surfaceVariant = PineSurfaceHigh,
    onSurfaceVariant = MutedPineInk,
    outline = PineBorder,
    outlineVariant = Color(0xFF1B3331),
    error = RoseAlertDark,
    onError = Color(0xFF3E0A0A)
)

private val HabitLightColorScheme = lightColorScheme(
    primary = EmeraldDeep,
    onPrimary = Color.White,
    primaryContainer = MintSoft,
    onPrimaryContainer = OnMintSoft,
    secondary = TerracottaWarm,
    onSecondary = Color.White,
    secondaryContainer = AmberSoft,
    onSecondaryContainer = OnAmberSoft,
    tertiary = SageBotanical,
    onTertiary = Color.White,
    tertiaryContainer = SageSoft,
    onTertiaryContainer = Color(0xFF0A2E1E),
    background = AlabasterBg,
    onBackground = CharcoalInk,
    surface = WarmWhiteCard,
    onSurface = CharcoalInk,
    surfaceVariant = SandSurface,
    onSurfaceVariant = MutedSlate,
    outline = SandBorder,
    outlineVariant = SandSurfaceHigh,
    error = RoseAlert,
    onError = Color.White
)

val HabitShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun HabitPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) HabitDarkColorScheme else HabitLightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HabitTypography,
            shapes = HabitShapes,
            content = content
        )
    }
}
