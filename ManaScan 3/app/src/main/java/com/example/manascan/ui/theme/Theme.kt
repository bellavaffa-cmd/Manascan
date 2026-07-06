package com.example.manascan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = MtgGold,
    onPrimary = InkPurpleDark,
    secondary = MtgGoldDark,
    background = InkPurpleDark,
    surface = InkPurple,
    onBackground = ParchmentLight,
    onSurface = ParchmentLight,
    error = ErrorRed
)

private val LightColors = lightColorScheme(
    primary = MtgGoldDark,
    onPrimary = ParchmentLight,
    secondary = MtgGold,
    background = ParchmentLight,
    surface = ParchmentLight,
    onBackground = InkPurple,
    onSurface = InkPurple,
    error = ErrorRed
)

@Composable
fun ManaScanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ManaScanTypography,
        content = content
    )
}
