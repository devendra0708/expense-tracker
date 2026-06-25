package com.expensetracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GreenPrimary = Color(0xFF2E7D32)
private val GreenDark = Color(0xFF1B5E20)
private val GreenLight = Color(0xFF4CAF50)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = GreenDark,
    secondary = Color(0xFF558B2F),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFB00020)
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenLight,
    onPrimary = GreenDark,
    primaryContainer = GreenDark,
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFF8BC34A),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E1E5),
    error = Color(0xFFCF6679)
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
