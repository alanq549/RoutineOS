package com.alan.routineos.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ColorExec,
    secondary = ColorPlan,
    tertiary = ColorPending,
    background = ColorBg,
    surface = ColorSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = ColorText,
    onSurface = ColorText,
    outline = ColorBorder
)

@Composable
fun RoutineOSTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )

}
